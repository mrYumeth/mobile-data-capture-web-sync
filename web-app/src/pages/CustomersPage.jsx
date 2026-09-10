import { useEffect, useState } from 'react'
import {
  RefreshCw,
  UserRound,
} from 'lucide-react'

import { customerApi } from '../services/api'

import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/components/ui/alert'

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

import { Button } from '@/components/ui/button'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

import { Textarea } from '@/components/ui/textarea'

import { MoreHorizontal } from 'lucide-react'

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

const FRONTEND_ONLY =
  import.meta.env.VITE_FRONTEND_ONLY === 'true'

const emptyForm = {
  name: '',
  phone: '',
  email: '',
  address: '',
}

const MOCK_CUSTOMERS = [
  {
    id: 'mock-customer-1',
    name: 'Lanka Retail Solutions',
    phone: '0771234567',
    email: 'operations@lankaretail.lk',
    address: 'Colombo 03, Western Province',
  },
  {
    id: 'mock-customer-2',
    name: 'Southern Distribution',
    phone: '0714567890',
    email: 'contact@southerndistribution.lk',
    address: 'Galle, Southern Province',
  },
  {
    id: 'mock-customer-3',
    name: 'Kandy Field Services',
    phone: '0769876543',
    email: 'team@kandyfield.lk',
    address: 'Kandy, Central Province',
  },
  {
    id: 'mock-customer-4',
    name: 'Northline Traders',
    phone: '0753456789',
    email: '',
    address: 'Jaffna, Northern Province',
  },
  {
    id: 'mock-customer-5',
    name: 'Eastern Agro Network',
    phone: '',
    email: 'admin@easternagro.lk',
    address: 'Batticaloa, Eastern Province',
  },
]

function CustomersPage() {
  const [customers, setCustomers] = useState(() =>
    FRONTEND_ONLY ? MOCK_CUSTOMERS : []
  )

  const [form, setForm] = useState(emptyForm)
  const [editingCustomerId, setEditingCustomerId] = useState(null)
  const [loading, setLoading] = useState(!FRONTEND_ONLY)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const [searchQuery, setSearchQuery] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)

  useEffect(() => {
    if (FRONTEND_ONLY) {
      return undefined
    }

    let cancelled = false

    customerApi
      .getAll()
      .then((data) => {
        if (!cancelled) {
          setCustomers(data)
          setError('')
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message || 'Failed to load customers.')
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  function handleChange(event) {
    const { name, value } = event.target

    if (name === 'phone') {
      const digitsOnly = value
        .replace(/\D/g, '')
        .slice(0, 10)

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

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
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

    if (FRONTEND_ONLY) {
      setError('')
      setMessage('')

      if (editingCustomerId) {
        setCustomers((currentCustomers) =>
          currentCustomers.map((customer) =>
            customer.id === editingCustomerId
              ? {
                  ...customer,
                  ...form,
                  name: form.name.trim(),
                }
              : customer
          )
        )

        setMessage('Preview customer updated successfully.')
      } else {
        const newCustomer = {
          id: `mock-customer-${Date.now()}`,
          ...form,
          name: form.name.trim(),
        }

        setCustomers((currentCustomers) => [
          ...currentCustomers,
          newCustomer,
        ])

        setMessage('Preview customer created successfully.')
      }

      resetForm()
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
      setError(err.message || 'Failed to save customer.')
    } finally {
      setSaving(false)
    }
  }

  async function loadCustomers() {
    if (FRONTEND_ONLY) {
      setMessage('Preview customer list refreshed.')
      setError('')
      return
    }

    try {
      setLoading(true)
      setError('')

      const data = await customerApi.getAll()
      setCustomers(data)
    } catch (err) {
      setError(err.message || 'Failed to load customers.')
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(customer) {
    if (FRONTEND_ONLY) {
      setCustomers((currentCustomers) =>
        currentCustomers.filter(
          (currentCustomer) =>
            currentCustomer.id !== customer.id
        )
      )

      if (editingCustomerId === customer.id) {
        resetForm()
      }

      setError('')
      setMessage('Preview customer deleted successfully.')
      return
    }

    try {
      setError('')
      setMessage('')

      await customerApi.remove(customer.id)

      setMessage('Customer deleted successfully.')
      await loadCustomers()
    } catch (err) {
      setError(err.message || 'Failed to delete customer.')
    }
  }

const filteredCustomers = customers.filter((customer) => {
  const query = searchQuery.trim().toLowerCase()

  if (!query) {
    return true
  }

  return [
    customer.name,
    customer.phone,
    customer.email,
    customer.address,
  ].some((value) =>
    String(value || '')
      .toLowerCase()
      .includes(query)
  )
}) 

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">
          Customers
        </h1>

        <p className="mt-1 text-muted-foreground">
          Manage customer master data used by the mobile app.
        </p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertTitle>Something went wrong</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {message && (
        <Alert className="border-green-200 bg-green-50 text-green-800 dark:border-green-900 dark:bg-green-950/30 dark:text-green-300">
          <AlertTitle>Success</AlertTitle>
          <AlertDescription>{message}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-6 xl:grid-cols-[360px_minmax(0,1fr)]">
        <Card className="h-fit">
          <CardHeader>
            <div className="flex items-center gap-3">
              <div className="flex size-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <UserRound
                  className="size-5"
                  aria-hidden="true"
                />
              </div>

              <div>
                <CardTitle>
                  {editingCustomerId
                    ? 'Edit Customer'
                    : 'Add Customer'}
                </CardTitle>

                <CardDescription>
                  {editingCustomerId
                    ? 'Update the selected customer record.'
                    : 'Create a new customer record.'}
                </CardDescription>
              </div>
            </div>
          </CardHeader>

          <CardContent>
            <form
              onSubmit={handleSubmit}
              className="space-y-5"
            >
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
                placeholder="customer@example.com"
              />

              <div className="space-y-2">
                <Label htmlFor="customer-address">
                  Address
                </Label>

                <Textarea
                  id="customer-address"
                  name="address"
                  value={form.address}
                  onChange={handleChange}
                  rows={4}
                  placeholder="Enter customer address"
                />
              </div>

              <div className="flex flex-wrap gap-3">
                <Button
                  type="submit"
                  disabled={saving}
                >
                  {saving
                    ? 'Saving...'
                    : editingCustomerId
                      ? 'Update Customer'
                      : 'Add Customer'}
                </Button>

                {editingCustomerId && (
                  <Button
                    type="button"
                    variant="outline"
                    onClick={resetForm}
                  >
                    Cancel
                  </Button>
                )}
              </div>
            </form>
          </CardContent>
        </Card>

        <Card className="min-w-0">
          <CardHeader>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <CardTitle>Customer List</CardTitle>

                <CardDescription>
                  {FRONTEND_ONLY
                    ? `${customers.length} preview customers`
                    : 'Customers available to FieldSync'}
                </CardDescription>
              </div>

                          <div className="relative w-full sm:max-w-xs">
              <Input
                type="search"
                placeholder="Search customers..."
                value={searchQuery}
                onChange={(event) =>
                  setSearchQuery(event.target.value)
                }
                aria-label="Search customers"
              />
            </div>

              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={loadCustomers}
                disabled={loading}
              >
                <RefreshCw
                  className={
                    loading
                      ? 'animate-spin'
                      : ''
                  }
                  aria-hidden="true"
                />

                Refresh
              </Button>
            </div>
          </CardHeader>

          <CardContent className="p-0">
            {loading ? (
              <CustomerTableSkeleton />
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Phone</TableHead>
                      <TableHead>Email</TableHead>
                      <TableHead>Address</TableHead>
                      <TableHead className="text-right">
                        Actions
                      </TableHead>
                    </TableRow>
                  </TableHeader>

                  <TableBody>
                    {filteredCustomers.length === 0? (
                      <TableRow>
                      <TableCell
                        colSpan={5}
                        className="h-28 text-center text-muted-foreground"
                      >
                        {searchQuery
                          ? 'No customers match your search.'
                          : 'No customers found.'}
                      </TableCell>
                      </TableRow>
                    ) : (
                      filteredCustomers.map((customer) => (
                        <TableRow key={customer.id}>
                          <TableCell className="font-medium">
                            {customer.name}
                          </TableCell>

                          <TableCell className="whitespace-nowrap">
                            {customer.phone || (
                              <span className="text-muted-foreground">
                                Not provided
                              </span>
                            )}
                          </TableCell>

                          <TableCell>
                            {customer.email || (
                              <span className="text-muted-foreground">
                                Not provided
                              </span>
                            )}
                          </TableCell>

                          <TableCell className="max-w-[240px]">
                            <span className="line-clamp-2">
                              {customer.address || (
                                <span className="text-muted-foreground">
                                  Not provided
                                </span>
                              )}
                            </span>
                          </TableCell>

                                        <TableCell>
                <div className="flex justify-end">
                  <DropdownMenu>
                    <DropdownMenuTrigger
                      render={
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon"
                          aria-label={`Actions for ${customer.name}`}
                        />
                      }
                    >
                      <MoreHorizontal className="size-4" />
                    </DropdownMenuTrigger>

                    <DropdownMenuContent align="end">
                      <DropdownMenuItem
                        onClick={() => handleEdit(customer)}
                      >
                        Edit Customer
                      </DropdownMenuItem>

                      <DropdownMenuSeparator />

                      <DropdownMenuItem
                        className="text-destructive focus:text-destructive"
                        onClick={() => setDeleteTarget(customer)}
                      >
                        Delete Customer
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <AlertDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(null)
          }
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              Delete customer?
            </AlertDialogTitle>

            <AlertDialogDescription>
              This will permanently delete{' '}
              <strong>{deleteTarget?.name}</strong>.
              This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>
              Cancel
            </AlertDialogCancel>

            <AlertDialogAction
              variant="destructive"
              onClick={() => {
                if (deleteTarget) {
                  handleDelete(deleteTarget)
                }

                setDeleteTarget(null)
              }}
            >
              Delete Customer
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
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
  const inputId = `customer-${name}`

  return (
    <div className="space-y-2">
      <Label htmlFor={inputId}>
        {label}
        {required && (
          <span
            className="ml-1 text-destructive"
            aria-hidden="true"
          >
            *
          </span>
        )}
      </Label>

      <Input
        id={inputId}
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        required={required}
        inputMode={inputMode}
        maxLength={maxLength}
        placeholder={placeholder}
      />
    </div>
  )
}

function CustomerTableSkeleton() {
  return (
    <div className="space-y-3 p-6">
      {Array.from({ length: 5 }).map((_, index) => (
        <div
          key={index}
          className="flex items-center gap-4"
        >
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-5 w-28" />
          <Skeleton className="hidden h-5 w-44 md:block" />
          <Skeleton className="hidden h-5 flex-1 lg:block" />
        </div>
      ))}
    </div>
  )
}

export default CustomersPage