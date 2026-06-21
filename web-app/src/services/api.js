const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(data?.message || 'API request failed')
  }

  return data
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
    return request('/api/locations');
  },

  create(location) {
    return request('/api/locations', {
      method: 'POST',
      body: JSON.stringify(location),
    });
  },

  update(id, location) {
    return request(`/api/locations/${id}`, {
      method: 'PUT',
      body: JSON.stringify(location),
    });
  },

  remove(id) {
    return request(`/api/locations/${id}`, {
      method: 'DELETE',
    });
  },
};

export const categoryApi = {
  getAll() {
    return request('/api/categories');
  },

  create(category) {
    return request('/api/categories', {
      method: 'POST',
      body: JSON.stringify(category),
    });
  },

  update(id, category) {
    return request(`/api/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(category),
    });
  },

  remove(id) {
    return request(`/api/categories/${id}`, {
      method: 'DELETE',
    });
  },
};

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
    return request('/api/captured-records');
  },

  getById(id) {
    return request(`/api/captured-records/${id}`);
  },
};