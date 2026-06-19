import { useEffect, useState } from 'react'
import { dashboardApi } from '../services/api'

function DashboardPage() {
  const [summary, setSummary] = useState({
    customers: 0,
    locations: 0,
    categories: 0,
    capturedRecords: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadSummary()
  }, [])

  async function loadSummary() {
    try {
      setLoading(true)
      setError('')
      const data = await dashboardApi.getSummary()
      setSummary(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="rounded-xl bg-white p-6 shadow-sm">
        Loading dashboard summary...
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600">
          Overview of master data and uploaded field records.
        </p>
      </div>

      {error && (
        <div className="mb-4 rounded-lg bg-red-50 p-4 text-red-700">
          {error}
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <SummaryCard
          title="Customers"
          value={summary.customers}
          description="Active customer records"
        />
        <SummaryCard
          title="Locations"
          value={summary.locations}
          description="Available field locations"
        />
        <SummaryCard
          title="Categories"
          value={summary.categories}
          description="Capture categories"
        />
        <SummaryCard
          title="Captured Records"
          value={summary.capturedRecords}
          description="Uploaded mobile records"
        />
      </div>
    </div>
  )
}

function SummaryCard({ title, value, description }) {
  return (
    <div className="rounded-xl bg-white p-6 shadow-sm">
      <p className="text-sm font-medium text-gray-500">{title}</p>
      <p className="mt-3 text-3xl font-bold text-gray-900">{value}</p>
      <p className="mt-2 text-sm text-gray-500">{description}</p>
    </div>
  )
}

export default DashboardPage