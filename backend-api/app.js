const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
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

function hashPassword(password, salt) {
  return crypto
    .pbkdf2Sync(password, salt, 100000, 32, 'sha256')
    .toString('hex');
}

function verifyPassword(password, storedPasswordHash) {
  if (!storedPasswordHash || !storedPasswordHash.includes(':')) {
    return false;
  }

function createPasswordHash(password) {
  const salt = crypto.randomBytes(16).toString('hex');
  const passwordHash = hashPassword(password, salt);

  return `${salt}:${passwordHash}`;
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

app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password, clientType } = req.body;

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
        password_hash,
        full_name,
        role,
        is_active
      FROM users
      WHERE username = $1
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

    const tokenPayload = {
      id: user.id,
      username: user.username,
      fullName: user.full_name,
      role: user.role,
      clientType: clientType || 'web',
    };

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

app.post('/api/auth/register', async (req, res) => {
  try {
    const { username, password, fullName, email, clientType } = req.body;

    if (!username || !username.trim()) {
      return res.status(400).json({
        message: 'Username is required',
      });
    }

    if (!password || password.length < 6) {
      return res.status(400).json({
        message: 'Password must contain at least 6 characters',
      });
    }

    if (!fullName || !fullName.trim()) {
      return res.status(400).json({
        message: 'Full name is required',
      });
    }

    const normalizedUsername = username.trim().toLowerCase();
    const normalizedEmail = email ? email.trim().toLowerCase() : null;

    const existingUser = await pool.query(
      `
      SELECT id
      FROM users
      WHERE LOWER(username) = $1
         OR ($2::text IS NOT NULL AND LOWER(email) = $2)
      LIMIT 1
      `,
      [normalizedUsername, normalizedEmail]
    );

    if (existingUser.rows.length > 0) {
      return res.status(409).json({
        message: 'Username or email is already registered',
      });
    }

    const passwordHash = createPasswordHash(password);

    const result = await pool.query(
      `
      INSERT INTO users (
        username,
        email,
        password_hash,
        full_name,
        role,
        is_active
      )
      VALUES ($1, $2, $3, $4, $5, TRUE)
      RETURNING id, username, email, full_name, role, is_active
      `,
      [
        normalizedUsername,
        normalizedEmail,
        passwordHash,
        fullName.trim(),
        'user',
      ]
    );

    const user = result.rows[0];

    const tokenPayload = {
      id: user.id,
      username: user.username,
      email: user.email,
      fullName: user.full_name,
      role: user.role,
      clientType: clientType || 'web',
    };

    const token = jwt.sign(tokenPayload, getJwtSecret(), {
      expiresIn: process.env.JWT_EXPIRES_IN || '12h',
    });

    res.status(201).json({
      message: 'Registration successful',
      token,
      user: tokenPayload,
    });
  } catch (error) {
    res.status(500).json({
      message: 'Registration failed',
      error: error.message,
    });
  }
});

app.get('/api/auth/me', authenticateToken, (req, res) => {
  res.json({
    user: req.user,
  });
});

app.use('/api/customers', authenticateToken, customerRoutes);
app.use('/api/locations', authenticateToken, locationRoutes);
app.use('/api/categories', authenticateToken, categoryRoutes);
app.use('/api/captured-records', authenticateToken, capturedRecordRoutes);

module.exports = app;