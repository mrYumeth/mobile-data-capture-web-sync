import { useEffect, useState } from 'react'
import { userApi } from '../services/api'
import ChangePasswordPage from './ChangePasswordPage'

function UserManagementPage() {
  const [users, setUsers] = useState([])
  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    accessWeb: true,
    accessMobile: false,
  })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [lastSetupLink, setLastSetupLink] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('users')
  const [editingUserId, setEditingUserId] = useState(null)

  async function loadUsers() {
    try {
      const data = await userApi.getAll()
      setUsers(data)
    } catch (error) {
      setError(error.message || 'Failed to load users.')
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  function handleChange(event) {
    const { name, value, checked, type } = event.target

    setFormData((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

async function handleCreateUser(event) {
  event.preventDefault()
  setError('')
  setMessage('')
  setLastSetupLink('')

  try {
    setIsLoading(true)

    if (editingUserId) {
      const result = await userApi.update(editingUserId, {
        fullName: formData.fullName,
        email: formData.email,
        accessWeb: formData.accessWeb,
        accessMobile: formData.accessMobile,
      })

      setMessage(result.message || 'User updated successfully.')
      handleCancelEdit()
      await loadUsers()
      return
    }

    const result = await userApi.create(formData)

    setMessage(result.message || 'User created successfully.')
    setLastSetupLink(result.setupLink || '')

    setFormData({
      fullName: '',
      username: '',
      email: '',
      accessWeb: true,
      accessMobile: false,
    })

    await loadUsers()
  } catch (error) {
    setError(error.message || 'Failed to save user.')
  } finally {
    setIsLoading(false)
  }
}

  function handleEditUser(user) {
  setEditingUserId(user.id)

  setFormData({
    fullName: user.full_name || '',
    username: user.username || '',
    email: user.email || '',
    accessWeb: Boolean(user.access_web),
    accessMobile: Boolean(user.access_mobile),
  })

  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function handleDeleteUser(user) {
  const confirmed = window.confirm(
  `Are you sure you want to permanently delete ${user.full_name || user.username}? This action cannot be undone.`
  )

  if (!confirmed) {
    return
  }

  try {
    setError('')
    setMessage('')

    const result = await userApi.remove(user.id)

    setMessage(result.message || 'User deleted successfully.')
    await loadUsers()
  } catch (error) {
    setError(error.message || 'Failed to delete user.')
  }
}

function handleCancelEdit() {
  setEditingUserId(null)

  setFormData({
    fullName: '',
    username: '',
    email: '',
    accessWeb: true,
    accessMobile: false,
  })
}

  async function updateAccess(user, changes) {
    try {
      setError('')
      setMessage('')

      const result = await userApi.updateAccess(user.id, changes)

      setMessage(result.message || 'User access updated.')
      await loadUsers()
    } catch (error) {
      setError(error.message || 'Failed to update access.')
    }
  }

  return (
    <section>
      <div className="mb-6">
        <p className="text-sm font-bold uppercase tracking-[0.25em] text-[#EB5979]">
          Admin
        </p>
        <h2 className="mt-2 text-3xl font-extrabold">Users</h2>
        <p className="mt-2 text-gray-500">
          Create users and assign web or mobile app access, and change password.
        </p>
      </div>
        <div className="mb-6 flex flex-wrap gap-3">
        <button
            type="button"
            onClick={() => setActiveTab('users')}
            className={`rounded-full px-5 py-2 text-sm font-bold transition ${
            activeTab === 'users'
                ? 'bg-[#EB5979] text-white shadow-lg shadow-[#EB5979]/30'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            }`}
        >
            User Management
        </button>
        <button
            type="button"
            onClick={() => setActiveTab('password')}
            className={`rounded-full px-5 py-2 text-sm font-bold transition ${
            activeTab === 'password'
                ? 'bg-[#EB5979] text-white shadow-lg shadow-[#EB5979]/30'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            }`}
        >
            Change My Password
        </button>
        </div>

        {activeTab === 'password' && <ChangePasswordPage />}
        {activeTab === 'users' && (
        <>
          {error && (
            <div className="mb-5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
              {error}
            </div>
          )}

          {message && (
            <div className="mb-5 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm font-medium text-green-700">
              {message}
            </div>
          )}

          {lastSetupLink && (
            <div className="mb-5 rounded-xl border border-yellow-200 bg-yellow-50 px-4 py-3 text-sm text-yellow-800">
              <p className="font-bold">Setup link</p>
              <p className="mt-1 break-all">{lastSetupLink}</p>
              <p className="mt-2">
                Copy this link and send it manually if email is not configured.
              </p>
            </div>
          )}

          <form
            onSubmit={handleCreateUser}
            className="mb-8 grid gap-4 rounded-2xl bg-white p-5 shadow-sm lg:grid-cols-2"
          >
            <div>
              <label className="mb-2 block text-sm font-semibold">Full Name</label>
              <input
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                className="form-input"
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-semibold">Username</label>
                <input
                name="username"
                value={formData.username}
                onChange={handleChange}
                className="form-input"
                disabled={Boolean(editingUserId)}
                required
                />
            </div>

            <div className="lg:col-span-2">
              <label className="mb-2 block text-sm font-semibold">Email</label>
              <input
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                className="form-input"
                required
              />
            </div>

            <div className="flex items-center gap-6 lg:col-span-2">
              <label className="flex items-center gap-2 text-sm font-semibold">
                <input
                  type="checkbox"
                  name="accessWeb"
                  checked={formData.accessWeb}
                  onChange={handleChange}
                />
                Web App Access
              </label>

              <label className="flex items-center gap-2 text-sm font-semibold">
                <input
                  type="checkbox"
                  name="accessMobile"
                  checked={formData.accessMobile}
                  onChange={handleChange}
                />
                Mobile App Access
              </label>
            </div>
                <div className="flex flex-wrap gap-3 lg:col-span-2">
                <button type="submit" className="primary-button" disabled={isLoading}>
                    {isLoading
                    ? editingUserId
                        ? 'Updating User...'
                        : 'Creating User...'
                    : editingUserId
                        ? 'Update User'
                        : 'Create User'}
                </button>

                {editingUserId && (
                    <button
                    type="button"
                    onClick={handleCancelEdit}
                    className="rounded-full border border-gray-200 bg-white px-5 py-2 text-sm font-bold text-gray-700 transition hover:bg-gray-100"
                    >
                    Cancel Edit
                    </button>
                )}
                </div>
          </form>

          <div className="overflow-x-auto rounded-2xl bg-white shadow-sm">
            <table className="min-w-full text-left text-sm">
              <thead className="border-b bg-gray-50 text-gray-600">
                <tr>
                  <th className="px-4 py-3">User</th>
                  <th className="px-4 py-3">Role</th>
                  <th className="px-4 py-3">Web</th>
                  <th className="px-4 py-3">Mobile</th>
                  <th className="px-4 py-3">Active</th>
                  <th className="px-4 py-3">Confirmed</th>
                  <th className="px-4 py-3">Actions</th>
                </tr>
              </thead>

              <tbody>
                {users.map((user) => (
                  <tr key={user.id} className="border-b">
                    <td className="px-4 py-3">
                      <p className="font-bold">{user.full_name || user.username}</p>
                      <p className="text-gray-500">{user.email}</p>
                      <p className="text-xs text-gray-400">@{user.username}</p>
                    </td>

                    <td className="px-4 py-3">{user.role}</td>

                    <td className="px-4 py-3">
                      <input
                        type="checkbox"
                        checked={Boolean(user.access_web)}
                        disabled={user.role === 'admin'}
                        onChange={(event) =>
                          updateAccess(user, { accessWeb: event.target.checked })
                        }
                      />
                    </td>

                    <td className="px-4 py-3">
                      <input
                        type="checkbox"
                        checked={Boolean(user.access_mobile)}
                        disabled={user.role === 'admin'}
                        onChange={(event) =>
                          updateAccess(user, { accessMobile: event.target.checked })
                        }
                      />
                    </td>

                    <td className="px-4 py-3">
                      <input
                        type="checkbox"
                        checked={Boolean(user.is_active)}
                        disabled={user.role === 'admin'}
                        onChange={(event) =>
                          updateAccess(user, { isActive: event.target.checked })
                        }
                      />
                    </td>

                    <td className="px-4 py-3">
                      {user.confirmed_at ? 'Yes' : 'Pending'}
                    </td>
                    <td className="px-4 py-3">
                        {user.role !== 'admin' ? (
                            <div className="flex flex-wrap gap-2">
                            <button
                                type="button"
                                onClick={() => handleEditUser(user)}
                                className="rounded-full bg-blue-50 px-3 py-1 text-xs font-bold text-blue-700 hover:bg-blue-100"
                            >
                                Edit
                            </button>

                            <button
                                type="button"
                                onClick={() => handleDeleteUser(user)}
                                className="rounded-full bg-red-50 px-3 py-1 text-xs font-bold text-red-700 hover:bg-red-100"
                            >
                                Delete
                            </button>
                            </div>
                        ) : (
                            <span className="text-xs text-gray-400">Protected</span>
                        )}
                        </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </section>
  )
}

export default UserManagementPage