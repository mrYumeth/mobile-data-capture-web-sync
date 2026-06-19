import { useState } from 'react'
import DashboardPage from './pages/DashboardPage'
import CustomersPage from './pages/CustomersPage'

const navigation = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'customers', label: 'Customers' },
  { key: 'locations', label: 'Locations' },
  { key: 'categories', label: 'Categories' },
  { key: 'capturedRecords', label: 'Captured Records' },
]

function App() {
  const [activePage, setActivePage] = useState('dashboard')

  function renderPage() {
    switch (activePage) {
      case 'customers':
        return <CustomersPage />
      case 'locations':
        return <PlaceholderPage title="Location Master" />
      case 'categories':
        return <PlaceholderPage title="Category Master" />
      case 'capturedRecords':
        return <PlaceholderPage title="Captured Records" />
      default:
        return <DashboardPage />
    }
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <aside className="fixed inset-y-0 left-0 hidden w-64 bg-gray-900 text-white lg:block">
        <div className="border-b border-gray-800 p-6">
          <h1 className="text-xl font-bold">Data Capture</h1>
          <p className="mt-1 text-sm text-gray-400">Web Sync Console</p>
        </div>

        <nav className="space-y-1 p-4">
          {navigation.map((item) => (
            <button
              key={item.key}
              type="button"
              onClick={() => setActivePage(item.key)}
              className={`w-full rounded-lg px-4 py-3 text-left text-sm font-medium transition ${
                activePage === item.key
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`}
            >
              {item.label}
            </button>
          ))}
        </nav>
      </aside>

      <div className="lg:pl-64">
        <header className="sticky top-0 z-10 border-b bg-white px-4 py-4 shadow-sm lg:px-8">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">
                Mobile Data Capture and Web Sync Prototype
              </h2>
              <p className="text-sm text-gray-500">
                Master data management and captured record monitoring
              </p>
            </div>

            <div className="flex flex-wrap gap-2 lg:hidden">
              {navigation.map((item) => (
                <button
                  key={item.key}
                  type="button"
                  onClick={() => setActivePage(item.key)}
                  className={`rounded-lg px-3 py-2 text-sm font-medium ${
                    activePage === item.key
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-700'
                  }`}
                >
                  {item.label}
                </button>
              ))}
            </div>
          </div>
        </header>

        <main className="p-4 lg:p-8">{renderPage()}</main>
      </div>
    </div>
  )
}

function PlaceholderPage({ title }) {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
        <p className="text-gray-600">
          This screen will be implemented next.
        </p>
      </div>

      <div className="rounded-xl bg-white p-6 shadow-sm">
        Coming soon
      </div>
    </div>
  )
}

export default App