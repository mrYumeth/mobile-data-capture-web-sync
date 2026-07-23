let cachedJwks = null;

async function getJose() {
  return import('jose');
}

function getKeycloakIssuer() {
  return process.env.KEYCLOAK_ISSUER;
}

function getAllowedClients() {
  return (process.env.KEYCLOAK_ALLOWED_CLIENTS || '')
    .split(',')
    .map((clientId) => clientId.trim())
    .filter(Boolean);
}

async function getJwks() {
  if (!cachedJwks) {
    const { createRemoteJWKSet } = await getJose();
    cachedJwks = createRemoteJWKSet(
      new URL(`${getKeycloakIssuer()}/protocol/openid-connect/certs`)
    );
  }

  return cachedJwks;
}

async function verifyKeycloakToken(token) {
  const issuer = getKeycloakIssuer();

  if (!issuer) {
    throw new Error('KEYCLOAK_ISSUER is not configured');
  }

  const { jwtVerify } = await getJose();
  const jwks = await getJwks();

  const { payload } = await jwtVerify(token, jwks, {
    issuer,
  });

  const allowedClients = getAllowedClients();

  if (allowedClients.length > 0) {
    const authorizedParty = payload.azp;
    const audience = Array.isArray(payload.aud)
      ? payload.aud
      : [payload.aud].filter(Boolean);

    const isAllowedClient =
      allowedClients.includes(authorizedParty) ||
      audience.some((audienceValue) => allowedClients.includes(audienceValue));

    if (!isAllowedClient) {
      throw new Error('Token was not issued for an allowed client');
    }
  }

  return payload;
}

module.exports = {
  verifyKeycloakToken,
};