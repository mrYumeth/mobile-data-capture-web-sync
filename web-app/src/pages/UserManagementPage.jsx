import { useEffect, useState } from 'react'
import { userApi } from '../services/api'
import ChangePasswordPage from './ChangePasswordPage'

import { Button } from '@/components/ui/button'

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/components/ui/alert'

import { Badge } from '@/components/ui/badge'

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'

const FRONTEND_ONLY =
  import.meta.env.VITE_FRONTEND_ONLY === 'true'

const MOCK_USERS = [
  {
    id: 'mock-admin-1',
    full_name: 'System Administrator',
    username: 'admin',
    email: 'admin@fieldsync.lk',
    role: 'admin',
    access_web: true,
    access_mobile: true,
    is_active: true,
    confirmed_at: '2026-08-12T09:30:00Z',
    keycloak_user_id: 'mock-keycloak-admin',
  },
  {
    id: 'mock-user-1',
    full_name: 'Nimal Perera',
    username: 'nimal.perera',
    email: 'nimal.perera@example.com',
    role: 'user',
    access_web: true,
    access_mobile: true,
    is_active: true,
    confirmed_at: '2026-08-21T11:15:00Z',
    keycloak_user_id: 'mock-keycloak-1',
  },
  {
    id: 'mock-user-2',
    full_name: 'Sachini Fernando',
    username: 'sachini.fernando',
    email: 'sachini.fernando@example.com',
    role: 'user',
    access_web: false,
    access_mobile: true,
    is_active: true,
    confirmed_at: '2026-09-01T08:45:00Z',
    keycloak_user_id: 'mock-keycloak-2',
  },
  {
    id: 'mock-user-3',
    full_name: 'Kasun Silva',
    username: 'kasun.silva',
    email: 'kasun.silva@example.com',
    role: 'user',
    access_web: true,
    access_mobile: false,
    is_active: false,
    confirmed_at: null,
    keycloak_user_id: null,
  },
  {
    id: 'mock-user-4',
    full_name: 'Amaya Jayasinghe',
    username: 'amaya.j',
    email: 'amaya.jayasinghe@example.com',
    role: 'user',
    access_web: true,
    access_mobile: true,
    is_active: true,
    confirmed_at: null,
    keycloak_user_id: null,
  },
]

function UserManagementPage() {
  const [users, setUsers] = useState(() =>
    FRONTEND_ONLY ? MOCK_USERS : []
  )

  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    accessWeb: true,
    accessMobile: false,
  })

  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [lastSetupLink, setLastSetupLink] = useState('')
  const [temporaryPassword, setTemporaryPassword] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('users')
  const [editingUserId, setEditingUserId] = useState(null)

  const isKeycloakAuth =
    (import.meta.env.VITE_AUTH_PROVIDER || 'keycloak') === 'keycloak'

  const passwordResetEnabled =
    import.meta.env.VITE_ENABLE_KEYCLOAK_PASSWORD_RESET === 'true'

  async function loadUsers() {
    if (FRONTEND_ONLY) {
      setUsers(MOCK_USERS)
      setError('')
      return
    }

    try {
      const data = await userApi.getAll()
      setUsers(data)
    } catch (error) {
      setError(
        error.message || 'Failed to load users.'
      )
    }
  }

  useEffect(() => {
    if (FRONTEND_ONLY) {
      return undefined
    }

    let cancelled = false

    userApi
      .getAll()
      .then((data) => {
        if (!cancelled) {
          setUsers(data)
          setError('')
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setError(
            error.message ||
              'Failed to load users.'
          )
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  function handleChange(event) {
    const {
      name,
      value,
      checked,
      type,
    } = event.target

    setFormData((current) => ({
      ...current,
      [name]:
        type === 'checkbox'
          ? checked
          : value,
    }))
  }

  async function handleCreateUser(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    setLastSetupLink('')
    setTemporaryPassword('')

    if (FRONTEND_ONLY) {
      if (
        !formData.accessWeb &&
        !formData.accessMobile
      ) {
        setError(
          'Select at least one access type: Web app, Mobile app, or both.'
        )
        return
      }

      if (editingUserId) {
        setUsers((currentUsers) =>
          currentUsers.map((user) =>
            user.id === editingUserId
              ? {
                  ...user,
                  full_name:
                    formData.fullName,
                  email:
                    formData.email,
                  access_web:
                    formData.accessWeb,
                  access_mobile:
                    formData.accessMobile,
                }
              : user
          )
        )

        setMessage(
          'Preview user updated successfully.'
        )
        handleCancelEdit()
        return
      }

      const mockUser = {
        id: `mock-user-${Date.now()}`,
        full_name: formData.fullName,
        username: formData.username,
        email: formData.email,
        role: 'user',
        access_web: formData.accessWeb,
        access_mobile:
          formData.accessMobile,
        is_active: true,
        confirmed_at: null,
        keycloak_user_id: null,
      }

      setUsers((currentUsers) => [
        ...currentUsers,
        mockUser,
      ])

      setFormData({
        fullName: '',
        username: '',
        email: '',
        accessWeb: true,
        accessMobile: false,
      })

      setMessage(
        'Preview user created successfully.'
      )
      return
    }

    try {
      setIsLoading(true)

      if (editingUserId) {
        const result =
          await userApi.update(
            editingUserId,
            {
              fullName:
                formData.fullName,
              email:
                formData.email,
              accessWeb:
                formData.accessWeb,
              accessMobile:
                formData.accessMobile,
            }
          )

        setMessage(
          result.message ||
            'User updated successfully.'
        )
        handleCancelEdit()
        await loadUsers()
        return
      }

      if (
        !formData.accessWeb &&
        !formData.accessMobile
      ) {
        setError(
          'Select at least one access type: Web app, Mobile app, or both.'
        )
        return
      }

      const result =
        await userApi.create(formData)

      setMessage(
        result.message ||
          'User created successfully.'
      )
      setLastSetupLink(
        result.setupLink || ''
      )
      setTemporaryPassword(
        result.keycloakTemporaryPassword || ''
      )

      setFormData({
        fullName: '',
        username: '',
        email: '',
        accessWeb: true,
        accessMobile: false,
      })

      await loadUsers()
    } catch (error) {
      setError(
        error.message ||
          'Failed to save user.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  function handleEditUser(user) {
    setEditingUserId(user.id)

    setFormData({
      fullName:
        user.full_name || '',
      username:
        user.username || '',
      email:
        user.email || '',
      accessWeb:
        Boolean(user.access_web),
      accessMobile:
        Boolean(user.access_mobile),
    })

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
  }

  async function handleDeleteUser(user) {
    if (FRONTEND_ONLY) {
      setUsers((currentUsers) =>
        currentUsers.filter(
          (currentUser) =>
            currentUser.id !== user.id
        )
      )

      setMessage('Preview user deleted.')
      setError('')
      return
    }

    try {
      setError('')
      setMessage('')

      const result =
        await userApi.remove(user.id)

      setMessage(
        result.message ||
          'User deleted successfully.'
      )
      await loadUsers()
    } catch (error) {
      setError(
        error.message ||
          'Failed to delete user.'
      )
    }
  }

  async function handleResetKeycloakPassword(user) {
    const confirmed = window.confirm(
      `Send an email and request ${user.full_name || user.username} to reset their password?`
    )

    if (!confirmed) {
      return
    }

    try {
      setError('')
      setMessage('')
      setLastSetupLink('')
      setTemporaryPassword('')

      const result =
        await userApi.resetKeycloakPassword(
          user.id
        )

      setMessage(
        result.message ||
          'Temporary password generated successfully.'
      )
      setTemporaryPassword(
        result.keycloakTemporaryPassword || ''
      )
    } catch (error) {
      setError(
        error.message ||
          'Failed to reset Keycloak password.'
      )
    }
  }

  function handleCancelEdit() {
    setEditingUserId(null)

    setFormData({
      fullName: '',
      username: '',
      email: '',
      accessWeb: true,
      accessMobile: false,
    })
  }

  async function updateAccess(user, changes) {
    if (FRONTEND_ONLY) {
      setUsers((currentUsers) =>
        currentUsers.map((currentUser) =>
          currentUser.id === user.id
            ? {
                ...currentUser,
                ...(changes.accessWeb !== undefined && {
                  access_web:
                    changes.accessWeb,
                }),
                ...(changes.accessMobile !== undefined && {
                  access_mobile:
                    changes.accessMobile,
                }),
                ...(changes.isActive !== undefined && {
                  is_active:
                    changes.isActive,
                }),
              }
            : currentUser
        )
      )

      setMessage(
        'Preview user access updated.'
      )
      setError('')
      return
    }

    try {
      setError('')
      setMessage('')

      const result =
        await userApi.updateAccess(
          user.id,
          changes
        )

      setMessage(
        result.message ||
          'User access updated.'
      )
      await loadUsers()
    } catch (error) {
      setError(
        error.message ||
          'Failed to update access.'
      )
    }
  }

  return (
    <section>
      <div className="mb-6">
        <p className="text-sm font-bold uppercase tracking-[0.25em] text-[#EB5979]">
          Admin
        </p>

        <h2 className="mt-2 text-3xl font-extrabold">
          Users
        </h2>

        <p className="mt-2 text-gray-500">
          Create FieldSync user profiles and assign web or mobile app access. Passwords are managed through Keycloak.
        </p>
      </div>

      <div className="mb-6 flex flex-wrap gap-3">
        <Button
          type="button"
          variant={
            activeTab === 'users'
              ? 'default'
              : 'outline'
          }
          onClick={() =>
            setActiveTab('users')
          }
          aria-pressed={
            activeTab === 'users'
          }
        >
          User Management
        </Button>

        {!isKeycloakAuth && (
          <Button
            type="button"
            variant={
              activeTab === 'password'
                ? 'default'
                : 'outline'
            }
            onClick={() =>
              setActiveTab('password')
            }
            aria-pressed={
              activeTab === 'password'
            }
          >
            Change My Password
          </Button>
        )}
      </div>

      {activeTab === 'password' && (
        <ChangePasswordPage />
      )}

      {activeTab === 'users' && (
        <>
          {error && (
            <Alert
              variant="destructive"
              className="mb-5"
            >
              <AlertTitle>
                Something went wrong
              </AlertTitle>

              <AlertDescription>
                {error}
              </AlertDescription>
            </Alert>
          )}

          {message && (
            <Alert className="mb-5 border-green-200 bg-green-50 text-green-800 dark:border-green-900 dark:bg-green-950/30 dark:text-green-300">
              <AlertTitle>
                Success
              </AlertTitle>

              <AlertDescription>
                {message}
              </AlertDescription>
            </Alert>
          )}

          {temporaryPassword && (
            <Alert className="mb-5 border-amber-200 bg-amber-50 text-amber-900 dark:border-amber-900 dark:bg-amber-950/30 dark:text-amber-200">
              <AlertTitle>
                Temporary Keycloak Password
              </AlertTitle>

              <AlertDescription className="space-y-2">
                <p className="break-all text-base">
                  {temporaryPassword}
                </p>

                <p>
                  Copy this password and provide it to the user securely.
                  The user will be asked to change it after first login.
                </p>
              </AlertDescription>
            </Alert>
          )}

          {lastSetupLink && (
            <Alert className="mb-5 border-amber-200 bg-amber-50 text-amber-900 dark:border-amber-900 dark:bg-amber-950/30 dark:text-amber-200">
              <AlertTitle>
                Setup Link
              </AlertTitle>

              <AlertDescription className="space-y-2">
                <p className="break-all">
                  {lastSetupLink}
                </p>

                <p>
                  Copy this link and send it manually if email is not configured.
                </p>
              </AlertDescription>
            </Alert>
          )}

          <Card className="mb-8">
            <CardHeader>
              <CardTitle>
                {editingUserId
                  ? 'Edit User'
                  : 'Create User'}
              </CardTitle>
            </CardHeader>

            <CardContent>
              <form
                onSubmit={
                  handleCreateUser
                }
                className="grid gap-5 lg:grid-cols-2"
              >
                <div className="space-y-2">
                  <Label htmlFor="fullName">
                    Full Name
                  </Label>

                  <Input
                    id="fullName"
                    name="fullName"
                    value={
                      formData.fullName
                    }
                    onChange={
                      handleChange
                    }
                    minLength={2}
                    maxLength={150}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="username">
                    Username
                  </Label>

                  <Input
                    id="username"
                    name="username"
                    value={
                      formData.username
                    }
                    onChange={
                      handleChange
                    }
                    disabled={
                      Boolean(
                        editingUserId
                      )
                    }
                    minLength={3}
                    maxLength={100}
                    pattern="[A-Za-z0-9._-]+"
                    title="Use only letters, numbers, dots, underscores and hyphens"
                    required
                  />
                </div>

                <div className="space-y-2 lg:col-span-2">
                  <Label htmlFor="email">
                    Email
                  </Label>

                  <Input
                    id="email"
                    name="email"
                    type="email"
                    value={
                      formData.email
                    }
                    onChange={
                      handleChange
                    }
                    maxLength={150}
                    required
                  />
                </div>

                <div className="flex items-center gap-6 lg:col-span-2">
                  <label className="flex items-center gap-2 text-sm font-semibold">
                    <input
                      type="checkbox"
                      name="accessWeb"
                      checked={
                        formData.accessWeb
                      }
                      onChange={
                        handleChange
                      }
                    />
                    Web App Access
                  </label>

                  <label className="flex items-center gap-2 text-sm font-semibold">
                    <input
                      type="checkbox"
                      name="accessMobile"
                      checked={
                        formData.accessMobile
                      }
                      onChange={
                        handleChange
                      }
                    />
                    Mobile App Access
                  </label>
                </div>

                <div className="flex flex-wrap gap-3 lg:col-span-2">
                  <Button
                    type="submit"
                    disabled={
                      isLoading
                    }
                  >
                    {isLoading
                      ? editingUserId
                        ? 'Updating User...'
                        : 'Creating User...'
                      : editingUserId
                        ? 'Update User'
                        : 'Create User'}
                  </Button>

                  <Button
                    type="button"
                    variant="outline"
                    onClick={
                      handleCancelEdit
                    }
                  >
                    Cancel Edit
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>
                Users
              </CardTitle>
            </CardHeader>

            <CardContent className="p-0">
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>
                        User
                      </TableHead>

                      <TableHead>
                        Role
                      </TableHead>

                      <TableHead>
                        Web
                      </TableHead>

                      <TableHead>
                        Mobile
                      </TableHead>

                      <TableHead>
                        Active
                      </TableHead>

                      <TableHead>
                        Confirmed
                      </TableHead>

                      <TableHead>
                        IAM
                      </TableHead>

                      <TableHead className="text-right">
                        Actions
                      </TableHead>
                    </TableRow>
                  </TableHeader>

                  <TableBody>
                    {users.length === 0 ? (
                      <TableRow>
                        <TableCell
                          colSpan={8}
                          className="h-24 text-center text-muted-foreground"
                        >
                          No users found.
                        </TableCell>
                      </TableRow>
                    ) : (
                      users.map((user) => (
                        <TableRow
                          key={user.id}
                        >
                          <TableCell>
                            <div className="space-y-1">
                              <p className="font-medium">
                                {user.full_name ||
                                  user.username}
                              </p>

                              <p className="text-sm text-muted-foreground">
                                {user.email}
                              </p>

                              <p className="text-xs text-muted-foreground">
                                @{user.username}
                              </p>
                            </div>
                          </TableCell>

                          <TableCell>
                            <Badge variant="secondary">
                              {user.role}
                            </Badge>
                          </TableCell>

                          <TableCell>
                            <Checkbox
                              checked={
                                Boolean(
                                  user.access_web
                                )
                              }
                              disabled={
                                user.role ===
                                'admin'
                              }
                              aria-label={`Web access for ${
                                user.full_name ||
                                user.username
                              }`}
                              onCheckedChange={(
                                checked
                              ) =>
                                updateAccess(
                                  user,
                                  {
                                    accessWeb:
                                      checked ===
                                      true,
                                  }
                                )
                              }
                            />
                          </TableCell>

                          <TableCell>
                            <Checkbox
                              checked={
                                Boolean(
                                  user.access_mobile
                                )
                              }
                              disabled={
                                user.role ===
                                'admin'
                              }
                              aria-label={`Mobile access for ${
                                user.full_name ||
                                user.username
                              }`}
                              onCheckedChange={(
                                checked
                              ) =>
                                updateAccess(
                                  user,
                                  {
                                    accessMobile:
                                      checked ===
                                      true,
                                  }
                                )
                              }
                            />
                          </TableCell>

                          <TableCell>
                            <Checkbox
                              checked={
                                Boolean(
                                  user.is_active
                                )
                              }
                              disabled={
                                user.role ===
                                'admin'
                              }
                              aria-label={`Active status for ${
                                user.full_name ||
                                user.username
                              }`}
                              onCheckedChange={(
                                checked
                              ) =>
                                updateAccess(
                                  user,
                                  {
                                    isActive:
                                      checked ===
                                      true,
                                  }
                                )
                              }
                            />
                          </TableCell>

                          <TableCell>
                            {user.confirmed_at ? (
                              <Badge>
                                Confirmed
                              </Badge>
                            ) : (
                              <Badge variant="outline">
                                Pending
                              </Badge>
                            )}
                          </TableCell>

                          <TableCell>
                            {isKeycloakAuth ? (
                              user.keycloak_user_id ? (
                                <Badge
                                  variant="outline"
                                  className="border-green-300 text-green-700 dark:text-green-400"
                                >
                                  Linked
                                </Badge>
                              ) : (
                                <Badge
                                  variant="outline"
                                  className="border-amber-300 text-amber-700 dark:text-amber-400"
                                >
                                  Not linked
                                </Badge>
                              )
                            ) : (
                              <Badge variant="secondary">
                                Local
                              </Badge>
                            )}
                          </TableCell>

                          <TableCell>
                            {user.role !== 'admin' ? (
                              <div className="flex flex-wrap justify-end gap-2">
                                <Button
                                  type="button"
                                  variant="outline"
                                  size="sm"
                                  onClick={() =>
                                    handleEditUser(
                                      user
                                    )
                                  }
                                >
                                  Edit
                                </Button>

                                {passwordResetEnabled &&
                                  isKeycloakAuth &&
                                  user.keycloak_user_id &&
                                  user.is_active && (
                                    <Button
                                      type="button"
                                      variant="outline"
                                      size="sm"
                                      onClick={() =>
                                        handleResetKeycloakPassword(
                                          user
                                        )
                                      }
                                    >
                                      Reset Password
                                    </Button>
                                  )}

                                <AlertDialog>
                                  <AlertDialogTrigger
                                    render={
                                      <Button
                                        type="button"
                                        variant="destructive"
                                        size="sm"
                                      />
                                    }
                                  >
                                    Delete
                                  </AlertDialogTrigger>

                                  <AlertDialogContent>
                                    <AlertDialogHeader>
                                      <AlertDialogTitle>
                                        Delete user?
                                      </AlertDialogTitle>

                                      <AlertDialogDescription>
                                        This will permanently delete{' '}
                                        <strong>
                                          {user.full_name ||
                                            user.username}
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
                                        onClick={() =>
                                          handleDeleteUser(
                                            user
                                          )
                                        }
                                      >
                                        Delete User
                                      </AlertDialogAction>
                                    </AlertDialogFooter>
                                  </AlertDialogContent>
                                </AlertDialog>
                              </div>
                            ) : (
                              <span className="text-xs text-muted-foreground">
                                Protected
                              </span>
                            )}
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </>
      )}
    </section>
  )
}

export default UserManagementPage
