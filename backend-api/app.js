const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const nodemailer = require('nodemailer');
const path = require('path');

const pool = require('./src/config/db');
const customerRoutes = require('./src/routes/customerRoutes');
const locationRoutes = require('./src/routes/locationRoutes');
const categoryRoutes = require('./src/routes/categoryRoutes');
const capturedRecordRoutes = require('./src/routes/capturedRecordRoutes');

const app = express();

const allowedOrigins = (process.env.CORS_ORIGIN || '')
  .split(',')
  .map((origin) => origin.trim())
  .filter(Boolean);

function getJwtSecret() {
  return process.env.JWT_SECRET || 'fieldsync-demo-secret-change-me';
}

function getFrontendUrl() {
  return process.env.FRONTEND_URL || 'http://localhost:5173';
}

function getMobileAppDownloadUrl() {
  return process.env.MOBILE_APP_DOWNLOAD_URL || '';
}

function hashPassword(password, salt) {
  return crypto
    .pbkdf2Sync(password, salt, 100000, 32, 'sha256')
    .toString('hex');
}

function createPasswordHash(password) {
  const salt = crypto.randomBytes(16).toString('hex');
  const passwordHash = hashPassword(password, salt);

  return `${salt}:${passwordHash}`;
}

function verifyPassword(password, storedPasswordHash) {
  if (!storedPasswordHash || !storedPasswordHash.includes(':')) {
    return false;
  }

  const [salt, storedHash] = storedPasswordHash.split(':');
  const calculatedHash = hashPassword(password, salt);

  const storedBuffer = Buffer.from(storedHash, 'hex');
  const calculatedBuffer = Buffer.from(calculatedHash, 'hex');

  if (storedBuffer.length !== calculatedBuffer.length) {
    return false;
  }

  return crypto.timingSafeEqual(storedBuffer, calculatedBuffer);
}

function generateSetupToken() {
  return crypto.randomBytes(32).toString('hex');
}

function buildUserPayload(user, clientType = 'web') {
  return {
    id: user.id,
    username: user.username,
    email: user.email,
    fullName: user.full_name,
    role: user.role,
    accessWeb: Boolean(user.access_web),
    accessMobile: Boolean(user.access_mobile),
    passwordChangeRequired: Boolean(user.password_change_required),
    clientType,
  };
}

function isClientAllowed(user, clientType) {
  if (user.role === 'admin') {
    return true;
  }

  if (clientType === 'mobile') {
    return Boolean(user.access_mobile);
  }

  return Boolean(user.access_web);
}

function authenticateToken(req, res, next) {
  const authHeader = req.headers.authorization || '';
  const token = authHeader.startsWith('Bearer ')
    ? authHeader.slice('Bearer '.length)
    : null;

  if (!token) {
    return res.status(401).json({
      message: 'Authentication token is required',
    });
  }

  try {
    req.user = jwt.verify(token, getJwtSecret());
    next();
  } catch (error) {
    return res.status(401).json({
      message: 'Invalid or expired authentication token',
    });
  }
}

function requireAdmin(req, res, next) {
  if (req.user?.role !== 'admin') {
    return res.status(403).json({
      message: 'Admin access is required',
    });
  }

  next();
}

async function sendUserInvitationEmail({
  user,
  setupLink,
  mobileAppDownloadUrl,
}) {
  if (!process.env.SMTP_HOST || !process.env.SMTP_USER || !process.env.SMTP_PASS) {
    return false;
  }

  const transporter = nodemailer.createTransport({
    host: process.env.SMTP_HOST,
    port: Number(process.env.SMTP_PORT || 587),
    secure: process.env.SMTP_SECURE === 'true',
    auth: {
      user: process.env.SMTP_USER,
      pass: process.env.SMTP_PASS,
    },
  });

  const accessList = [];

  if (user.access_web) {
    accessList.push(`Web App Access: ${getFrontendUrl()}`);
  }

  if (user.access_mobile && mobileAppDownloadUrl) {
    accessList.push(`Mobile App Download: ${mobileAppDownloadUrl}`);
  }

  const accessText = accessList.length > 0 ? accessList.join('\n') : 'No app link configured.';

  await transporter.sendMail({
    from: process.env.EMAIL_FROM || process.env.SMTP_USER,
    to: user.email,
    subject: 'Your FieldSync account has been created',
    text: `Hello ${user.full_name || user.username},

Your FieldSync account has been created by the administrator.

Username: ${user.username}

Please confirm your account and set your password using the link below:
${setupLink}

Access details:
${accessText}

For security reasons, your password is not sent by email. You must set your own password using the setup link.

Regards,
FieldSync Team`,
  });

  return true;
}

app.use(
  helmet({
    crossOriginResourcePolicy: false,
  })
);

app.use(
  cors({
    origin(origin, callback) {
      if (!origin || allowedOrigins.length === 0 || allowedOrigins.includes(origin)) {
        return callback(null, true);
      }

      return callback(new Error('Not allowed by CORS'));
    },
  })
);

app.use(
  rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 300,
    standardHeaders: true,
    legacyHeaders: false,
  })
);

app.use(express.json({ limit: '10mb' }));
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

app.get('/', (req, res) => {
  res.json({
    message: 'Mobile Data Capture Backend API is running',
  });
});

app.get('/health', (req, res) => {
  res.json({
    status: 'OK',
    timestamp: new Date().toISOString(),
  });
});

/**
 * LOGIN
 * Public route.
 * Checks username/password from database and validates app access permission.
 */
app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password, clientType } = req.body;
    const resolvedClientType = clientType === 'mobile' ? 'mobile' : 'web';

    if (!username || !password) {
      return res.status(400).json({
        message: 'Username and password are required',
      });
    }

    const result = await pool.query(
      `
      SELECT
        id,
        username,
        email,
        password_hash,
        full_name,
        role,
        access_web,
        access_mobile,
        password_change_required,
        confirmed_at,
        is_active
      FROM users
      WHERE LOWER(username) = LOWER($1)
      LIMIT 1
      `,
      [username.trim()]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({
        message: 'Invalid username or password',
      });
    }

    const user = result.rows[0];

    if (!user.is_active || !verifyPassword(password, user.password_hash)) {
      return res.status(401).json({
        message: 'Invalid username or password',
      });
    }

    if (!user.confirmed_at && user.role !== 'admin') {
      return res.status(403).json({
        message: 'Please confirm your account and set your password using the email link before logging in',
      });
    }

    if (!isClientAllowed(user, resolvedClientType)) {
      return res.status(403).json({
        message: `Your account is not allowed to access the ${resolvedClientType} application`,
      });
    }

    const tokenPayload = buildUserPayload(user, resolvedClientType);

    const token = jwt.sign(tokenPayload, getJwtSecret(), {
      expiresIn: process.env.JWT_EXPIRES_IN || '12h',
    });

    res.json({
      message: 'Login successful',
      token,
      user: tokenPayload,
    });
  } catch (error) {
    res.status(500).json({
      message: 'Login failed',
      error: error.message,
    });
  }
});

/**
 * SETUP PASSWORD
 * Public route.
 * User opens setup link from email and sets their own password.
 */
app.post('/api/auth/setup-password', async (req, res) => {
  try {
    const { token, password } = req.body;

    if (!token) {
      return res.status(400).json({
        message: 'Setup token is required',
      });
    }

    if (!password || password.length < 6) {
      return res.status(400).json({
        message: 'Password must contain at least 6 characters',
      });
    }

    const passwordHash = createPasswordHash(password);

    const result = await pool.query(
      `
      UPDATE users
      SET password_hash = $1,
          password_change_required = FALSE,
          confirmed_at = CURRENT_TIMESTAMP,
          confirmation_token = NULL,
          confirmation_expires_at = NULL,
          updated_at = CURRENT_TIMESTAMP
      WHERE confirmation_token = $2
        AND confirmation_expires_at > CURRENT_TIMESTAMP
        AND is_active = TRUE
      RETURNING
        id,
        username,
        email,
        full_name,
        role,
        access_web,
        access_mobile,
        password_change_required
      `,
      [passwordHash, token]
    );

    if (result.rows.length === 0) {
      return res.status(400).json({
        message: 'Invalid or expired setup link',
      });
    }

    res.json({
      message: 'Password set successfully. You can now login.',
      user: result.rows[0],
    });
  } catch (error) {
    res.status(500).json({
      message: 'Password setup failed',
      error: error.message,
    });
  }
});

/**
 * CHANGE PASSWORD
 * Authenticated route.
 * Any logged-in user can change their password.
 */
app.post('/api/auth/change-password', authenticateToken, async (req, res) => {
  try {
    const { currentPassword, newPassword } = req.body;

    if (!currentPassword || !newPassword) {
      return res.status(400).json({
        message: 'Current password and new password are required',
      });
    }

    if (newPassword.length < 6) {
      return res.status(400).json({
        message: 'New password must contain at least 6 characters',
      });
    }

    const result = await pool.query(
      `
      SELECT id, password_hash
      FROM users
      WHERE id = $1 AND is_active = TRUE
      LIMIT 1
      `,
      [req.user.id]
    );

    if (
      result.rows.length === 0 ||
      !verifyPassword(currentPassword, result.rows[0].password_hash)
    ) {
      return res.status(401).json({
        message: 'Current password is incorrect',
      });
    }

    await pool.query(
      `
      UPDATE users
      SET password_hash = $1,
          password_change_required = FALSE,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = $2
      `,
      [createPasswordHash(newPassword), req.user.id]
    );

    res.json({
      message: 'Password changed successfully',
    });
  } catch (error) {
    res.status(500).json({
      message: 'Password change failed',
      error: error.message,
    });
  }
});

app.get('/api/auth/me', authenticateToken, (req, res) => {
  res.json({
    user: req.user,
  });
});

/**
 * ADMIN: LIST USERS
 */
app.get('/api/admin/users', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const result = await pool.query(
      `
      SELECT
        id,
        username,
        email,
        full_name,
        role,
        access_web,
        access_mobile,
        is_active,
        confirmed_at,
        password_change_required,
        created_at
      FROM users
      ORDER BY created_at DESC
      `
    );

    res.json(result.rows);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch users',
      error: error.message,
    });
  }
});

/**
 * ADMIN: CREATE USER
 * Admin creates the user and assigns Web/Mobile/Both permissions.
 */
app.post('/api/admin/users', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const { fullName, username, email, accessWeb, accessMobile } = req.body;

    if (!fullName || !fullName.trim()) {
      return res.status(400).json({
        message: 'Full name is required',
      });
    }

    if (!username || !username.trim()) {
      return res.status(400).json({
        message: 'Username is required',
      });
    }

    if (!email || !email.trim()) {
      return res.status(400).json({
        message: 'Email is required for sending the account setup link',
      });
    }

    if (!accessWeb && !accessMobile) {
      return res.status(400).json({
        message: 'Select at least one access type: Web app, Mobile app, or both',
      });
    }

    const normalizedUsername = username.trim().toLowerCase();
    const normalizedEmail = email.trim().toLowerCase();

    const existingUser = await pool.query(
      `
      SELECT id
      FROM users
      WHERE LOWER(username) = $1 OR LOWER(email) = $2
      LIMIT 1
      `,
      [normalizedUsername, normalizedEmail]
    );

    if (existingUser.rows.length > 0) {
      return res.status(409).json({
        message: 'Username or email is already registered',
      });
    }

    const setupToken = generateSetupToken();
    const setupLink = `${getFrontendUrl().replace(/\/$/, '')}/?setupToken=${setupToken}`;
    const mobileAppDownloadUrl = getMobileAppDownloadUrl();

    const result = await pool.query(
      `
      INSERT INTO users (
        username,
        email,
        password_hash,
        full_name,
        role,
        access_web,
        access_mobile,
        password_change_required,
        confirmation_token,
        confirmation_expires_at,
        is_active,
        created_by
      )
      VALUES (
        $1,
        $2,
        'PASSWORD_NOT_SET',
        $3,
        'user',
        $4,
        $5,
        TRUE,
        $6,
        CURRENT_TIMESTAMP + INTERVAL '7 days',
        TRUE,
        $7
      )
      RETURNING
        id,
        username,
        email,
        full_name,
        role,
        access_web,
        access_mobile,
        is_active,
        confirmed_at,
        password_change_required,
        created_at
      `,
      [
        normalizedUsername,
        normalizedEmail,
        fullName.trim(),
        Boolean(accessWeb),
        Boolean(accessMobile),
        setupToken,
        req.user.id,
      ]
    );

    const user = result.rows[0];
    let emailSent = false;

    try {
      emailSent = await sendUserInvitationEmail({
        user,
        setupLink,
        mobileAppDownloadUrl,
      });
    } catch (emailError) {
      emailSent = false;
      console.error('Failed to send user invitation email:', emailError.message);
    }

    res.status(201).json({
      message: emailSent
        ? 'User created and setup email sent successfully'
        : 'User created. Email was not sent because SMTP is not configured or failed.',
      user,
      emailSent,
      setupLink,
      mobileAppDownloadUrl,
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to create user',
      error: error.message,
    });
  }
});

app.patch('/api/admin/users/:id', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const { id } = req.params;
    const { fullName, email, accessWeb, accessMobile, isActive } = req.body;

    if (Number(id) === req.user.id && isActive === false) {
      return res.status(400).json({
        message: 'You cannot deactivate your own admin account',
      });
    }

    const existingUser = await pool.query(
      `
      SELECT id, role
      FROM users
      WHERE id = $1
      LIMIT 1
      `,
      [id]
    );

    if (existingUser.rows.length === 0) {
      return res.status(404).json({
        message: 'User not found',
      });
    }

    if (existingUser.rows[0].role === 'admin') {
      return res.status(400).json({
        message: 'Admin account details cannot be edited from user management',
      });
    }

    if (accessWeb === false && accessMobile === false) {
      return res.status(400).json({
        message: 'User must have at least one access type',
      });
    }

    const normalizedEmail = email ? email.trim().toLowerCase() : null;

    if (normalizedEmail) {
      const emailCheck = await pool.query(
        `
        SELECT id
        FROM users
        WHERE LOWER(email) = $1
          AND id <> $2
        LIMIT 1
        `,
        [normalizedEmail, id]
      );

      if (emailCheck.rows.length > 0) {
        return res.status(409).json({
          message: 'Email is already used by another user',
        });
      }
    }

    const result = await pool.query(
      `
      UPDATE users
      SET full_name = COALESCE($1, full_name),
          email = COALESCE($2, email),
          access_web = COALESCE($3, access_web),
          access_mobile = COALESCE($4, access_mobile),
          is_active = COALESCE($5, is_active),
          updated_at = CURRENT_TIMESTAMP
      WHERE id = $6
      RETURNING
        id,
        username,
        email,
        full_name,
        role,
        access_web,
        access_mobile,
        is_active,
        confirmed_at,
        password_change_required,
        created_at
      `,
      [
        fullName?.trim() || null,
        normalizedEmail,
        accessWeb,
        accessMobile,
        isActive,
        id,
      ]
    );

    res.json({
      message: 'User updated successfully',
      user: result.rows[0],
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to update user',
      error: error.message,
    });
  }
});


app.delete('/api/admin/users/:id', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const { id } = req.params;

    if (Number(id) === req.user.id) {
      return res.status(400).json({
        message: 'You cannot delete your own admin account',
      });
    }

    const existingUser = await pool.query(
      `
      SELECT id, role
      FROM users
      WHERE id = $1
      LIMIT 1
      `,
      [id]
    );

    if (existingUser.rows.length === 0) {
      return res.status(404).json({
        message: 'User not found',
      });
    }

    if (existingUser.rows[0].role === 'admin') {
      return res.status(400).json({
        message: 'Admin accounts cannot be deleted from user management',
      });
    }

    const result = await pool.query(
      `
      DELETE FROM users
      WHERE id = $1
      RETURNING
        id,
        username,
        email,
        full_name,
        role
      `,
      [id]
    );

    res.json({
      message: 'User permanently deleted successfully',
      user: result.rows[0],
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to delete user',
      error: error.message,
    });
  }
});

/**
 * ADMIN: UPDATE USER ACCESS
 */
app.patch('/api/admin/users/:id/access', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const { id } = req.params;
    const { accessWeb, accessMobile, isActive } = req.body;

    if (Number(id) === req.user.id && isActive === false) {
      return res.status(400).json({
        message: 'You cannot deactivate your own admin account',
      });
    }

    const result = await pool.query(
      `
      UPDATE users
      SET access_web = COALESCE($1, access_web),
          access_mobile = COALESCE($2, access_mobile),
          is_active = COALESCE($3, is_active),
          updated_at = CURRENT_TIMESTAMP
      WHERE id = $4
      RETURNING
        id,
        username,
        email,
        full_name,
        role,
        access_web,
        access_mobile,
        is_active,
        confirmed_at,
        password_change_required,
        created_at
      `,
      [accessWeb, accessMobile, isActive, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'User not found',
      });
    }

    res.json({
      message: 'User access updated successfully',
      user: result.rows[0],
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to update user access',
      error: error.message,
    });
  }
});

app.use('/api/customers', authenticateToken, customerRoutes);
app.use('/api/locations', authenticateToken, locationRoutes);
app.use('/api/categories', authenticateToken, categoryRoutes);
app.use('/api/captured-records', authenticateToken, capturedRecordRoutes);

module.exports = app;