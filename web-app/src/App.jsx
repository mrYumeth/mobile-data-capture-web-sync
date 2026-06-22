import { useEffect, useState } from 'react'
import DashboardPage from './pages/DashboardPage'
import CustomersPage from './pages/CustomersPage'
import LocationsPage from './pages/LocationsPage'
import CategoriesPage from './pages/CategoriesPage'
import CapturedRecordsPage from './pages/CapturedRecordsPage'
import LoginPage from './pages/LoginPage'

const navigation = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'customers', label: 'Customers' },
  { key: 'locations', label: 'Locations' },
  { key: 'categories', label: 'Categories' },
  { key: 'capturedRecords', label: 'Captured Records' },
]

function App() {
  const [activePage, setActivePage] = useState('dashboard')
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('fieldsync-theme') || 'light'
  })

  const [isAuthenticated, setIsAuthenticated] = useState(() => {
  return localStorage.getItem('fieldsync-admin-auth') === 'true'
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

  function handleLogin() {
  setIsAuthenticated(true)
}

function handleLogout() {
  localStorage.removeItem('fieldsync-admin-auth')
  setIsAuthenticated(false)
  setActivePage('dashboard')
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
            {navigation.map((item) => (
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
                className="hidden rounded-full border border-white/10 px-4 py-2 text-sm font-semibold transition hover:bg-white/10 xl:block"              >
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