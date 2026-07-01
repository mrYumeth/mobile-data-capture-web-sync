import { useState } from 'react'
import { authApi } from '../services/api'

function RegisterPage({ onRegister, onBackToLogin, theme, toggleTheme }) {
  const [fullName, setFullName] = useState('')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    try {
      setIsLoading(true)

      const result = await authApi.register({
        fullName: fullName.trim(),
        username: username.trim(),
        email: email.trim() || null,
        password,
      })

      localStorage.setItem('fieldsync-auth-token', result.token)
      localStorage.setItem('fieldsync-admin-auth', 'true')
      localStorage.setItem('fieldsync-auth-user', JSON.stringify(result.user))

      onRegister(result.user)
    } catch (error) {
      setError(error.message || 'Registration failed.')
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
            Create FieldSync Account
          </h1>
          <p className="mt-2 text-gray-600">
            Register as a normal user for mobile/web access.
          </p>
        </div>

        {error && (
          <div className="mb-5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="mb-2 block text-sm font-semibold text-gray-700">
              Full Name
            </label>
            <input
              type="text"
              value={fullName}
              onChange={(event) => setFullName(event.target.value)}
              placeholder="Enter full name"
              className="form-input"
              required
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-gray-700">
              Username
            </label>
            <input
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="Enter username"
              className="form-input"
              autoComplete="username"
              required
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-gray-700">
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="Enter email address"
              className="form-input"
              autoComplete="email"
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-gray-700">
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Enter password"
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
              placeholder="Confirm password"
              className="form-input"
              autoComplete="new-password"
              required
            />
          </div>

          <button type="submit" className="primary-button w-full" disabled={isLoading}>
            {isLoading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <button
          type="button"
          onClick={onBackToLogin}
          className="mt-5 w-full text-sm font-semibold text-[#EB5979]"
        >
          Already have an account? Login
        </button>

        <div className="mt-6 rounded-2xl bg-white p-4 text-sm text-gray-600">
          <p className="font-semibold text-gray-800">Security note</p>
          <p className="mt-1">
            Public registration creates normal users only. Admin users are managed separately.
          </p>
        </div>
      </div>
    </div>
  )
}

export default RegisterPage