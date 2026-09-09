# FieldSync Production OpenBao Security

## Production AppRole Bootstrap

FieldSync production uses a dedicated AppRole named:

`fieldsync-spring-prod`

The application must never receive or store a long-lived raw SecretID.

The trusted deployment process generates a SecretID using OpenBao response wrapping:

```bash
bao write \
  -wrap-ttl=5m \
  -f \
  auth/approle/role/fieldsync-spring-prod/secret-id