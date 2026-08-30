$ErrorActionPreference = "Stop"

Write-Host "Initializing FieldSync local OpenBao..."

# ---------------------------------------------------------
# Paths
# ---------------------------------------------------------

$devEnvironmentPath = Split-Path $PSScriptRoot -Parent

$envPath = Join-Path $devEnvironmentPath ".env"

$policyPath = Join-Path `
    $devEnvironmentPath `
    "openbao\policies\fieldsync-spring-read.hcl"


if (-not (Test-Path $envPath)) {
    throw ".env file was not found."
}

if (-not (Test-Path $policyPath)) {
    throw "OpenBao policy file was not found."
}


# ---------------------------------------------------------
# Read root token without displaying it
# ---------------------------------------------------------

$rootTokenLine = Get-Content $envPath |
    Where-Object {
        $_ -match '^OPENBAO_DEV_ROOT_TOKEN='
    } |
    Select-Object -First 1

if (-not $rootTokenLine) {
    throw "OPENBAO_DEV_ROOT_TOKEN is missing from .env"
}

$rootToken = $rootTokenLine.Substring(
    $rootTokenLine.IndexOf("=") + 1
)

if ([string]::IsNullOrWhiteSpace($rootToken)) {
    throw "OPENBAO_DEV_ROOT_TOKEN is empty."
}


# ---------------------------------------------------------
# Verify OpenBao
# ---------------------------------------------------------

docker exec `
    -e BAO_TOKEN=$rootToken `
    fieldsync-openbao `
    bao status | Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "OpenBao is not available."
}

Write-Host "OpenBao is available."


# ---------------------------------------------------------
# Check KV secrets engine
# ---------------------------------------------------------

$secretMountJson = docker exec `
    -e BAO_TOKEN=$rootToken `
    fieldsync-openbao `
    bao secrets list -format=json

$secretMounts = $secretMountJson |
    ConvertFrom-Json

$secretProperty = $secretMounts.PSObject.Properties["secret/"]

if ($null -eq $secretProperty) {

    Write-Host "Creating KV v2 secrets engine..."

    docker exec `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao secrets enable `
        -path=secret `
        kv-v2 | Out-Null

    if ($LASTEXITCODE -ne 0) {
        throw "Unable to enable KV v2 secrets engine."
    }
}
else {

    $secretVersion = $secretProperty.Value.options.version

    if ($secretVersion -ne "2") {
        throw "The secret/ mount exists but is not KV version 2."
    }

    Write-Host "KV v2 secrets engine already available."
}


# ---------------------------------------------------------
# Enable AppRole authentication
# ---------------------------------------------------------

$authJson = docker exec `
    -e BAO_TOKEN=$rootToken `
    fieldsync-openbao `
    bao auth list -format=json

$authMethods = $authJson |
    ConvertFrom-Json

if (
    $authMethods.PSObject.Properties.Name `
        -notcontains "approle/"
) {

    Write-Host "Enabling AppRole authentication..."

    docker exec `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao auth enable approle | Out-Null

    if ($LASTEXITCODE -ne 0) {
        throw "Unable to enable AppRole."
    }
}
else {
    Write-Host "AppRole authentication already enabled."
}


# ---------------------------------------------------------
# Install FieldSync Spring policy
# ---------------------------------------------------------

Write-Host "Installing FieldSync Spring policy..."

Get-Content $policyPath -Raw |
    docker exec `
        -i `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao policy write `
        fieldsync-spring-read `
        - | Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "Unable to create the FieldSync Spring policy."
}


# ---------------------------------------------------------
# Create/configure Spring AppRole
# ---------------------------------------------------------

Write-Host "Configuring FieldSync Spring AppRole..."

docker exec `
    -e BAO_TOKEN=$rootToken `
    fieldsync-openbao `
    bao write `
    auth/approle/role/fieldsync-spring `
    token_policies="fieldsync-spring-read" `
    token_ttl="1h" `
    token_max_ttl="4h" `
    secret_id_ttl="24h" `
    secret_id_num_uses=0 | Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "Unable to configure FieldSync Spring AppRole."
}

# ---------------------------------------------------------
# Read FieldSync database credentials
# ---------------------------------------------------------

function Get-RequiredEnvValue {

    param (
        [string]$Key
    )

    $line = Get-Content $envPath |
        Where-Object {
            $_ -match "^$([regex]::Escape($Key))="
        } |
        Select-Object -First 1

    if (-not $line) {
        throw "$Key is missing from .env"
    }

    $value = $line.Substring(
        $line.IndexOf("=") + 1
    )

    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "$Key is empty in .env"
    }

    return $value
}

function Get-OptionalEnvValue {

    param (
        [string]$Key,
        [string]$DefaultValue = ""
    )

    $line =
        Get-Content $envPath |
        Where-Object {
            $_ -match "^$([regex]::Escape($Key))="
        } |
        Select-Object -First 1

    if (-not $line) {
        return $DefaultValue
    }

    $value =
        $line.Substring(
            $line.IndexOf("=") + 1
        )

    if ([string]::IsNullOrWhiteSpace($value)) {
        return $DefaultValue
    }

    return $value
}


$fieldsyncDbName =
    Get-RequiredEnvValue "FIELDSYNC_DB_NAME"

$fieldsyncDbUser =
    Get-RequiredEnvValue "FIELDSYNC_DB_USER"

$fieldsyncDbPassword =
    Get-RequiredEnvValue "FIELDSYNC_DB_PASSWORD"

$fieldsyncRuntimeDbUser =
    Get-RequiredEnvValue "FIELDSYNC_RUNTIME_DB_USER"

$fieldsyncRuntimeDbPassword =
    Get-RequiredEnvValue "FIELDSYNC_RUNTIME_DB_PASSWORD"

$supabaseUrl =
    Get-OptionalEnvValue `
        "SUPABASE_URL"

$supabaseServiceRoleKey =
    Get-OptionalEnvValue `
        "SUPABASE_SERVICE_ROLE_KEY"

$supabaseStorageBucket =
    Get-OptionalEnvValue `
        "SUPABASE_STORAGE_BUCKET" `
        "captured-images"

$supabaseSignedUrlExpiresIn =
    Get-OptionalEnvValue `
        "SUPABASE_SIGNED_URL_EXPIRES_IN" `
        "3600"

$keycloakAdminClientSecret =
    Get-RequiredEnvValue `
        "KEYCLOAK_ADMIN_CLIENT_SECRET"

$supabaseSignedUrlExpiresIn =
    $supabaseSignedUrlExpiresIn.Trim()

$parsedSignedUrlExpiresIn = 0

if (
    -not [int]::TryParse(
        $supabaseSignedUrlExpiresIn,
        [ref]$parsedSignedUrlExpiresIn
    ) -or
    $parsedSignedUrlExpiresIn -le 0
) {

    throw "SUPABASE_SIGNED_URL_EXPIRES_IN must be a positive integer."
}

$supabaseSignedUrlExpiresIn =
    $parsedSignedUrlExpiresIn.ToString()


# ---------------------------------------------------------
# Store Spring datasource credentials in OpenBao
# ---------------------------------------------------------

Write-Host "Creating Spring datasource secret..."

$datasourceSecretJson = @{
    database = $fieldsyncDbName
    username = $fieldsyncRuntimeDbUser
    password = $fieldsyncRuntimeDbPassword
} |
    ConvertTo-Json -Compress


$datasourceSecretJson |
    docker exec `
        -i `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao kv put `
        secret/fieldsync/local/spring-datasource `
        - |
    Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "Unable to create Spring datasource secret."
}

Write-Host "Spring datasource secret created."

# ---------------------------------------------------------
# Store Flyway migration credentials in OpenBao
# ---------------------------------------------------------

Write-Host "Creating Spring Flyway secret..."

$flywaySecretJson = @{
    database = $fieldsyncDbName
    username = $fieldsyncDbUser
    password = $fieldsyncDbPassword
} |
    ConvertTo-Json -Compress


$flywaySecretJson |
    docker exec `
        -i `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao kv put `
        secret/fieldsync/local/spring-flyway `
        - |
    Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "Unable to create Spring Flyway secret."
}

Write-Host "Spring Flyway secret created."


# ---------------------------------------------------------
# Store Spring image storage configuration in OpenBao
# ---------------------------------------------------------

Write-Host "Creating Spring image storage secret..."

$storageSecretJson = @{

    url =
        $supabaseUrl

    service_role_key =
        $supabaseServiceRoleKey

    bucket =
        $supabaseStorageBucket

    signed_url_expires_in =
        $supabaseSignedUrlExpiresIn

} |
    ConvertTo-Json -Compress


$storageSecretJson |
    docker exec `
        -i `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao kv put `
        secret/fieldsync/local/spring-storage `
        - |
    Out-Null


if ($LASTEXITCODE -ne 0) {

    throw "Unable to create Spring image storage secret."
}


Write-Host "Spring image storage secret created."


# ---------------------------------------------------------
# Store Keycloak Admin API client secret in OpenBao
# ---------------------------------------------------------

Write-Host "Creating Keycloak Admin API secret..."

$keycloakAdminSecretJson = @{
    client_secret = $keycloakAdminClientSecret
} |
    ConvertTo-Json -Compress


$keycloakAdminSecretJson |
    docker exec `
        -i `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao kv put `
        secret/fieldsync/local/keycloak-admin `
        - |
    Out-Null


if ($LASTEXITCODE -ne 0) {
    throw "Unable to create Keycloak Admin API secret."
}

Write-Host "Keycloak Admin API secret created."


# ---------------------------------------------------------
# Create non-sensitive verification secret
# ---------------------------------------------------------

Write-Host "Creating verification secret..."

docker exec `
    -e BAO_TOKEN=$rootToken `
    fieldsync-openbao `
    bao kv put `
    secret/fieldsync/local/verification `
    application=fieldsync `
    environment=local | Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "Unable to create verification secret."
}


# ---------------------------------------------------------
# Generate Role ID
# ---------------------------------------------------------

$roleId = (
    docker exec `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao read `
        -field=role_id `
        auth/approle/role/fieldsync-spring/role-id
).Trim()

if ([string]::IsNullOrWhiteSpace($roleId)) {
    throw "Unable to obtain AppRole Role ID."
}


# ---------------------------------------------------------
# Generate Secret ID
# ---------------------------------------------------------

$secretId = (
    docker exec `
        -e BAO_TOKEN=$rootToken `
        fieldsync-openbao `
        bao write `
        -f `
        -field=secret_id `
        auth/approle/role/fieldsync-spring/secret-id
).Trim()

if ([string]::IsNullOrWhiteSpace($secretId)) {
    throw "Unable to obtain AppRole Secret ID."
}


# ---------------------------------------------------------
# Helper for updating .env safely
# ---------------------------------------------------------

function Set-EnvValue {

    param (
        [string]$Path,
        [string]$Key,
        [string]$Value
    )

    $lines = Get-Content $Path

    $found = $false

    $updatedLines = foreach ($line in $lines) {

        if (
            $line -match "^$([regex]::Escape($Key))="
        ) {

            $found = $true

            "$Key=$Value"
        }
        else {
            $line
        }
    }

    if (-not $found) {
        $updatedLines += "$Key=$Value"
    }

    $utf8NoBom = New-Object `
        System.Text.UTF8Encoding($false)

    [System.IO.File]::WriteAllLines(
        $Path,
        $updatedLines,
        $utf8NoBom
    )
}


# ---------------------------------------------------------
# Store credentials locally
# ---------------------------------------------------------

Set-EnvValue `
    -Path $envPath `
    -Key "OPENBAO_ROLE_ID" `
    -Value $roleId

Set-EnvValue `
    -Path $envPath `
    -Key "OPENBAO_SECRET_ID" `
    -Value $secretId

# ---------------------------------------------------------
# Create OpenBao Agent credential files
# ---------------------------------------------------------

Write-Host "Preparing OpenBao Agent credentials..."

$agentCredentialDirectory = Join-Path `
    $devEnvironmentPath `
    "openbao\agent-credentials"

New-Item `
    -ItemType Directory `
    -Force `
    -Path $agentCredentialDirectory |
    Out-Null


$roleIdPath = Join-Path `
    $agentCredentialDirectory `
    "role_id"

$secretIdPath = Join-Path `
    $agentCredentialDirectory `
    "secret_id"


$utf8NoBom = New-Object `
    System.Text.UTF8Encoding($false)


[System.IO.File]::WriteAllText(
    $roleIdPath,
    $roleId,
    $utf8NoBom
)

[System.IO.File]::WriteAllText(
    $secretIdPath,
    $secretId,
    $utf8NoBom
)

# ---------------------------------------------------------
# Prepare rendered-secret directory
# ---------------------------------------------------------

$renderedSecretDirectory = Join-Path `
    $devEnvironmentPath `
    "openbao\rendered"

New-Item `
    -ItemType Directory `
    -Force `
    -Path $renderedSecretDirectory |
    Out-Null

Write-Host "OpenBao Agent credential files prepared."


# ---------------------------------------------------------
# Clear sensitive variables
# ---------------------------------------------------------

$rootToken = $null
$secretId = $null

$fieldsyncDbName = $null
$fieldsyncDbUser = $null
$fieldsyncDbPassword = $null

$fieldsyncRuntimeDbUser = $null
$fieldsyncRuntimeDbPassword = $null
$flywaySecretJson = $null

$datasourceSecretJson = $null

$keycloakAdminClientSecret = $null
$keycloakAdminSecretJson = $null


Write-Host ""
Write-Host "FieldSync OpenBao initialization completed."
Write-Host "AppRole credentials were stored in dev-environment/.env."
Write-Host "No credentials were printed."  

