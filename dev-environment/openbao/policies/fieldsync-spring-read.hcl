# FieldSync Spring API - local development secret access

# Allow Spring to read FieldSync local KV v2 secrets.
path "secret/data/fieldsync/local/*" {
  capabilities = ["read"]
}

# Allow metadata access required for KV v2 operations.
path "secret/metadata/fieldsync/local/*" {
  capabilities = ["read", "list"]
}