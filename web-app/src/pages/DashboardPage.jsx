import { useEffect, useState } from 'react'
import {
  Database,
  MapPin,
  Tags,
  Users,
} from 'lucide-react'

import { dashboardApi } from '../services/api'

import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/components/ui/alert'

import { Badge } from '@/components/ui/badge'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import { Skeleton } from '@/components/ui/skeleton'

const FRONTEND_ONLY =
  import.meta.env.VITE_FRONTEND_ONLY === 'true'

const MOCK_SUMMARY = {
  customers: 42,
  locations: 18,
  categories: 7,
  capturedRecords: 1284,
}

const EMPTY_SUMMARY = {
  customers: 0,
  locations: 0,
  categories: 0,
  capturedRecords: 0,
}

const summaryItems = [
  {
    key: 'customers',
    title: 'Customers',
    description: 'Active customer records',
    icon: Users,
  },
  {
    key: 'locations',
    title: 'Locations',
    description: 'Available field locations',
    icon: MapPin,
  },
  {
    key: 'categories',
    title: 'Categories',
    description: 'Capture categories',
    icon: Tags,
  },
  {
    key: 'capturedRecords',
    title: 'Captured Records',
    description: 'Uploaded mobile records',
    icon: Database,
  },
]

function DashboardPage() {
  const [summary, setSummary] = useState(() =>
    FRONTEND_ONLY ? MOCK_SUMMARY : EMPTY_SUMMARY
  )

  const [loading, setLoading] = useState(!FRONTEND_ONLY)
  const [error, setError] = useState('')

  useEffect(() => {
    if (FRONTEND_ONLY) {
      return undefined
    }

    let cancelled = false

    dashboardApi
      .getSummary()
      .then((data) => {
        if (cancelled) {
          return
        }

        setSummary(data)
        setError('')
      })
      .catch((err) => {
        if (cancelled) {
          return
        }

        setError(
          err.message || 'Failed to load dashboard summary.'
        )
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

  if (loading) {
    return <DashboardSkeleton />
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            Dashboard
          </h1>

          <p className="mt-1 text-muted-foreground">
            Overview of master data and uploaded field records.
          </p>
        </div>

        <Badge variant="secondary" className="w-fit">
          {FRONTEND_ONLY ? 'Preview Data' : 'Live Data'}
        </Badge>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertTitle>Unable to load dashboard</AlertTitle>

          <AlertDescription>
            {error}
          </AlertDescription>
        </Alert>
      )}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {summaryItems.map((item) => (
          <SummaryCard
            key={item.key}
            title={item.title}
            value={summary[item.key]}
            description={item.description}
            icon={item.icon}
          />
        ))}
      </div>
    </div>
  )
}

function SummaryCard({
  title,
  value,
  description,
  icon: Icon,
}) {
  return (
    <Card className="transition-shadow hover:shadow-md">
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div className="space-y-1">
          <CardTitle className="text-sm font-medium">
            {title}
          </CardTitle>

          <CardDescription>
            {description}
          </CardDescription>
        </div>

        <div className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
          <Icon className="size-5" aria-hidden="true" />
        </div>
      </CardHeader>

      <CardContent>
        <p className="text-3xl font-bold tracking-tight">
          {Number(value ?? 0).toLocaleString()}
        </p>
      </CardContent>
    </Card>
  )
}

function DashboardSkeleton() {
  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <Skeleton className="h-8 w-40" />
        <Skeleton className="h-4 w-80 max-w-full" />
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: 4 }).map((_, index) => (
          <Card key={index}>
            <CardHeader className="space-y-3">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-4 w-36" />
            </CardHeader>

            <CardContent>
              <Skeleton className="h-9 w-20" />
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}

export default DashboardPage