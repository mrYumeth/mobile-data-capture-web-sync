# =========================================================
# FieldSync Local OpenBao Agent
# =========================================================

pid_file = "/tmp/fieldsync-openbao-agent.pid"


# =========================================================
# OpenBao Server
# =========================================================

vault {
  address = "http://openbao:8200"
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

      secret_id_file_path = "/openbao/credentials/secret_id"

      remove_secret_id_file_after_reading = false
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

  contents = "{{ with secret \"secret/fieldsync/local/verification\" }}{{ .Data.data.application }}|{{ .Data.data.environment }}{{ end }}"

  destination = "/openbao/rendered/verification.txt"

  error_on_missing_key = true
}

# =========================================================
# Spring Datasource Configuration
# =========================================================

template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-datasource\" }}jdbc:postgresql://localhost:5434/{{ .Data.data.database }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.datasource.url"

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-datasource\" }}{{ .Data.data.username }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.datasource.username"

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-datasource\" }}{{ .Data.data.password }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.datasource.password"

  error_on_missing_key = true
}


# =========================================================
# Spring Flyway Configuration
# =========================================================

template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-flyway\" }}jdbc:postgresql://localhost:5434/{{ .Data.data.database }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.flyway.url"

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-flyway\" }}{{ .Data.data.username }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.flyway.user"

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-flyway\" }}{{ .Data.data.password }}{{ end }}"

  destination = "/openbao/rendered/spring/spring.flyway.password"

  error_on_missing_key = true
}

# =========================================================
# Spring Image Storage Configuration
# =========================================================

template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-storage\" }}{{ .Data.data.url }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.storage.supabase.url"

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-storage\" }}{{ .Data.data.service_role_key }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.storage.supabase.service-role-key"

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-storage\" }}{{ .Data.data.bucket }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.storage.supabase.bucket"

  error_on_missing_key = true
}


template {

  contents = "{{ with secret \"secret/fieldsync/local/spring-storage\" }}{{ .Data.data.signed_url_expires_in }}{{ end }}"

  destination = "/openbao/rendered/spring/fieldsync.storage.supabase.signed-url-expires-in"

  error_on_missing_key = true
}