const crypto = require('crypto');

function getBaseUrl() {
  return (process.env.KEYCLOAK_ADMIN_BASE_URL || '').replace(/\/$/, '');
}

function getRealm() {
  return process.env.KEYCLOAK_ADMIN_REALM || process.env.VITE_KEYCLOAK_REALM || 'fieldsync';
}

function isKeycloakAdminConfigured() {
  return Boolean(
    getBaseUrl() &&
      getRealm() &&
      process.env.KEYCLOAK_ADMIN_CLIENT_ID &&
      process.env.KEYCLOAK_ADMIN_CLIENT_SECRET
  );
}

async function getAdminAccessToken() {
  const baseUrl = getBaseUrl();
  const realm = getRealm();

  const response = await fetch(
    `${baseUrl}/realms/${realm}/protocol/openid-connect/token`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        grant_type: 'client_credentials',
        client_id: process.env.KEYCLOAK_ADMIN_CLIENT_ID,
        client_secret: process.env.KEYCLOAK_ADMIN_CLIENT_SECRET,
      }),
    }
  );

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(
      data?.error_description ||
        data?.error ||
        'Failed to get Keycloak admin access token'
    );
  }

  return data.access_token;
}

function splitFullName(fullName) {
  const parts = fullName.trim().split(/\s+/);

  if (parts.length === 1) {
    return {
      firstName: parts[0],
      lastName: '',
    };
  }

  return {
    firstName: parts[0],
    lastName: parts.slice(1).join(' '),
  };
}

function getUserIdFromLocation(locationHeader) {
  if (!locationHeader) {
    return null;
  }

  const parts = locationHeader.split('/');
  return parts[parts.length - 1] || null;
}

async function createKeycloakUser({
  username,
  email,
  fullName,
  accessWeb,
  accessMobile,
}) {
  if (!isKeycloakAdminConfigured()) {
    throw new Error('Keycloak Admin API is not configured');
  }

  const baseUrl = getBaseUrl();
  const realm = getRealm();
  const token = await getAdminAccessToken();
  const { firstName, lastName } = splitFullName(fullName);

  const response = await fetch(`${baseUrl}/admin/realms/${realm}/users`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      username,
      email,
      firstName,
      lastName,
    enabled: true,
    emailVerified: true,
    requiredActions: ['UPDATE_PASSWORD'],
      attributes: {
        fieldsync_access_web: [String(Boolean(accessWeb))],
        fieldsync_access_mobile: [String(Boolean(accessMobile))],
      },
    }),
  });

  if (response.status === 409) {
    throw new Error('A Keycloak user with this username or email already exists');
  }

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Failed to create Keycloak user: ${errorText}`);
  }

  const keycloakUserId = getUserIdFromLocation(response.headers.get('location'));

  if (!keycloakUserId) {
    throw new Error('Keycloak user was created, but user ID was not returned');
  }

  return keycloakUserId;
}

async function sendKeycloakUserInviteEmail(keycloakUserId) {
  if (process.env.KEYCLOAK_SEND_INVITE_EMAIL !== 'true') {
    return false;
  }

  const baseUrl = getBaseUrl();
  const realm = getRealm();
  const token = await getAdminAccessToken();

  const query = new URLSearchParams();

  if (process.env.KEYCLOAK_WEB_CLIENT_ID) {
    query.set('client_id', process.env.KEYCLOAK_WEB_CLIENT_ID);
  }

  if (process.env.KEYCLOAK_WEB_REDIRECT_URI) {
    query.set('redirect_uri', process.env.KEYCLOAK_WEB_REDIRECT_URI);
  }

  const queryText = query.toString();
  const url = `${baseUrl}/admin/realms/${realm}/users/${keycloakUserId}/execute-actions-email${
    queryText ? `?${queryText}` : ''
  }`;

  const response = await fetch(url, {
    method: 'PUT',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(['VERIFY_EMAIL', 'UPDATE_PASSWORD']),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Failed to send Keycloak invite email: ${errorText}`);
  }

  return true;
}

function shouldCreateTemporaryPassword() {
  return process.env.KEYCLOAK_CREATE_TEMP_PASSWORD === 'true';
}

function generateTemporaryPassword() {
  return `Fs-${crypto.randomBytes(6).toString('base64url')}!9`;
}

async function setKeycloakTemporaryPassword(keycloakUserId, temporaryPassword) {
  if (!isKeycloakAdminConfigured()) {
    throw new Error('Keycloak Admin API is not configured');
  }

  const baseUrl = getBaseUrl();
  const realm = getRealm();
  const token = await getAdminAccessToken();

  const response = await fetch(
    `${baseUrl}/admin/realms/${realm}/users/${keycloakUserId}/reset-password`,
    {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        type: 'password',
        value: temporaryPassword,
        temporary: true,
      }),
    }
  );

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Failed to set Keycloak temporary password: ${errorText}`);
  }

  return true;
}

module.exports = {
  isKeycloakAdminConfigured,
  createKeycloakUser,
  sendKeycloakUserInviteEmail,
  shouldCreateTemporaryPassword,
  generateTemporaryPassword,
  setKeycloakTemporaryPassword,
};