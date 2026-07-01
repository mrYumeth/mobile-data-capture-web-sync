import { useState } from 'react'
import { authApi } from '../services/api'

function ChangePasswordPage() {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setMessage('')

    if (newPassword.length < 6) {
      setError('New password must contain at least 6 characters.')
      return
    }

    if (newPassword !== confirmPassword) {
      setError('New passwords do not match.')
      return
    }

    try {
      setIsLoading(true)

      await authApi.changePassword({
        currentPassword,
        newPassword,
      })

      setMessage('Password changed successfully. Please use your new password next time you login.')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (error) {
      setError(error.message || 'Password change failed.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <section className="rounded-2xl bg-white p-6 shadow-sm">
      <div className="mb-6">
        <p className="text-sm font-bold uppercase tracking-[0.25em] text-[#EB5979]">
        Account Security
        </p>
        <h3 className="mt-2 text-2xl font-extrabold">Change My Password</h3>
        <p className="mt-2 text-gray-500">
        Update the password of your currently logged-in FieldSync account.
        </p>
      </div>

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

      <form onSubmit={handleSubmit} className="max-w-xl space-y-5">
        <div>
          <label className="mb-2 block text-sm font-semibold text-gray-700">
            Current Password
          </label>
          <input
            type="password"
            value={currentPassword}
            onChange={(event) => setCurrentPassword(event.target.value)}
            placeholder="Enter current password"
            className="form-input"
            autoComplete="current-password"
            required
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-semibold text-gray-700">
            New Password
          </label>
          <input
            type="password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            placeholder="Enter new password"
            className="form-input"
            autoComplete="new-password"
            required
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-semibold text-gray-700">
            Confirm New Password
          </label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            placeholder="Confirm new password"
            className="form-input"
            autoComplete="new-password"
            required
          />
        </div>

        <button type="submit" className="primary-button" disabled={isLoading}>
          {isLoading ? 'Changing Password...' : 'Change Password'}
        </button>
      </form>
    </section>
  )
}

export default ChangePasswordPage