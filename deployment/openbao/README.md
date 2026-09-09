# FieldSync Production OpenBao Security

## Production AppRole Bootstrap

FieldSync production uses a dedicated AppRole:

`fieldsync-spring-prod`

The application must never receive or persist a long-lived raw SecretID.

A trusted deployment process generates the SecretID using OpenBao response wrapping:

```bash
bao write \
  -wrap-ttl=5m \
  -f \
  auth/approle/role/fieldsync-spring-prod/secret-id
```

Only the returned response-wrapping token is delivered to the OpenBao Agent.

The wrapping token is written temporarily to:

`/openbao/credentials/secret_id_wrapped`

The RoleID is supplied separately through:

`/openbao/credentials/role_id`

The wrapping token, SecretID, administrator token, and other OpenBao credentials must never be committed to Git or written to application configuration files.

## Production Credential Restrictions

The production AppRole uses:

- SecretID TTL: 10 minutes
- SecretID uses: 1
- Runtime token TTL: 30 minutes
- Runtime token maximum TTL: 2 hours
- Least-privilege policy: `fieldsync-spring-prod-read`

OpenBao Agent validates the response-wrapping path, unwraps the SecretID, authenticates, and removes the temporary bootstrap credential after reading it.

## Filesystem Security

Production credential and rendered-secret directories must only be accessible by the trusted service account running OpenBao Agent and Spring Boot.

Recommended Linux permissions:

```bash
umask 077

mkdir -p /openbao/credentials
mkdir -p /openbao/rendered/spring

chmod 700 /openbao/credentials
chmod 700 /openbao/rendered
chmod 700 /openbao/rendered/spring
```

Rendered secret files are configured with:

```text
0600
```

This means only the owning service identity can read or modify them.

OpenBao Agent templates also use:

```text
backup = false
```

to avoid keeping additional copies of rendered secrets.

OpenBao Agent and Spring Boot must therefore use the same trusted service identity or another explicitly restricted shared-permission model.

## Production Secret Paths

Production secrets are isolated under:

```text
secret/fieldsync/prod/*
```

The Spring runtime policy grants read access only to the exact production secrets required by the application.

Local development secrets under:

```text
secret/fieldsync/local/*
```

remain separate and must not be reused as the production secret namespace.

## Secret Rotation

When a production credential is rotated:

1. Update the corresponding value in OpenBao.
2. Allow OpenBao Agent to render the updated secret.
3. Verify the rendered file exists with restricted permissions.
4. Perform a controlled Spring Boot restart so Spring ConfigTree loads the new value.
5. Verify application health and required integrations.
6. Revoke or disable the previous external credential where applicable.

Spring Boot must not continue indefinitely using an old database password, Keycloak client secret, API key, or other rotated credential.

## Deployment Requirements

Production deployment must provide:

- HTTPS connectivity to OpenBao through `VAULT_ADDR`
- a trusted mechanism for supplying the RoleID
- a short-lived response-wrapping token for SecretID bootstrap
- one-use SecretIDs
- restricted filesystem permissions
- least-privilege OpenBao policies
- automatic deletion of the bootstrap credential after consumption
- controlled Spring Boot restart after secret rotation

The local OpenBao configuration under `dev-environment` is intentionally less strict for development convenience and must not be reused as the production bootstrap configuration.