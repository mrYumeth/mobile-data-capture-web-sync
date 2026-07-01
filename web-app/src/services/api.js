const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000'

const AUTH_TOKEN_KEY = 'fieldsync-auth-token'
const AUTH_STATE_KEY = 'fieldsync-admin-auth'

function getAuthToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY)
}

function clearAuthState() {
  localStorage.removeItem(AUTH_TOKEN_KEY)
  localStorage.removeItem(AUTH_STATE_KEY)
}

async function request(path, options = {}) {
  const token = getAuthToken()

  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  })

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthState()
    }

    throw new Error(data?.message || 'API request failed')
  }

  return data
}

export const authApi = {
  login(credentials) {
    return request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        ...credentials,
        clientType: 'web',
      }),
    })
  },

  setupPassword(data) {
    return request('/api/auth/setup-password', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  changePassword(data) {
    return request('/api/auth/change-password', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  me() {
    return request('/api/auth/me')
  },
}

export const userApi = {
  getAll() {
    return request('/api/admin/users')
  },

  create(user) {
    return request('/api/admin/users', {
      method: 'POST',
      body: JSON.stringify(user),
    })
  },

  update(id, user) {
    return request(`/api/admin/users/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(user),
    })
  },

  updateAccess(id, accessData) {
    return request(`/api/admin/users/${id}/access`, {
      method: 'PATCH',
      body: JSON.stringify(accessData),
    })
  },

    remove(id) {
    return request(`/api/admin/users/${id}`, {
      method: 'DELETE',
    })
  },
  
}

export const customerApi = {
  getAll() {
    return request('/api/customers')
  },

  create(customer) {
    return request('/api/customers', {
      method: 'POST',
      body: JSON.stringify(customer),
    })
  },

  update(id, customer) {
    return request(`/api/customers/${id}`, {
      method: 'PUT',
      body: JSON.stringify(customer),
    })
  },

  remove(id) {
    return request(`/api/customers/${id}`, {
      method: 'DELETE',
    })
  },
}

export const locationApi = {
  getAll() {
    return request('/api/locations')
  },

  create(location) {
    return request('/api/locations', {
      method: 'POST',
      body: JSON.stringify(location),
    })
  },

  update(id, location) {
    return request(`/api/locations/${id}`, {
      method: 'PUT',
      body: JSON.stringify(location),
    })
  },

  remove(id) {
    return request(`/api/locations/${id}`, {
      method: 'DELETE',
    })
  },
}

export const categoryApi = {
  getAll() {
    return request('/api/categories')
  },

  create(category) {
    return request('/api/categories', {
      method: 'POST',
      body: JSON.stringify(category),
    })
  },

  update(id, category) {
    return request(`/api/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(category),
    })
  },

  remove(id) {
    return request(`/api/categories/${id}`, {
      method: 'DELETE',
    })
  },
}

export const dashboardApi = {
  async getSummary() {
    const [customers, locations, categories, capturedRecords] =
      await Promise.all([
        request('/api/customers'),
        request('/api/locations'),
        request('/api/categories'),
        request('/api/captured-records'),
      ])

    return {
      customers: customers.length,
      locations: locations.length,
      categories: categories.length,
      capturedRecords: capturedRecords.length,
    }
  },
}

export const capturedRecordApi = {
  getAll() {
    return request('/api/captured-records')
  },

  getById(id) {
    return request(`/api/captured-records/${id}`)
  },
}