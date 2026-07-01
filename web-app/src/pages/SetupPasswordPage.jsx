import { useState } from 'react'
import { authApi } from '../services/api'

function SetupPasswordPage({ setupToken, onBackToLogin, theme, toggleTheme }) {
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setMessage('')

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    try {
      setIsLoading(true)

      await authApi.setupPassword({
        token: setupToken,
        password,
      })

      setMessage('Password set successfully. You can now login.')
    } catch (error) {
      setError(error.message || 'Password setup failed.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-8">
      <button
        type="button"
        onClick={toggleTheme}
        className="theme-toggle fixed right-6 top-6 z-30"
        title="Toggle light/dark theme"
      >
        {theme === 'dark' ? '☀' : '☾'}
      </button>

      <div className="w-full max-w-md rounded-[32px] border border-white/20 bg-white/80 p-8 shadow-[0_30px_90px_rgba(0,0,0,0.22)] backdrop-blur-xl">
        <div className="mb-8 text-center">
          <img
            src="/logo.png"
            alt="FieldSync Logo"
            className="mx-auto h-20 w-20 rounded-full object-contain"
          />
          <h1 className="mt-4 text-3xl font-extrabold text-gray-950">
            Set Your Password
          </h1>
          <p className="mt-2 text-gray-600">
            Create a password to activate your FieldSync account.
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

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="mb-2 block text-sm font-semibold text-gray-700">
              New Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Enter new password"
              className="form-input"
              autoComplete="new-password"
              required
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-gray-700">
              Confirm Password
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

          <button type="submit" className="primary-button w-full" disabled={isLoading}>
            {isLoading ? 'Setting Password...' : 'Set Password'}
          </button>
        </form>

        <button
          type="button"
          onClick={onBackToLogin}
          className="mt-5 w-full text-sm font-semibold text-[#EB5979]"
        >
          Back to Login
        </button>
      </div>
    </div>
  )
}

export default SetupPasswordPage