import { useEffect, useState } from 'react'
import { authApi } from '../services/api'
import {
  getKeycloakToken,
  initKeycloak,
  loginWithKeycloak,
} from '../services/keycloakService'

function LoginPage({
  onLogin,
  onShowRegister,
  theme,
  toggleTheme,
}) {
  const [error, setError] = useState('')

  useEffect(() => {
    async function completeKeycloakLogin() {
      try {
        const isAuthenticated =
          await initKeycloak()

        if (!isAuthenticated) {
          return
        }

        const token =
          getKeycloakToken()

        if (!token) {
          return
        }

        localStorage.setItem(
          'fieldsync-auth-token',
          token
        )

        localStorage.setItem(
          'fieldsync-admin-auth',
          'true'
        )

        const user =
          await authApi.me()

        onLogin(
          user.user
        )
      } catch (error) {
        setError(
          error.message ||
            'Keycloak login failed.'
        )
      }
    }

    completeKeycloakLogin()
  }, [onLogin])

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

      <div className="grid w-full max-w-5xl overflow-hidden rounded-[32px] border border-white/20 bg-white/80 shadow-[0_30px_90px_rgba(0,0,0,0.22)] backdrop-blur-xl lg:grid-cols-[1fr_420px]">
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
                    Admin Web Console
                  </p>
                </div>
              </div>

              <h2 className="max-w-md text-4xl font-extrabold leading-tight">
                Offline mobile data capture and web synchronization.
              </h2>

              <p className="mt-5 max-w-md text-lg text-gray-300">
                Manage master data, view uploaded field records, and monitor
                mobile sync activity from one clean dashboard.
              </p>
            </div>
          </div>
        </div>

        <div className="bg-[#F1F1F3] p-8 text-[#111827] sm:p-10">
          <div className="mb-8 text-center lg:hidden">
            <img
              src="/logo.png"
              alt="FieldSync Logo"
              className="mx-auto h-20 w-20 rounded-full object-contain"
            />

            <h1 className="mt-4 text-3xl font-extrabold">
              Field
              <span className="text-[#EB5979]">
                Sync
              </span>
            </h1>

            <p className="text-gray-600">
              Admin Web Console
            </p>
          </div>

          <div>
            <p className="text-sm font-bold uppercase tracking-[0.25em] text-[#EB5979]">
              Admin Login
            </p>

            <h2 className="mt-3 text-3xl font-extrabold text-gray-950">
              Welcome back
            </h2>

            <p className="mt-2 text-gray-600">
              Sign in to access the FieldSync dashboard.
            </p>
          </div>

          {error && (
            <div className="mt-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
              {error}
            </div>
          )}

          <div className="mt-8 space-y-5">
            <button
              type="button"
              onClick={() =>
                loginWithKeycloak()
              }
              className="primary-button w-full"
            >
              Login
            </button>

            <p className="text-center text-sm text-gray-500">
              You will be redirected to the FieldSync IAM login page.
            </p>
          </div>

          <div className="mt-6 rounded-2xl bg-white p-4 text-sm text-gray-600">
            <p className="font-semibold text-gray-800">
              Secure access only
            </p>

            <p className="mt-1">
              User accounts are created by the administrator. Please use the
              username provided by your admin.
            </p>
          </div>

          <div className="mt-4 rounded-2xl bg-white p-4 text-sm text-gray-600">
            <p className="font-semibold text-gray-800">
              New company?
            </p>

            <p className="mt-1">
              Register your company to create a new tenant workspace and first
              admin account.
            </p>

            <button
              type="button"
              onClick={onShowRegister}
              className="mt-3 font-semibold text-[#EB5979] hover:underline"
            >
              Register Company
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default LoginPage