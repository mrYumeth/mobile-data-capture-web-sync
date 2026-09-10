import { useEffect, useState } from 'react'
import {
  Eye,
  Maximize2,
  RefreshCw,
  Search,
  X,
} from 'lucide-react'

import { capturedRecordApi } from '../services/api'

import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/components/ui/alert'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

const FRONTEND_ONLY =
  import.meta.env.VITE_FRONTEND_ONLY === 'true'

function createPreviewImage(label) {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="800" viewBox="0 0 1200 800">
      <rect width="1200" height="800" fill="#f4f4f5"/>
      <rect x="60" y="60" width="1080" height="680" rx="32" fill="#ffffff" stroke="#e4e4e7" stroke-width="4"/>
      <circle cx="600" cy="320" r="92" fill="#EB5979" opacity="0.16"/>
      <path d="M550 320h100M600 270v100" stroke="#EB5979" stroke-width="18" stroke-linecap="round"/>
      <text x="600" y="500" text-anchor="middle" font-family="Arial, sans-serif" font-size="42" font-weight="700" fill="#18181b">${label}</text>
      <text x="600" y="555" text-anchor="middle" font-family="Arial, sans-serif" font-size="24" fill="#71717a">FieldSync frontend preview</text>
    </svg>
  `

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

const MOCK_RECORDS = [
  {
    id: 1001,
    customer_name: 'Lanka Retail Solutions',
    location_name: 'Colombo Central',
    category_name: 'Site Inspection',
    description: 'Routine field inspection completed successfully.',
    latitude: 6.9271,
    longitude: 79.8612,
    captured_at: '2026-09-10T08:45:00Z',
    received_at: '2026-09-10T08:47:00Z',
    images: [
      {
        full_image_url: createPreviewImage('Site Inspection 01'),
      },
      {
        full_image_url: createPreviewImage('Site Inspection 02'),
      },
    ],
  },
  {
    id: 1002,
    customer_name: 'Southern Distribution',
    location_name: 'Galle Regional Office',
    category_name: 'Inventory Check',
    description: 'Inventory quantities verified during the customer visit.',
    latitude: 6.0329,
    longitude: 80.2168,
    captured_at: '2026-09-09T10:15:00Z',
    received_at: '2026-09-09T10:18:00Z',
    images: [
      {
        full_image_url: createPreviewImage('Inventory Check'),
      },
    ],
  },
  {
    id: 1003,
    customer_name: 'Kandy Field Services',
    location_name: 'Kandy Field Point',
    category_name: 'Maintenance',
    description: 'Maintenance status and site condition captured.',
    latitude: 7.2906,
    longitude: 80.6337,
    captured_at: '2026-09-08T05:30:00Z',
    received_at: '2026-09-08T05:35:00Z',
    images: [
      {
        full_image_url: createPreviewImage('Maintenance 01'),
      },
      {
        full_image_url: createPreviewImage('Maintenance 02'),
      },
      {
        full_image_url: createPreviewImage('Maintenance 03'),
      },
    ],
  },
  {
    id: 1004,
    customer_name: 'Northline Traders',
    location_name: 'Jaffna Service Area',
    category_name: 'Customer Visit',
    description: 'Customer visit completed and follow-up information recorded.',
    latitude: 9.6615,
    longitude: 80.0255,
    captured_at: '2026-09-07T07:20:00Z',
    received_at: '2026-09-07T07:23:00Z',
    images: [],
  },
]

function CapturedRecordsPage() {
  const [records, setRecords] = useState(() =>
    FRONTEND_ONLY ? MOCK_RECORDS : []
  )
  const [selectedRecord, setSelectedRecord] = useState(null)
  const [isFullScreenOpen, setIsFullScreenOpen] = useState(false)
  const [loading, setLoading] = useState(!FRONTEND_ONLY)
  const [detailsLoading, setDetailsLoading] = useState(false)
  const [error, setError] = useState('')
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    if (FRONTEND_ONLY) {
      return undefined
    }

    let cancelled = false

    capturedRecordApi
      .getAll()
      .then((data) => {
        if (!cancelled) {
          setRecords(data)
          setError('')
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message || 'Failed to load captured records.')
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

  async function loadRecords() {
    if (FRONTEND_ONLY) {
      setRecords(MOCK_RECORDS)
      setSelectedRecord(null)
      setIsFullScreenOpen(false)
      setError('')
      return
    }

    try {
      setLoading(true)
      setError('')

      const data = await capturedRecordApi.getAll()
      setRecords(data)
    } catch (err) {
      setError(err.message || 'Failed to load captured records.')
    } finally {
      setLoading(false)
    }
  }

  async function openDetails(recordId) {
    if (FRONTEND_ONLY) {
      const record = records.find(
        (currentRecord) => currentRecord.id === recordId
      )

      setSelectedRecord(record || null)
      setIsFullScreenOpen(false)
      setError('')
      return
    }

    try {
      setDetailsLoading(true)
      setError('')
      setIsFullScreenOpen(false)

      const data = await capturedRecordApi.getById(recordId)
      setSelectedRecord(data)
    } catch (err) {
      setError(err.message || 'Failed to load record details.')
    } finally {
      setDetailsLoading(false)
    }
  }

  function closeDetails() {
    setSelectedRecord(null)
    setIsFullScreenOpen(false)
  }

  function formatDateTime(value) {
    if (!value) {
      return '-'
    }

    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
      return value
    }

    return date.toLocaleString()
  }

  const filteredRecords = records.filter((record) => {
    const query = searchQuery.trim().toLowerCase()

    if (!query) {
      return true
    }

    return [
      record.id,
      record.customer_name,
      record.location_name,
      record.category_name,
      record.description,
    ].some((value) =>
      String(value || '')
        .toLowerCase()
        .includes(query)
    )
  })

  return (
    <div className="min-w-0 space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">
          Captured Records
        </h1>

        <p className="mt-1 text-muted-foreground">
          View field records and captured images uploaded from the mobile app.
        </p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertTitle>Something went wrong</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid min-w-0 grid-cols-1 gap-6 2xl:grid-cols-[minmax(0,1fr)_420px]">
        <Card className="min-w-0">
          <CardHeader>
            <div className="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
              <div>
                <CardTitle>Uploaded Records</CardTitle>

                <CardDescription>
                  {FRONTEND_ONLY
                    ? `${records.length} preview captured records`
                    : 'Records received from the mobile data capture app.'}
                </CardDescription>
              </div>

              <div className="flex w-full flex-col gap-2 sm:flex-row xl:w-auto">
                <div className="relative w-full sm:min-w-[280px]">
                  <Search
                    className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
                    aria-hidden="true"
                  />

                  <Input
                    type="search"
                    placeholder="Search captured records..."
                    value={searchQuery}
                    onChange={(event) =>
                      setSearchQuery(event.target.value)
                    }
                    className="pl-9"
                    aria-label="Search captured records"
                  />
                </div>

                <Button
                  type="button"
                  variant="outline"
                  onClick={loadRecords}
                  disabled={loading}
                >
                  <RefreshCw
                    className={loading ? 'animate-spin' : ''}
                    aria-hidden="true"
                  />
                  Refresh
                </Button>
              </div>
            </div>
          </CardHeader>

          <CardContent className="p-0">
            {loading ? (
              <CapturedRecordsSkeleton />
            ) : (
              <div className="w-full overflow-x-auto">
                <Table className="min-w-[860px]">
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>Customer</TableHead>
                      <TableHead>Location</TableHead>
                      <TableHead>Category</TableHead>
                      <TableHead>Images</TableHead>
                      <TableHead>Captured At</TableHead>
                      <TableHead>Received At</TableHead>
                      <TableHead className="text-right">
                        Action
                      </TableHead>
                    </TableRow>
                  </TableHeader>

                  <TableBody>
                    {filteredRecords.length === 0 ? (
                      <TableRow>
                        <TableCell
                          colSpan={8}
                          className="h-28 text-center text-muted-foreground"
                        >
                          {searchQuery
                            ? 'No captured records match your search.'
                            : 'No captured records found.'}
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredRecords.map((record) => {
                        const imageCount =
                          getRecordImageUrls(record).length

                        return (
                          <TableRow
                            key={record.id}
                            data-state={
                              selectedRecord?.id === record.id
                                ? 'selected'
                                : undefined
                            }
                          >
                            <TableCell className="font-medium">
                              #{record.id}
                            </TableCell>

                            <TableCell>
                              {record.customer_name || '-'}
                            </TableCell>

                            <TableCell>
                              {record.location_name || '-'}
                            </TableCell>

                            <TableCell>
                              {record.category_name || '-'}
                            </TableCell>

                            <TableCell>
                              <Badge
                                variant={
                                  imageCount > 0
                                    ? 'secondary'
                                    : 'outline'
                                }
                              >
                                {imageCount}{' '}
                                {imageCount === 1 ? 'image' : 'images'}
                              </Badge>
                            </TableCell>

                            <TableCell className="whitespace-nowrap">
                              {formatDateTime(record.captured_at)}
                            </TableCell>

                            <TableCell className="whitespace-nowrap">
                              {formatDateTime(record.received_at)}
                            </TableCell>

                            <TableCell className="text-right">
                              <Button
                                type="button"
                                size="sm"
                                onClick={() =>
                                  openDetails(record.id)
                                }
                              >
                                <Eye aria-hidden="true" />
                                View
                              </Button>
                            </TableCell>
                          </TableRow>
                        )
                      })
                    )}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="min-w-0">
          <CardHeader>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <CardTitle>Record Details</CardTitle>

                <CardDescription>
                  Inspect the selected field record and its images.
                </CardDescription>
              </div>

              {selectedRecord && (
                <div className="flex flex-wrap gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() =>
                      setIsFullScreenOpen(true)
                    }
                  >
                    <Maximize2 aria-hidden="true" />
                    Full Screen
                  </Button>

                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={closeDetails}
                  >
                    <X aria-hidden="true" />
                    Clear
                  </Button>
                </div>
              )}
            </div>
          </CardHeader>

          <CardContent>
            {detailsLoading ? (
              <RecordDetailsSkeleton />
            ) : selectedRecord ? (
              <RecordDetails
                record={selectedRecord}
                formatDateTime={formatDateTime}
              />
            ) : (
              <div className="rounded-lg border border-dashed p-8 text-center">
                <p className="font-medium">
                  Select a record to view details.
                </p>

                <p className="mt-1 text-sm text-muted-foreground">
                  The selected record&apos;s GPS, images, and captured data will appear here.
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {isFullScreenOpen && selectedRecord && (
        <FullScreenRecordModal
          record={selectedRecord}
          formatDateTime={formatDateTime}
          onClose={() =>
            setIsFullScreenOpen(false)
          }
        />
      )}
    </div>
  )
}

function getRecordImageUrls(record) {
  if (
    Array.isArray(record.images) &&
    record.images.length > 0
  ) {
    return record.images
      .map((image) => image.full_image_url)
      .filter(Boolean)
  }

  return record.full_image_url
    ? [record.full_image_url]
    : []
}

function RecordDetails({
  record,
  formatDateTime,
}) {
  const imageUrls = getRecordImageUrls(record)

  return (
    <div className="space-y-5">
      <div className="rounded-lg bg-muted p-4">
        <p className="text-sm font-medium text-muted-foreground">
          Record ID
        </p>

        <p className="text-xl font-bold">
          #{record.id}
        </p>
      </div>

      <DetailSection title="Master Data">
        <DetailRow
          label="Customer"
          value={record.customer_name}
        />
        <DetailRow
          label="Location"
          value={record.location_name}
        />
        <DetailRow
          label="Category"
          value={record.category_name}
        />
      </DetailSection>

      <DetailSection title="Captured Information">
        <DetailRow
          label="Captured At"
          value={formatDateTime(record.captured_at)}
        />
        <DetailRow
          label="Received At"
          value={formatDateTime(record.received_at)}
        />
        <DetailRow
          label="Description"
          value={record.description}
        />
      </DetailSection>

      <DetailSection title="GPS Coordinates">
        <DetailRow
          label="Latitude"
          value={record.latitude}
        />
        <DetailRow
          label="Longitude"
          value={record.longitude}
        />
      </DetailSection>

      <div>
        <h3 className="mb-2 text-sm font-semibold uppercase tracking-wide text-muted-foreground">
          Captured Images
        </h3>

        <ImageGallery imageUrls={imageUrls} />
      </div>
    </div>
  )
}

function FullScreenRecordModal({
  record,
  formatDateTime,
  onClose,
}) {
  const imageUrls = getRecordImageUrls(record)

  return (
    <div className="fixed inset-0 z-[999] flex items-center justify-center bg-black/70 px-4 py-6 backdrop-blur-sm">
      <Card className="max-h-[92vh] w-full max-w-6xl overflow-hidden">
        <CardHeader className="border-b">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-sm font-bold uppercase tracking-[0.25em] text-primary">
                Captured Record
              </p>

              <CardTitle className="mt-1 text-2xl">
                Record #{record.id}
              </CardTitle>

              <CardDescription>
                Full screen view of uploaded field record details.
              </CardDescription>
            </div>

            <Button
              type="button"
              variant="outline"
              onClick={onClose}
            >
              <X aria-hidden="true" />
              Close
            </Button>
          </div>
        </CardHeader>

        <CardContent className="max-h-[calc(92vh-120px)] overflow-y-auto p-6">
          <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_460px]">
            <div className="space-y-5">
              <DetailSection title="Master Data">
                <DetailRow
                  label="Customer"
                  value={record.customer_name}
                />
                <DetailRow
                  label="Location"
                  value={record.location_name}
                />
                <DetailRow
                  label="Category"
                  value={record.category_name}
                />
              </DetailSection>

              <DetailSection title="Captured Information">
                <DetailRow
                  label="Captured At"
                  value={formatDateTime(record.captured_at)}
                />
                <DetailRow
                  label="Received At"
                  value={formatDateTime(record.received_at)}
                />
                <DetailRow
                  label="Description"
                  value={record.description}
                />
              </DetailSection>

              <DetailSection title="GPS Coordinates">
                <DetailRow
                  label="Latitude"
                  value={record.latitude}
                />
                <DetailRow
                  label="Longitude"
                  value={record.longitude}
                />
              </DetailSection>
            </div>

            <div>
              <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-muted-foreground">
                Captured Images
              </h3>

              <ImageGallery
                imageUrls={imageUrls}
                large
              />

              <div className="mt-4 rounded-lg bg-muted p-4 text-sm text-muted-foreground">
                Click any image to open it in a new browser tab.
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

function ImageGallery({
  imageUrls,
  large = false,
}) {
  if (imageUrls.length === 0) {
    return (
      <div className="rounded-lg border border-dashed p-6 text-center text-muted-foreground">
        No images available.
      </div>
    )
  }

  return (
    <div
      className={
        large
          ? 'grid grid-cols-2 gap-3'
          : 'grid grid-cols-1 gap-3 sm:grid-cols-2'
      }
    >
      {imageUrls.map((imageUrl, index) => (
        <a
          key={`${imageUrl}-${index}`}
          href={imageUrl}
          target="_blank"
          rel="noreferrer"
          className="group block"
        >
          <div className="overflow-hidden rounded-xl border bg-background shadow-sm">
            <img
              src={imageUrl}
              alt={`Captured field record ${index + 1}`}
              className={`w-full object-cover transition group-hover:scale-105 ${
                large ? 'h-44' : 'h-40'
              }`}
            />

            {large && (
              <div className="px-3 py-2 text-xs font-semibold text-muted-foreground">
                Image {index + 1}
              </div>
            )}
          </div>
        </a>
      ))}
    </div>
  )
}

function DetailSection({
  title,
  children,
}) {
  return (
    <div>
      <h3 className="mb-2 text-sm font-semibold uppercase tracking-wide text-muted-foreground">
        {title}
      </h3>

      <div className="overflow-hidden rounded-lg border">
        {children}
      </div>
    </div>
  )
}

function DetailRow({
  label,
  value,
}) {
  return (
    <div className="grid grid-cols-1 gap-1 border-b px-4 py-3 last:border-b-0 sm:grid-cols-[130px_1fr]">
      <span className="font-medium text-muted-foreground">
        {label}
      </span>

      <span className="min-w-0 break-words">
        {value ?? '-'}
      </span>
    </div>
  )
}

function CapturedRecordsSkeleton() {
  return (
    <div className="space-y-3 p-6">
      {Array.from({ length: 5 }).map((_, index) => (
        <div
          key={index}
          className="flex items-center gap-4"
        >
          <Skeleton className="h-5 w-16" />
          <Skeleton className="h-5 w-40" />
          <Skeleton className="hidden h-5 w-32 md:block" />
          <Skeleton className="hidden h-5 flex-1 lg:block" />
          <Skeleton className="h-8 w-20" />
        </div>
      ))}
    </div>
  )
}

function RecordDetailsSkeleton() {
  return (
    <div className="space-y-4">
      <Skeleton className="h-20 w-full" />
      <Skeleton className="h-32 w-full" />
      <Skeleton className="h-40 w-full" />
      <Skeleton className="h-28 w-full" />
    </div>
  )
}

export default CapturedRecordsPage
