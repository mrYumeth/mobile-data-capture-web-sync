# FieldSync Schema Comparison

## 1. Authoritative Schema

The schema snapshot:

database/baseline/supabase-public-schema.sql

is treated as the authoritative schema reference for the migration.


## 2. Core Tables

The live application database contains:

- tenants
- users
- customers
- locations
- categories
- captured_records
- captured_images


## 3. Prisma Comparison

backend-api/prisma/schema.prisma broadly represents the current
multi-tenant business model.

However, the following live database features are not completely
represented in Prisma:

- users.keycloak_user_id
- unique partial index on keycloak_user_id
- case-insensitive username unique index
- case-insensitive email unique index
- PostgreSQL Row Level Security configuration
- tenant RLS policies
- app_current_tenant_id() database function

Therefore Prisma must not be treated as the sole source for the
Spring Boot database model.


## 4. Legacy schema.sql Comparison

database/schema.sql is outdated.

It does not represent:

- tenants
- tenant_id ownership
- tenant foreign keys
- user email/access configuration
- Keycloak user linking
- password setup/confirmation fields
- tenant indexes
- current unique indexes
- RLS
- tenant context function

database/schema.sql must not be used to create the Spring Boot
database.


## 5. Current RLS State

The live database has RLS enabled on:

- tenants
- users
- customers
- locations
- categories
- captured_records
- captured_images

Tenant isolation policies currently exist on:

- customers
- locations
- categories
- captured_records
- captured_images

The policies compare tenant_id with:

app_current_tenant_id()

The Node backend currently sets:

app.current_tenant_id

inside a database transaction.


## 6. Important Indexes

The Spring/PostgreSQL target must preserve relevant indexes including:

- tenants.slug unique
- users.username unique
- case-insensitive username uniqueness
- case-insensitive email uniqueness
- unique keycloak_user_id where non-null
- users tenant index
- customers tenant index
- locations tenant index
- categories tenant index
- captured records tenant indexes
- captured images tenant index


## 7. Foreign Keys

The target schema must preserve:

- users -> tenants
- users.created_by -> users
- customers -> tenants
- locations -> tenants
- categories -> tenants
- captured_records -> tenants
- captured_records -> customers
- captured_records -> locations
- captured_records -> categories
- captured_images -> tenants
- captured_images -> captured_records

Captured image deletion must retain the existing cascade behaviour
from captured_records to captured_images.


## 8. User Authentication Columns

The live users table still contains legacy password-management fields:

- password_hash
- password_change_required
- confirmation_token
- confirmation_expires_at
- confirmed_at

These columns will initially remain during migration to preserve
database compatibility.

Spring Boot must not introduce a second password authentication
system.

Keycloak remains the authentication authority.

Legacy password fields can only be removed in a later migration
after the Keycloak-only cutover has been verified.


## 9. Flyway Strategy

Do not directly convert the raw Supabase dump into a Flyway migration.

Recommended migration structure:

V1 - Core FieldSync schema
- tables
- primary keys
- foreign keys
- sequences/identity
- indexes
- current compatibility columns

V2 or later - Tenant RLS
- app_current_tenant_id()
- RLS enabling
- tenant policies

RLS should be activated only after Spring Boot reliably establishes
the database tenant context.


## 10. Migration Decisions

- Live Supabase schema is the reference.
- Prisma is a supporting model, not the source of truth.
- database/schema.sql is legacy.
- Existing Node backend remains unchanged during migration.
- Do not remove legacy user columns yet.
- Do not modify production Supabase during this phase.
- Do not enable new RLS behaviour until Spring tenant handling is ready.
- Keycloak database remains separate from FieldSync database.