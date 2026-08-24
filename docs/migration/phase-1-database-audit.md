# FieldSync Database Migration Audit

## 1. Authoritative Source

The current running Supabase PostgreSQL `public` schema is treated
as the authoritative database structure for the migration.

Schema snapshot:

database/baseline/supabase-public-schema.sql

The snapshot contains schema definitions only and does not contain
application row data.


## 2. Existing Database Representations

The repository currently contains multiple representations:

- database/baseline/supabase-public-schema.sql
- backend-api/prisma/schema.prisma
- database/schema.sql
- database/migrations/
- backend-api/sql/rls_phase_1.sql

These representations must be compared before Spring Boot JPA
entities or Flyway migrations are created.


## 3. Expected Core Tables

- tenants
- users
- customers
- locations
- categories
- captured_records
- captured_images


## 4. Migration Target

The application PostgreSQL database will eventually run in the
FieldSync Docker environment.

Spring Boot will manage versioned database changes using Flyway.

Keycloak will continue using a separate PostgreSQL database.


## 5. Tenant Requirements

Application business data must remain tenant scoped.

The backend must derive the trusted tenant from the authenticated
FieldSync user.

Client-provided tenant identifiers must not determine authorization.


## 6. Migration Rules

- Do not modify the current Supabase production schema during audit.
- Do not migrate production data during Phase 1.
- Treat the live schema snapshot as the current database reference.
- Do not use database/schema.sql as the sole source of truth.
- Compare the live schema against Prisma.
- Preserve foreign keys and indexes required by the current system.
- Preserve tenant_id relationships.
- Review RLS separately before expanding it.
- Do not merge the Keycloak database with the FieldSync database.
- Do not commit database credentials or application data.
- Create Flyway migrations only after the schema comparison is complete.