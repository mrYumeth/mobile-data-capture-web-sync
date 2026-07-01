import { useEffect, useState } from 'react'
import DashboardPage from './pages/DashboardPage'
import CustomersPage from './pages/CustomersPage'
import LocationsPage from './pages/LocationsPage'
import CategoriesPage from './pages/CategoriesPage'
import CapturedRecordsPage from './pages/CapturedRecordsPage'
import LoginPage from './pages/LoginPage'
import UserManagementPage from './pages/UserManagementPage'
import SetupPasswordPage from './pages/SetupPasswordPage'

const AUTH_TOKEN_KEY = 'fieldsync-auth-token'
const AUTH_STATE_KEY = 'fieldsync-admin-auth'
const AUTH_USER_KEY = 'fieldsync-auth-user'

const navigation = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'customers', label: 'Customers' },
  { key: 'locations', label: 'Locations' },
  { key: 'categories', label: 'Categories' },
  { key: 'capturedRecords', label: 'Captured Records' },
  { key: 'users', label: 'Users', adminOnly: true },
]

function getStoredUser() {
  const storedUser = localStorage.getItem(AUTH_USER_KEY)

  if (!storedUser) {
    return null
  }

  try {
    return JSON.parse(storedUser)
  } catch {
    localStorage.removeItem(AUTH_USER_KEY)
    return null
  }
}

function App() {
  const [activePage, setActivePage] = useState('dashboard')

  const setupToken = new URLSearchParams(window.location.search).get('setupToken')

  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('fieldsync-theme') || 'light'
  })

  const [currentUser, setCurrentUser] = useState(() => {
    return getStoredUser()
  })

  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return Boolean(localStorage.getItem(AUTH_TOKEN_KEY))
  })

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('fieldsync-theme', theme)
  }, [theme])

  function toggleTheme() {
    setTheme((currentTheme) =>
      currentTheme === 'dark' ? 'light' : 'dark'
    )
  }

  function handleLogin(user) {
    if (user) {
      localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
      setCurrentUser(user)
    }

    localStorage.setItem(AUTH_STATE_KEY, 'true')
    setIsAuthenticated(true)
  }

  function handleLogout() {
    localStorage.removeItem(AUTH_TOKEN_KEY)
    localStorage.removeItem(AUTH_STATE_KEY)
    localStorage.removeItem(AUTH_USER_KEY)

    setCurrentUser(null)
    setIsAuthenticated(false)
    setActivePage('dashboard')
  }

  if (setupToken) {
  return (
    <SetupPasswordPage
      setupToken={setupToken}
      onBackToLogin={() => {
        window.history.replaceState({}, document.title, window.location.pathname)
        setIsAuthenticated(false)
      }}
      theme={theme}
      toggleTheme={toggleTheme}
    />
  )
}

if (!isAuthenticated) {
  return (
    <LoginPage
      onLogin={handleLogin}
      theme={theme}
      toggleTheme={toggleTheme}
    />
  )
}

  function renderPage() {
    switch (activePage) {
      case 'customers':
        return <CustomersPage />
      case 'locations':
        return <LocationsPage />
      case 'categories':
        return <CategoriesPage />
      case 'capturedRecords':
        return <CapturedRecordsPage />
          case 'users':
      return currentUser?.role === 'admin' ? (
        <UserManagementPage />
      ) : (
        <DashboardPage />
      )
      default:
        return <DashboardPage />
    }
  }

  return (
    <div className="min-h-screen px-4 py-4">
      <header className="glass-header fixed left-1/2 top-4 z-50 w-[calc(100%-2rem)] max-w-7xl -translate-x-1/2 rounded-2xl px-5 py-4">
        <div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
          <div className="flex items-center gap-3">
            <img
              src="/logo.png"
              alt="FieldSync Logo"
              className="h-12 w-12 rounded-full object-contain"
            />

            <div>
              <h1 className="text-2xl font-extrabold tracking-tight">
                Field<span className="accent-text">Sync</span>
              </h1>
              <p className="text-sm" style={{ color: 'var(--header-muted)' }}>
                Data Capture Platform
              </p>
            </div>
          </div>

          <nav className="flex flex-wrap items-center gap-2">
            {navigation
              .filter((item) => !item.adminOnly || currentUser?.role === 'admin')
              .map((item) => (
                <button
                  key={item.key}
                  type="button"
                  onClick={() => setActivePage(item.key)}
                  className={`nav-link ${
                    activePage === item.key ? 'nav-link-active' : ''
                  }`}
                >
                  {item.label}
                </button>
              ))}
          </nav>

          <div className="flex items-center gap-3">
            {currentUser && (
              <div className="hidden text-right text-xs xl:block">
                <p className="font-semibold">
                  {currentUser.fullName || currentUser.username}
                </p>
                <p style={{ color: 'var(--header-muted)' }}>
                  {currentUser.role || 'admin'}
                </p>
              </div>
            )}

            <button
              type="button"
              onClick={toggleTheme}
              className="theme-toggle"
              title="Toggle light/dark theme"
            >
              {theme === 'dark' ? '☀' : '☾'}
            </button>

            <button
              type="button"
              onClick={handleLogout}
              className="hidden rounded-full border border-white/10 px-4 py-2 text-sm font-semibold transition hover:bg-white/10 xl:block"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl pb-6 pt-44 xl:pt-32">
        <div className="page-shell min-w-0 overflow-hidden p-4 lg:p-6">
          {renderPage()}
        </div>
      </main>
    </div>
  )
}

export default App