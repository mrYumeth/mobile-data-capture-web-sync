import Keycloak from 'keycloak-js'

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'fieldsync',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'fieldsync-web',
})

let initPromise = null

export function initKeycloak() {
  if (!initPromise) {
    initPromise = keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    })
  }

  return initPromise
}

export async function loginWithKeycloak() {
  await initKeycloak()

  return keycloak.login({
    redirectUri: window.location.origin,
  })
}

export async function logoutFromKeycloak() {
  await initKeycloak()

  return keycloak.logout({
    redirectUri: window.location.origin,
  })
}

export function getKeycloakToken() {
  return keycloak.token
}

export async function refreshKeycloakToken() {
  if (!keycloak.authenticated) {
    return null
  }

  await keycloak.updateToken(60)
  return keycloak.token
}