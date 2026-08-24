# FieldSync Existing API Contract Inventory

## 1. Purpose

This document records the existing Node/Express API contracts that
must be preserved or intentionally replaced during migration to
Spring Boot.

Current backend:
Node.js + Express

Target backend:
Java 21 + Spring Boot

Migration rule:
Do not switch React or Flutter to Spring Boot until the required
endpoint produces compatible behaviour.


## 2. Authentication Endpoints

| Method | Endpoint | Authentication | Current Purpose | Spring Target |
|---|---|---|---|---|
| GET | / | Public | Backend status message | Health/System Controller |
| GET | /health | Public | Health check | Actuator or Health Controller |
| POST | /api/auth/login | Public | Legacy local login | Compatibility only / retire |
| POST | /api/auth/register-tenant | Public | Create tenant + first admin | TenantProvisioningController |
| POST | /api/auth/setup-password | Public | Legacy local password setup | Compatibility only / retire |
| POST | /api/auth/change-password | Authenticated | Legacy password change | Compatibility only / retire |
| GET | /api/auth/me | Bearer JWT | Resolve logged-in FieldSync user | AuthController |


## 3. User Administration Endpoints

| Method | Endpoint | Required Access | Purpose |
|---|---|---|---|
| GET | /api/admin/users | Admin | List tenant users |
| POST | /api/admin/users | Admin | Create application + Keycloak user |
| PATCH | /api/admin/users/:id | Admin | Update user |
| DELETE | /api/admin/users/:id | Admin | Delete user |
| PATCH | /api/admin/users/:id/access | Admin | Change Web/Mobile access |
| POST | /api/admin/users/:id/reset-keycloak-password | Admin | Trigger Keycloak password reset |


## 4. Customer Endpoints

| Method | Endpoint | Required Access | Tenant Scoped |
|---|---|---|---|
| GET | /api/customers | Authenticated | Yes |
| POST | /api/customers | Web access/Admin | Yes |
| PUT | /api/customers/:id | Web access/Admin | Yes |
| DELETE | /api/customers/:id | Admin | Yes |

Customer fields currently include:

- id
- tenant_id
- name
- phone
- email
- address
- is_active
- created_at
- updated_at


## 5. Location Endpoints

| Method | Endpoint | Required Access | Tenant Scoped |
|---|---|---|---|
| GET | /api/locations | Authenticated | Yes |
| POST | /api/locations | Web access/Admin | Yes |
| PUT | /api/locations/:id | Web access/Admin | Yes |
| DELETE | /api/locations/:id | Admin | Yes |


## 6. Category Endpoints

| Method | Endpoint | Required Access | Tenant Scoped |
|---|---|---|---|
| GET | /api/categories | Authenticated | Yes |
| POST | /api/categories | Web access/Admin | Yes |
| PUT | /api/categories/:id | Web access/Admin | Yes |
| DELETE | /api/categories/:id | Admin | Yes |


## 7. Captured Record Endpoints

| Method | Endpoint | Required Access | Purpose |
|---|---|---|---|
| GET | /api/captured-records | Authenticated | List tenant records |
| GET | /api/captured-records/:id | Authenticated | Retrieve one record |
| POST | /api/captured-records | Authenticated | Upload field record and images |

POST /api/captured-records uses multipart/form-data.

Fields:

- customer_id
- location_id
- category_id
- description
- latitude
- longitude
- captured_at
- images
- image (legacy compatibility)

Maximum multiple images:
10

Successful creation returns conceptually:

{
  "message": "Captured record created successfully",
  "record": {
    "id": ...
  }
}


## 8. Trusted Authentication Context

Existing behaviour:

Keycloak access token
→ validate token
→ obtain Keycloak subject (sub)
→ resolve application user
→ resolve trusted tenant_id
→ check active account
→ determine Web/Mobile access
→ expose authenticated application user

Required Spring behaviour:

JWT
→ Spring Security
→ Keycloak JWKS validation
→ resolve application user
→ establish TenantContext
→ role/access authorization
→ tenant-scoped service/repository operation

Client-supplied tenant IDs must not be trusted for authorization.


## 9. Tenant Registration Contract

POST /api/auth/register-tenant

Input:

{
  "tenantName": "...",
  "tenantSlug": "...",
  "fullName": "...",
  "username": "...",
  "email": "..."
}

With Keycloak enabled, the frontend does not send a password.

Expected process:

1. Validate registration.
2. Check tenant slug.
3. Check username/email.
4. Create tenant.
5. Create Keycloak administrator.
6. Create FieldSync application user.
7. Link Keycloak user ID.
8. Commit application database transaction.
9. Trigger password/invitation flow.
10. Return tenant and user information.

Failure handling must avoid leaving a partial tenant or Keycloak user.


## 10. React API Dependencies

React currently consumes:

- /api/auth/me
- /api/auth/register-tenant
- /api/admin/users
- /api/customers
- /api/locations
- /api/categories
- /api/captured-records

React authentication should continue using Keycloak directly.

React API base URL will eventually change from the Node backend
to the Spring Boot backend.


## 11. Flutter API Dependencies

Flutter uses:

Authentication:
Keycloak → /api/auth/me

Master-data sync:
GET /api/customers
GET /api/locations
GET /api/categories

Record sync:
POST /api/captured-records

The POST captured-record request must remain multipart compatible.

After a successful upload, Flutter expects:

record.id

The returned server ID is stored against the SQLite record and the
local record is marked as synchronized.


## 12. Spring Boot Controller Mapping

| Current Node Area | Proposed Spring Component |
|---|---|
| Authentication | AuthController |
| Tenant registration | TenantProvisioningController |
| User administration | UserAdminController |
| Customers | CustomerController |
| Locations | LocationController |
| Categories | CategoryController |
| Captured records | CapturedRecordController |
| Keycloak administration | KeycloakAdminService |
| Tenant resolution | TenantContext / CurrentUserService |
| Image handling | ImageStorageService |
| Email | EmailService |


## 13. Compatibility Rules

- Preserve HTTP methods.
- Preserve endpoint paths where practical.
- Preserve major request field names.
- Preserve response structures required by React and Flutter.
- Preserve HTTP status behaviour where practical.
- Never accept tenant ownership from the client.
- Keep Keycloak as the authentication authority.
- Keep Supabase Storage during the initial backend migration.
- Keep the existing Node backend available until Spring Boot parity
  has been tested.