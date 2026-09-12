import { useState } from 'react'
import { authApi } from '../services/api'

function RegisterTenantPage({
  onRegisterSuccess,
  onBackToLogin,
  theme,
  toggleTheme,
}) {
  const [tenantName, setTenantName] = useState('')
  const [tenantSlug, setTenantSlug] = useState('')
  const [fullName, setFullName] = useState('')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [temporaryPassword, setTemporaryPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const isKeycloakAuth =
    (import.meta.env.VITE_AUTH_PROVIDER || 'keycloak') === 'keycloak'

  const isDark = theme === 'dark'

  const rightPanelStyle = {
    backgroundColor: isDark ? '#27272A' : '#F1F1F3',
    color: isDark ? '#F4F4F5' : '#111827',
  }

  const headingStyle = {
    color: isDark ? '#FAFAFA' : '#030712',
  }

  const mutedTextStyle = {
    color: isDark ? '#A1A1AA' : '#4B5563',
  }

  const labelStyle = {
    color: isDark ? '#E4E4E7' : '#374151',
  }

  const helperStyle = {
    color: isDark ? '#A1A1AA' : '#6B7280',
  }

  const inputStyle = {
    backgroundColor: isDark ? '#18181B' : '#FFFFFF',
    color: isDark ? '#F4F4F5' : '#111111',
    borderColor: isDark
      ? 'rgba(255, 255, 255, 0.12)'
      : '#D1D5DB',
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')

    setSuccessMessage('')
    setTemporaryPassword('')

    if (!isKeycloakAuth && password !== confirmPassword) {
      setError('Password and confirm password do not match.')
      return
    }

    setIsSubmitting(true)

    try {
      const registrationPayload = {
        tenantName: tenantName.trim(),
        tenantSlug: tenantSlug.trim() || undefined,
        fullName: fullName.trim(),
        username: username.trim(),
        email: email.trim(),
      }

      if (!isKeycloakAuth) {
        registrationPayload.password = password
      }

      const result =
        await authApi.registerTenant(registrationPayload)

      if (isKeycloakAuth) {
        setSuccessMessage(
          result.message ||
            'Company registered successfully.'
        )

        setTemporaryPassword(
          result.keycloakTemporaryPassword || ''
        )

        return
      }

      localStorage.setItem(
        'fieldsync-auth-token',
        result.token
      )

      localStorage.setItem(
        'fieldsync-admin-auth',
        'true'
      )

      onRegisterSuccess(result.user)
    } catch (error) {
      setError(
        error.message ||
          'Company registration failed.'
      )
    } finally {
      setIsSubmitting(false)
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

      <div
        className="grid w-full max-w-6xl overflow-hidden rounded-[32px] border shadow-[0_30px_90px_rgba(0,0,0,0.22)] backdrop-blur-xl lg:grid-cols-[1fr_520px]"
        style={{
          borderColor: isDark
            ? 'rgba(255,255,255,0.10)'
            : 'rgba(255,255,255,0.20)',
          backgroundColor: isDark
            ? '#27272A'
            : 'rgba(255,255,255,0.80)',
        }}
      >
        <div className="relative hidden bg-[#2A2B32] p-10 text-white lg:block">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(235,89,121,0.35),transparent_35%)]" />

          <div className="relative z-10 flex h-full flex-col justify-between">
            <div>
              <div className="mb-8 flex items-center gap-4">
                <img
                  src="/logo.png"
                  alt="FieldSync Logo"
                  className="h-16 w-16 rounded-full bg-white object-contain p-1"
                />

                <div>
                  <h1 className="text-3xl font-extrabold">
                    Field
                    <span className="text-[#EB5979]">
                      Sync
                    </span>
                  </h1>

                  <p className="text-gray-300">
                    Company Registration
                  </p>
                </div>
              </div>

              <h2 className="max-w-md text-4xl font-extrabold leading-tight">
                Create a secure workspace for your field data team.
              </h2>

              <p className="mt-5 max-w-md text-lg text-gray-300">
                Register your company, create the first admin account, and
                manage users, customers, locations, categories, and captured
                records under one isolated tenant.
              </p>
            </div>

            <div className="rounded-2xl border border-white/10 bg-white/10 p-5 backdrop-blur">
              <p className="text-sm text-gray-300">
                Multi-tenant access
              </p>

              <p className="mt-2 font-semibold">
                Each company receives its own tenant workspace.
              </p>
            </div>
          </div>
        </div>

        <div
          className="p-8 sm:p-10"
          style={rightPanelStyle}
        >
          <div>
            <p className="text-sm font-bold uppercase tracking-[0.25em] text-[#EB5979]">
              Register Company
            </p>

            <h2
              className="mt-3 text-3xl font-extrabold"
              style={headingStyle}
            >
              Create your tenant
            </h2>

            <p
              className="mt-2"
              style={mutedTextStyle}
            >
              This will create a new company workspace and the first admin
              user.
            </p>
          </div>

          {error && (
            <div
              className="mt-6 rounded-md border px-4 py-3 text-sm font-medium"
              style={{
                backgroundColor: isDark
                  ? 'rgba(69, 10, 10, 0.45)'
                  : '#FEF2F2',
                borderColor: isDark
                  ? 'rgba(127, 29, 29, 0.7)'
                  : '#FECACA',
                color: isDark
                  ? '#FCA5A5'
                  : '#B91C1C',
              }}
            >
              {error}
            </div>
          )}

          {successMessage && (
            <div
              className="mt-6 rounded-md border px-4 py-3 text-sm font-medium"
              style={{
                backgroundColor: isDark
                  ? 'rgba(5, 46, 22, 0.45)'
                  : '#F0FDF4',
                borderColor: isDark
                  ? 'rgba(20, 83, 45, 0.7)'
                  : '#BBF7D0',
                color: isDark
                  ? '#86EFAC'
                  : '#15803D',
              }}
            >
              {successMessage}
            </div>
          )}

          {temporaryPassword && (
            <div
              className="mt-6 rounded-md border px-4 py-3 text-sm"
              style={{
                backgroundColor: isDark
                  ? 'rgba(66, 32, 6, 0.45)'
                  : '#FEFCE8',
                borderColor: isDark
                  ? 'rgba(113, 63, 18, 0.7)'
                  : '#FEF08A',
                color: isDark
                  ? '#FDE68A'
                  : '#854D0E',
              }}
            >
              <p className="font-bold">
                Temporary Keycloak Password
              </p>

              <p className="mt-1 break-all text-base">
                {temporaryPassword}
              </p>

              <p className="mt-2">
                Use this password to log in through Keycloak. You will be
                asked to create a new password after first login.
              </p>
            </div>
          )}

          <form
            onSubmit={handleSubmit}
            className="mt-8 space-y-5"
          >
            <div>
              <label
                className="mb-2 block text-sm font-semibold"
                style={labelStyle}
              >
                Company Name
              </label>

              <input
                type="text"
                value={tenantName}
                onChange={(event) =>
                  setTenantName(event.target.value)
                }
                placeholder="Example: ABC Company"
                className="form-input"
                style={inputStyle}
                required
              />
            </div>

            <div>
              <label
                className="mb-2 block text-sm font-semibold"
                style={labelStyle}
              >
                Company Slug
              </label>

              <input
                type="text"
                value={tenantSlug}
                onChange={(event) =>
                  setTenantSlug(event.target.value)
                }
                placeholder="Example: abc-company"
                className="form-input"
                style={inputStyle}
              />

              <p
                className="mt-1 text-xs"
                style={helperStyle}
              >
                Optional. If left empty, it will be generated from the company
                name.
              </p>
            </div>

            <div>
              <label
                className="mb-2 block text-sm font-semibold"
                style={labelStyle}
              >
                Admin Full Name
              </label>

              <input
                type="text"
                value={fullName}
                onChange={(event) =>
                  setFullName(event.target.value)
                }
                placeholder="Enter admin full name"
                className="form-input"
                style={inputStyle}
                required
              />
            </div>

            <div>
              <label
                className="mb-2 block text-sm font-semibold"
                style={labelStyle}
              >
                Admin Username
              </label>

              <input
                type="text"
                value={username}
                onChange={(event) =>
                  setUsername(event.target.value)
                }
                placeholder="Enter admin username"
                className="form-input"
                style={inputStyle}
                autoComplete="username"
                required
              />
            </div>

            <div>
              <label
                className="mb-2 block text-sm font-semibold"
                style={labelStyle}
              >
                Admin Email
              </label>

              <input
                type="email"
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                placeholder="Enter admin email"
                className="form-input"
                style={inputStyle}
                autoComplete="email"
                required
              />
            </div>

            {!isKeycloakAuth && (
              <>
                <div>
                  <label
                    className="mb-2 block text-sm font-semibold"
                    style={labelStyle}
                  >
                    Password
                  </label>

                  <input
                    type="password"
                    value={password}
                    onChange={(event) =>
                      setPassword(event.target.value)
                    }
                    placeholder="Enter password"
                    className="form-input"
                    style={inputStyle}
                    autoComplete="new-password"
                    required
                  />
                </div>

                <div>
                  <label
                    className="mb-2 block text-sm font-semibold"
                    style={labelStyle}
                  >
                    Confirm Password
                  </label>

                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={(event) =>
                      setConfirmPassword(event.target.value)
                    }
                    placeholder="Confirm password"
                    className="form-input"
                    style={inputStyle}
                    autoComplete="new-password"
                    required
                  />
                </div>
              </>
            )}

            <button
              type="submit"
              className="primary-button w-full"
              disabled={isSubmitting}
            >
              {isSubmitting
                ? 'Creating Company...'
                : 'Register Company'}
            </button>
          </form>

          <div
            className="mt-6 text-center text-sm"
            style={mutedTextStyle}
          >
            Already have a company account?{' '}

            <button
              type="button"
              onClick={onBackToLogin}
              className="font-semibold text-[#EB5979] hover:underline"
            >
              Back to Login
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default RegisterTenantPage