# FieldSync Current-System Audit

## 1. Migration Baseline

Source branch:
feature/keycloak-iam

Migration branch:
feature/fieldsync-architecture-migration

Existing backend:
Node.js + Express

Target backend:
Java 21 + Spring Boot

Web:
React + JavaScript + Vite

Mobile:
Flutter

IAM:
Keycloak

Current application database:
Supabase PostgreSQL

Target application database:
Containerized PostgreSQL

Image storage:
Supabase Storage retained during initial migration

Email:
Brevo

Secret management target:
OpenBao


## 2. Authentication Flow

React / Flutter
→ Keycloak
→ Access Token
→ Backend token validation
→ Resolve Keycloak user
→ Resolve application user
→ Resolve trusted tenant
→ Check role/application access
→ Execute tenant-scoped operation


## 3. Core Data Model

- tenants
- users
- customers
- locations
- categories
- captured_records
- captured_images


## 4. API Groups

- Authentication
- Tenant registration
- User administration
- Customers
- Locations
- Categories
- Captured records
- Image upload/access
- Mobile master-data synchronization
- Mobile record synchronization


## 5. Tenant Isolation

Tenant identity is resolved by the backend from the
authenticated application user.

Client supplied tenant identifiers must not be trusted.

Business data queries must always be tenant scoped.


## 6. Mobile Synchronization

Master data:
GET customers
GET locations
GET categories
→ SQLite

Captured data:
Pending SQLite record
→ POST /api/captured-records
→ image upload
→ server record ID
→ mark local record Synced


## 7. External Services

Keycloak:
Authentication and user management

Supabase:
Current PostgreSQL + captured-image Storage

Brevo:
Transactional/application email


## 8. Migration Rules

- Keep Node backend operational during migration.
- Preserve existing API contracts wherever practical.
- Preserve React UI behaviour.
- Preserve Flutter offline behaviour.
- Preserve tenant isolation.
- Preserve Supabase Storage initially.
- Do not trust client tenant IDs.
- Do not store passwords in application PostgreSQL.
- Move runtime secrets to OpenBao.
- Do not retire Node until Spring Boot reaches functional parity.