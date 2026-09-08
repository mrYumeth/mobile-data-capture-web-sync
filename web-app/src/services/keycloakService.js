import Keycloak from 'keycloak-js'
import { requireSecureServiceUrl } from './secureUrl'

const keycloak = new Keycloak({
  url: requireSecureServiceUrl(
    import.meta.env.VITE_KEYCLOAK_URL ||
      'http://localhost:8080',
    'Keycloak URL'
  ),
  realm:
    import.meta.env.VITE_KEYCLOAK_REALM ||
    'fieldsync',
  clientId:
    import.meta.env.VITE_KEYCLOAK_CLIENT_ID ||
    'fieldsync-web',
})

let initPromise = null
let refreshPromise = null

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

  if (!refreshPromise) {
    refreshPromise = (async () => {
      await keycloak.updateToken(60)
      return keycloak.token
    })()
  }

  try {
    return await refreshPromise
  } finally {
    refreshPromise = null
  }
}

export function clearKeycloakToken() {
  keycloak.clearToken()
}