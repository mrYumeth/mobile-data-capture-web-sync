# Used only by the trusted deployment/bootstrap process.
# It may create a SecretID, but response wrapping is mandatory.

path "auth/approle/role/fieldsync-spring-prod/secret-id" {
  capabilities = ["create", "update"]

  min_wrapping_ttl = "1s"
  max_wrapping_ttl = "5m"
}