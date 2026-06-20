import { useState } from 'react'
import DashboardPage from './pages/DashboardPage'
import CustomersPage from './pages/CustomersPage'
import LocationsPage from './pages/LocationsPage';
import CategoriesPage from './pages/CategoriesPage';

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
        return <LocationsPage />
      case 'categories':
        return <CategoriesPage />
      case 'capturedRecords':
        return <PlaceholderPage title="Captured Records" />
      default:
        return <DashboardPage />
    }
  }

  return (
    <div className="min-h-screen bg-[#F8FAFC]">
      <aside className="fixed inset-y-0 left-0 hidden w-64 bg-[#0F172A] text-white lg:block">
        <div className="border-b border-gray-800 p-6">
          <h1 className="text-xl font-bold">FieldSync</h1>
            <p className="mt-1 text-sm text-slate-400">Web Sync Console</p>
        </div>

        <nav className="space-y-1 p-4">
          {navigation.map((item) => (
            <button
              key={item.key}
              type="button"
              onClick={() => setActivePage(item.key)}
              className={`w-full rounded-lg px-4 py-3 text-left text-sm font-medium transition ${
                activePage === item.key
                  ? 'bg-[#2563EB] text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`}
            >
              {item.label}
            </button>
          ))}
        </nav>
      </aside>

      <div className="lg:pl-64">
        <header className="sticky top-0 z-10 border-b border-[#E5E7EB] bg-white px-4 py-4 shadow-sm lg:px-8">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">
                FieldSync Data Capture Platform
              </h2>
              <p className="text-sm text-gray-500">
                Offline mobile data capture and cloud synchronization console
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
                      ? 'bg-[#2563EB] text-white'
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

      <div className="app-card p-6">
        Coming soon
      </div>
    </div>
  )
}

export default App