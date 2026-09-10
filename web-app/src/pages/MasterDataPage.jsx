import { useEffect, useState } from 'react'
import {
  MoreHorizontal,
  RefreshCw,
} from 'lucide-react'

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

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

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

const FRONTEND_ONLY =
  import.meta.env.VITE_FRONTEND_ONLY === 'true'

function MasterDataPage({
  title,
  description,
  api,
  fields,
  emptyForm,
  tableColumns,
  itemLabel,
  previewItems = [],
}) {
  const [items, setItems] = useState(() =>
    FRONTEND_ONLY ? previewItems : []
  )
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(!FRONTEND_ONLY)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)

  const itemPlural =
    itemLabel === 'Category'
      ? 'categories'
      : `${itemLabel.toLowerCase()}s`

  useEffect(() => {
    if (FRONTEND_ONLY) {
      return undefined
    }

    let cancelled = false

    api
      .getAll()
      .then((data) => {
        if (!cancelled) {
          setItems(data)
          setError('')
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message || `Failed to load ${itemLabel.toLowerCase()} records.`)
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
  }, [api, itemLabel])

  async function loadItems() {
    if (FRONTEND_ONLY) {
      setMessage(`Preview ${itemLabel.toLowerCase()} list refreshed.`)
      setError('')
      return
    }

    try {
      setLoading(true)
      setError('')

      const data = await api.getAll()
      setItems(data)
    } catch (err) {
      setError(err.message || `Failed to load ${itemLabel.toLowerCase()} records.`)
    } finally {
      setLoading(false)
    }
  }

  function handleChange(event) {
    const { name, value } = event.target

    setForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  function resetForm() {
    setForm(emptyForm)
    setEditingId(null)
  }

  function handleEdit(item) {
    const nextForm = {}

    fields.forEach((field) => {
      nextForm[field.name] = item[field.name] || ''
    })

    setForm(nextForm)
    setEditingId(item.id)
    setMessage('')
    setError('')

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
  }

  async function handleSubmit(event) {
    event.preventDefault()

    const requiredField = fields.find(
      (field) => field.required && !form[field.name]?.trim()
    )

    if (requiredField) {
      setError(`${requiredField.label} is required.`)
      return
    }

    if (FRONTEND_ONLY) {
      setError('')
      setMessage('')

      if (editingId) {
        setItems((currentItems) =>
          currentItems.map((item) =>
            item.id === editingId
              ? {
                  ...item,
                  ...form,
                }
              : item
          )
        )

        setMessage(`Preview ${itemLabel.toLowerCase()} updated successfully.`)
      } else {
        const newItem = {
          id: `mock-${itemLabel.toLowerCase()}-${Date.now()}`,
          ...form,
        }

        setItems((currentItems) => [
          ...currentItems,
          newItem,
        ])

        setMessage(`Preview ${itemLabel.toLowerCase()} created successfully.`)
      }

      resetForm()
      return
    }

    try {
      setSaving(true)
      setError('')
      setMessage('')

      if (editingId) {
        await api.update(editingId, form)
        setMessage(`${itemLabel} updated successfully.`)
      } else {
        await api.create(form)
        setMessage(`${itemLabel} created successfully.`)
      }

      resetForm()
      await loadItems()
    } catch (err) {
      setError(err.message || `Failed to save ${itemLabel.toLowerCase()}.`)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(item) {
    if (FRONTEND_ONLY) {
      setItems((currentItems) =>
        currentItems.filter(
          (currentItem) => currentItem.id !== item.id
        )
      )

      if (editingId === item.id) {
        resetForm()
      }

      setError('')
      setMessage(`Preview ${itemLabel.toLowerCase()} deleted successfully.`)
      return
    }

    try {
      setError('')
      setMessage('')

      await api.remove(item.id)

      setMessage(`${itemLabel} deleted successfully.`)
      await loadItems()
    } catch (err) {
      setError(err.message || `Failed to delete ${itemLabel.toLowerCase()}.`)
    }
  }

  const filteredItems = items.filter((item) => {
    const query = searchQuery.trim().toLowerCase()

    if (!query) {
      return true
    }

    return tableColumns.some((column) =>
      String(item[column.key] || '')
        .toLowerCase()
        .includes(query)
    )
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">
          {title}
        </h1>

        <p className="mt-1 text-muted-foreground">
          {description}
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
            <CardTitle>
              {editingId ? `Edit ${itemLabel}` : `Add ${itemLabel}`}
            </CardTitle>

            <CardDescription>
              {editingId
                ? `Update the selected ${itemLabel.toLowerCase()} record.`
                : `Create a new ${itemLabel.toLowerCase()} record.`}
            </CardDescription>
          </CardHeader>

          <CardContent>
            <form
              onSubmit={handleSubmit}
              className="space-y-5"
            >
              {fields.map((field) => (
                <FormField
                  key={field.name}
                  field={field}
                  value={form[field.name]}
                  onChange={handleChange}
                />
              ))}

              <div className="flex flex-wrap gap-3">
                <Button
                  type="submit"
                  disabled={saving}
                >
                  {saving
                    ? 'Saving...'
                    : editingId
                      ? `Update ${itemLabel}`
                      : `Add ${itemLabel}`}
                </Button>

                {editingId && (
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
                <CardTitle>
                  {itemLabel} List
                </CardTitle>

                <CardDescription>
                  {FRONTEND_ONLY
                    ? `${items.length} preview records`
                    : `Manage existing ${itemLabel.toLowerCase()} records.`}
                </CardDescription>
              </div>

              <div className="relative w-full sm:max-w-xs">
                <Input
                  type="search"
                  placeholder={`Search ${itemPlural}...`}
                  value={searchQuery}
                  onChange={(event) =>
                    setSearchQuery(event.target.value)
                  }
                  aria-label={`Search ${itemPlural}`}
                />
              </div>

              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={loadItems}
                disabled={loading}
              >
                <RefreshCw
                  className={loading ? 'animate-spin' : ''}
                  aria-hidden="true"
                />
                Refresh
              </Button>
            </div>
          </CardHeader>

          <CardContent className="p-0">
            {loading ? (
              <MasterDataTableSkeleton />
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      {tableColumns.map((column) => (
                        <TableHead key={column.key}>
                          {column.label}
                        </TableHead>
                      ))}

                      <TableHead className="text-right">
                        Actions
                      </TableHead>
                    </TableRow>
                  </TableHeader>

                  <TableBody>
                    {filteredItems.length === 0 ? (
                      <TableRow>
                        <TableCell
                          colSpan={tableColumns.length + 1}
                          className="h-28 text-center text-muted-foreground"
                        >
                          {searchQuery
                            ? `No ${itemLabel.toLowerCase()} records match your search.`
                            : 'No records found.'}
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredItems.map((item) => (
                        <TableRow key={item.id}>
                          {tableColumns.map((column) => (
                            <TableCell key={column.key}>
                              {item[column.key] || (
                                <span className="text-muted-foreground">
                                  Not provided
                                </span>
                              )}
                            </TableCell>
                          ))}

                          <TableCell>
                            <div className="flex justify-end">
                              <DropdownMenu>
                                <DropdownMenuTrigger
                                  render={
                                    <Button
                                      type="button"
                                      variant="ghost"
                                      size="icon"
                                      aria-label={`Actions for ${item.name || itemLabel}`}
                                    />
                                  }
                                >
                                  <MoreHorizontal
                                    className="size-4"
                                    aria-hidden="true"
                                  />
                                </DropdownMenuTrigger>

                                <DropdownMenuContent align="end">
                                  <DropdownMenuItem
                                    onClick={() =>
                                      handleEdit(item)
                                    }
                                  >
                                    Edit {itemLabel}
                                  </DropdownMenuItem>

                                  <DropdownMenuSeparator />

                                  <DropdownMenuItem
                                    className="text-destructive focus:text-destructive"
                                    onClick={() =>
                                      setDeleteTarget(item)
                                    }
                                  >
                                    Delete {itemLabel}
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
              Delete {itemLabel.toLowerCase()}?
            </AlertDialogTitle>

            <AlertDialogDescription>
              This will permanently delete{' '}
              <strong>
                {deleteTarget?.name || `this ${itemLabel.toLowerCase()}`}
              </strong>
              . This action cannot be undone.
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
              Delete {itemLabel}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}

function FormField({
  field,
  value,
  onChange,
}) {
  const inputId = `master-data-${field.name}`

  if (field.type === 'textarea') {
    return (
      <div className="space-y-2">
        <Label htmlFor={inputId}>
          {field.label}

          {field.required && (
            <span
              className="ml-1 text-destructive"
              aria-hidden="true"
            >
              *
            </span>
          )}
        </Label>

        <Textarea
          id={inputId}
          name={field.name}
          value={value || ''}
          onChange={onChange}
          required={field.required}
          rows={3}
        />
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <Label htmlFor={inputId}>
        {field.label}

        {field.required && (
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
        type={field.type || 'text'}
        name={field.name}
        value={value || ''}
        onChange={onChange}
        required={field.required}
      />
    </div>
  )
}

function MasterDataTableSkeleton() {
  return (
    <div className="space-y-3 p-6">
      {Array.from({ length: 4 }).map((_, index) => (
        <div
          key={index}
          className="flex items-center gap-4"
        >
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-5 flex-1" />
          <Skeleton className="h-8 w-10" />
        </div>
      ))}
    </div>
  )
}

export default MasterDataPage
