# FieldSync Local Development Environment

This directory contains the supporting infrastructure used during
the FieldSync architecture migration.

## Services

| Service | Local Port | Purpose |
|---|---:|---|
| FieldSync PostgreSQL | 5434 | Application database |
| Keycloak PostgreSQL | 5435 | Keycloak IAM database |
| Keycloak | 8080 | Authentication and identity management |
| OpenBao | 8200 | Local secret management |

The application PostgreSQL database and Keycloak PostgreSQL database
must remain separate.

## Initial Setup

From dev-environment:

1. Copy the environment template:

   Copy-Item .env.example .env

2. Replace placeholder values in `.env` with local credentials.

3. Prepare the sanitized local Keycloak realm:

   .\scripts\prepare-keycloak-realm.ps1

4. Start the infrastructure:

   docker compose up -d

5. Initialize OpenBao:

   .\scripts\init-openbao.ps1

## Verify Containers

Run:

docker compose ps

Expected services:

- fieldsync-postgres
- fieldsync-keycloak-postgres
- fieldsync-keycloak
- fieldsync-openbao

## Verify Keycloak

Open:

http://localhost:8080/admin/

OIDC discovery:

http://localhost:8080/realms/fieldsync/.well-known/openid-configuration

The local realm must contain:

- fieldsync-web
- fieldsync-mobile
- fieldsync-backend-admin

## Verify OpenBao

Run:

Invoke-RestMethod http://localhost:8200/v1/sys/health

Expected:

- initialized = true
- sealed = false

The Spring application will use AppRole authentication rather than
the OpenBao root token.

## Important Development Note

OpenBao currently runs in development mode.

Its policies, AppRoles, and secrets are temporary and can be lost
when the OpenBao container/process is recreated or restarted.

Run:

.\scripts\init-openbao.ps1

to recreate the local FieldSync OpenBao configuration.

Development mode must not be used in production.

## Security

Never commit:

- .env
- database passwords
- OpenBao tokens
- OpenBao Secret IDs
- Keycloak client secrets
- SMTP passwords
- Supabase service-role credentials

The committed `.env.example` must contain placeholders only.