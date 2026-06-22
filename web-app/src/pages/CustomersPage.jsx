import { useEffect, useState } from 'react'
import { customerApi } from '../services/api'

const emptyForm = {
  name: '',
  phone: '',
  email: '',
  address: '',
}

function CustomersPage() {
  const [customers, setCustomers] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingCustomerId, setEditingCustomerId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    loadCustomers()
  }, [])

  async function loadCustomers() {
    try {
      setLoading(true)
      setError('')
      const data = await customerApi.getAll()
      setCustomers(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

function handleChange(event) {
  const { name, value } = event.target

  if (name === 'phone') {
    const digitsOnly = value.replace(/\D/g, '').slice(0, 10)

    setForm((current) => ({
      ...current,
      phone: digitsOnly,
    }))

    return
  }

  setForm((current) => ({
    ...current,
    [name]: value,
  }))
}

  function resetForm() {
    setForm(emptyForm)
    setEditingCustomerId(null)
  }

  function handleEdit(customer) {
    setEditingCustomerId(customer.id)
    setForm({
      name: customer.name || '',
      phone: customer.phone || '',
      email: customer.email || '',
      address: customer.address || '',
    })
    setMessage('')
    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (!form.name.trim()) {
      setError('Customer name is required.')
      return
    }

        if (form.phone && !/^\d{10}$/.test(form.phone)) {
    setError('Phone number must contain exactly 10 digits.')
    return
    }

    try {
      setSaving(true)
      setError('')
      setMessage('')

      if (editingCustomerId) {
        await customerApi.update(editingCustomerId, form)
        setMessage('Customer updated successfully.')
      } else {
        await customerApi.create(form)
        setMessage('Customer created successfully.')
      }

      resetForm()
      await loadCustomers()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(customer) {
    const confirmed = window.confirm(
      `Are you sure you want to delete ${customer.name}?`
    )

    if (!confirmed) {
      return
    }

    try {
      setError('')
      setMessage('')
      await customerApi.remove(customer.id)
      setMessage('Customer deleted successfully.')
      await loadCustomers()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Customers</h1>
        <p className="text-gray-600">
          Manage customer master data used by the mobile app.
        </p>
      </div>

      {error && (
        <div className="mb-4 rounded-lg bg-red-50 p-4 text-red-700">
          {error}
        </div>
      )}

      {message && (
        <div className="mb-4 rounded-lg bg-green-50 p-4 text-green-700">
          {message}
        </div>
      )}

      <div className="grid gap-6 xl:grid-cols-[380px_1fr]">
        <div className="app-card p-6">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">
            {editingCustomerId ? 'Edit Customer' : 'Add Customer'}
          </h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <FormField
              label="Customer Name"
              name="name"
              value={form.name}
              onChange={handleChange}
              required
            />

            <FormField
            label="Phone"
            name="phone"
            value={form.phone}
            onChange={handleChange}
            inputMode="numeric"
            maxLength={10}
            placeholder="0771234567"
            />

            <FormField
              label="Email"
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
            />

            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">
                Address
              </label>
              <textarea
                name="address"
                value={form.address}
                onChange={handleChange}
                rows="3"
                className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
              />
            </div>

            <div className="flex gap-3">
              <button
                type="submit"
                disabled={saving}
                className="rounded-lg bg-[#EB5979] px-4 py-2 font-medium text-white hover:bg-[#D94368] disabled:opacity-60"
              >
                {saving
                  ? 'Saving...'
                  : editingCustomerId
                    ? 'Update'
                    : 'Save'}
              </button>

              {editingCustomerId && (
                <button
                  type="button"
                  onClick={resetForm}
                  className="rounded-lg border border-gray-300 px-4 py-2 font-medium text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>

        <div className="app-card p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">
              Customer List
            </h2>
            <button
              type="button"
              onClick={loadCustomers}
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Refresh
            </button>
          </div>

          {loading ? (
            <p>Loading customers...</p>
          ) : customers.length === 0 ? (
            <p className="text-gray-500">No customers found.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-left text-sm">
                <thead>
                  <tr className="border-b bg-gray-50">
                    <TableHeader>Name</TableHeader>
                    <TableHeader>Phone</TableHeader>
                    <TableHeader>Email</TableHeader>
                    <TableHeader>Address</TableHeader>
                    <TableHeader>Actions</TableHeader>
                  </tr>
                </thead>
                <tbody>
                  {customers.map((customer) => (
                    <tr key={customer.id} className="border-b">
                      <TableCell>{customer.name}</TableCell>
                      <TableCell>{customer.phone || '-'}</TableCell>
                      <TableCell>{customer.email || '-'}</TableCell>
                      <TableCell>{customer.address || '-'}</TableCell>
                      <TableCell>
                        <div className="flex gap-2">
                          <button
                            type="button"
                            onClick={() => handleEdit(customer)}
                            className="rounded bg-amber-100 px-3 py-1 text-amber-700 hover:bg-amber-200"
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDelete(customer)}
                            className="rounded bg-red-100 px-3 py-1 text-red-700 hover:bg-red-200"
                          >
                            Delete
                          </button>
                        </div>
                      </TableCell>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function FormField({
  label,
  name,
  value,
  onChange,
  type = 'text',
  required = false,
  inputMode,
  maxLength,
  placeholder,
}) {
  return (
    <div>
      <label className="mb-1 block text-sm font-medium text-gray-700">
        {label}
      </label>
        <input
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        required={required}
        inputMode={inputMode}
        maxLength={maxLength}
        placeholder={placeholder}
        className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none focus:border-blue-500"
        />
    </div>
  )
}

function TableHeader({ children }) {
  return (
    <th className="whitespace-nowrap px-4 py-3 font-semibold text-gray-700">
      {children}
    </th>
  )
}

function TableCell({ children }) {
  return <td className="px-4 py-3 text-gray-700">{children}</td>
}

export default CustomersPage