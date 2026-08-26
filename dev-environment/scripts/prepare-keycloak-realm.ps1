$ErrorActionPreference = "Stop"

Write-Host "Preparing local FieldSync Keycloak realm..."

$devEnvironmentPath = Split-Path $PSScriptRoot -Parent
$repoRoot = Split-Path $devEnvironmentPath -Parent

$sourcePath = Join-Path `
    $repoRoot `
    "keycloak\realm-export\fieldsync-realm-template.json"

$targetDirectory = Join-Path `
    $devEnvironmentPath `
    "keycloak-import"

$targetPath = Join-Path `
    $targetDirectory `
    "fieldsync-realm.json"


if (-not (Test-Path $sourcePath)) {
    throw "FieldSync realm template was not found."
}

New-Item `
    -ItemType Directory `
    -Force `
    -Path $targetDirectory | Out-Null


# ---------------------------------------------------------
# Read canonical FieldSync realm
# ---------------------------------------------------------

$realm = Get-Content `
    $sourcePath `
    -Raw |
    ConvertFrom-Json


if ($realm.realm -ne "fieldsync") {
    throw "Expected the fieldsync realm."
}


# ---------------------------------------------------------
# Remove environment-specific SMTP configuration
# ---------------------------------------------------------

$realm.PSObject.Properties.Remove("smtpServer")


# ---------------------------------------------------------
# Sanitize backend service-account client
# ---------------------------------------------------------

$backendClients = @(
    $realm.clients |
    Where-Object {
        $_.clientId -eq "fieldsync-backend-admin"
    }
)

if ($backendClients.Count -ne 1) {
    throw "Expected exactly one fieldsync-backend-admin client."
}

$backendClient = $backendClients[0]

# Never import an exported or masked client secret.
$backendClient.PSObject.Properties.Remove("secret")

# Service account does not require browser redirects.
$backendClient.redirectUris = @()
$backendClient.webOrigins = @()

if ($null -ne $backendClient.attributes) {
    $backendClient.attributes.PSObject.Properties.Remove(
        "client.secret.creation.time"
    )
}


# ---------------------------------------------------------
# Verify required FieldSync clients
# ---------------------------------------------------------

$requiredClients = @(
    "fieldsync-web",
    "fieldsync-mobile",
    "fieldsync-backend-admin"
)

foreach ($requiredClient in $requiredClients) {

    $client = $realm.clients |
        Where-Object {
            $_.clientId -eq $requiredClient
        }

    if ($null -eq $client) {
        throw "Required Keycloak client missing: $requiredClient"
    }
}


# ---------------------------------------------------------
# Write sanitized local realm
# ---------------------------------------------------------

$json = $realm |
    ConvertTo-Json -Depth 100

$utf8NoBom = New-Object `
    System.Text.UTF8Encoding($false)

[System.IO.File]::WriteAllText(
    $targetPath,
    $json,
    $utf8NoBom
)

Write-Host ""
Write-Host "Local FieldSync realm prepared successfully."
Write-Host "Generated:"
Write-Host $targetPath