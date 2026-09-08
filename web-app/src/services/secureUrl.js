function isLocalhost(hostname) {
  const host = hostname
    .replace(/^\[|\]$/g, '')
    .toLowerCase()

  return (
    host === 'localhost' ||
    host === '127.0.0.1' ||
    host === '::1'
  )
}

export function requireSecureServiceUrl(value, name) {
  let url

  try {
    url = new URL(value)
  } catch {
    throw new Error(`${name} must be a valid absolute URL`)
  }

  if (url.protocol === 'https:') {
    return value.replace(/\/+$/, '')
  }

  if (
    url.protocol === 'http:' &&
    isLocalhost(url.hostname)
  ) {
    return value.replace(/\/+$/, '')
  }

  throw new Error(
    `${name} must use HTTPS outside localhost`
  )
}