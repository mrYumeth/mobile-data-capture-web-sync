# FieldSync Spring production runtime policy.
# Only the exact production secrets required by Spring may be read.

path "secret/data/fieldsync/prod/verification" {
  capabilities = ["read"]
}

path "secret/data/fieldsync/prod/spring-datasource" {
  capabilities = ["read"]
}

path "secret/data/fieldsync/prod/spring-flyway" {
  capabilities = ["read"]
}

path "secret/data/fieldsync/prod/spring-storage" {
  capabilities = ["read"]
}

path "secret/data/fieldsync/prod/keycloak-admin" {
  capabilities = ["read"]
}