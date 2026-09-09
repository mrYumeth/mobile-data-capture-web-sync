# =========================================================
# FieldSync Local OpenBao Agent
# =========================================================

pid_file = "/tmp/fieldsync-openbao-agent.pid"


# =========================================================
# OpenBao Server
# =========================================================

vault {
  # Production address is supplied through VAULT_ADDR.
  # It must use HTTPS.
  retry {
    num_retries = 5
  }
}

# =========================================================
# AppRole Auto Authentication
# =========================================================

auto_auth {

  method {
    type = "approle"

    mount_path = "auth/approle"

    config = {
      role_id_file_path = "/openbao/credentials/role_id"

      # This file contains a short-lived response-wrapping token,
      # not the raw SecretID.
      secret_id_file_path = "/openbao/credentials/secret_id_wrapped"

      secret_id_response_wrapping_path =
      "auth/approle/role/fieldsync-spring-prod/secret-id"

      # Remove the bootstrap credential immediately after reading it.
      remove_secret_id_file_after_reading = true
    }
  }
}


# =========================================================
# Secret Rendering
# =========================================================

template_config {
  static_secret_render_interval = "30s"

  exit_on_retry_failure = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/prod/verification\" }}{{ .Data.data.application }}|{{ .Data.data.environment }}{{ end }}"

  destination = "/openbao/rendered/verification.txt"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}

# =========================================================
# Spring Datasource Configuration
# =========================================================

template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-datasource\" }}jdbc:postgresql://localhost:5434/{{ .Data.data.database }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.datasource.url"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-datasource\" }}{{ .Data.data.username }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.datasource.username"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-datasource\" }}{{ .Data.data.password }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.datasource.password"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


# =========================================================
# Spring Flyway Configuration
# =========================================================

template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-flyway\" }}jdbc:postgresql://localhost:5434/{{ .Data.data.database }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.flyway.url"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-flyway\" }}{{ .Data.data.username }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.flyway.user"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-flyway\" }}{{ .Data.data.password }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.flyway.password"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}

# =========================================================
# Spring Image Storage Configuration
# =========================================================

template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-storage\" }}{{ .Data.data.url }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.storage.supabase.url"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-storage\" }}{{ .Data.data.service_role_key }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.storage.supabase.service-role-key"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-storage\" }}{{ .Data.data.bucket }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.storage.supabase.bucket"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/prod/spring-storage\" }}{{ .Data.data.signed_url_expires_in }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.storage.supabase.signed-url-expires-in"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}


# =========================================================
# Spring Keycloak Admin API Configuration
# =========================================================

template {

  contents = "{{ with secret \"secret/fieldsync/prod/keycloak-admin\" }}{{ .Data.data.client_secret }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.keycloak.admin.client-secret"

  perms = "0600"
  backup = false

  error_on_missing_key = true
}