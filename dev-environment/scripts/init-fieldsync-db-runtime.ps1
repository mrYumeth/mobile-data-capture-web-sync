$ErrorActionPreference = "Stop"

Write-Host "Initializing FieldSync PostgreSQL runtime role..."

$devEnvironmentPath =
    Split-Path $PSScriptRoot -Parent

$envPath =
    Join-Path $devEnvironmentPath ".env"

if (-not (Test-Path $envPath)) {
    throw ".env file was not found."
}


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

    $value =
        $line.Substring(
            $line.IndexOf("=") + 1
        )

    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "$Key is empty in .env"
    }

    return $value
}


$dbName =
    Get-RequiredEnvValue "FIELDSYNC_DB_NAME"

$dbAdminUser =
    Get-RequiredEnvValue "FIELDSYNC_DB_USER"

$dbAdminPassword =
    Get-RequiredEnvValue "FIELDSYNC_DB_PASSWORD"

$runtimeUser =
    Get-RequiredEnvValue "FIELDSYNC_RUNTIME_DB_USER"

$runtimePassword =
    Get-RequiredEnvValue "FIELDSYNC_RUNTIME_DB_PASSWORD"


# Only allow normal PostgreSQL identifiers here.
foreach (
    $identifier in @(
        $dbName,
        $dbAdminUser,
        $runtimeUser
    )
) {

    if (
        $identifier -notmatch
        '^[a-z_][a-z0-9_]*$'
    ) {
        throw "Invalid PostgreSQL identifier: $identifier"
    }
}


$runtimePasswordSql =
    $runtimePassword.Replace(
        "'",
        "''"
    )


$sql = @"
DO `$do`$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = '$runtimeUser'
    ) THEN

        CREATE ROLE $runtimeUser
            LOGIN
            PASSWORD '$runtimePasswordSql'
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOINHERIT
            NOBYPASSRLS;

    ELSE

        ALTER ROLE $runtimeUser
            WITH LOGIN
            PASSWORD '$runtimePasswordSql'
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOINHERIT
            NOBYPASSRLS;

    END IF;

END
`$do`$;


GRANT CONNECT
ON DATABASE $dbName
TO $runtimeUser;


GRANT USAGE
ON SCHEMA public
TO $runtimeUser;


GRANT
    SELECT,
    INSERT,
    UPDATE,
    DELETE
ON ALL TABLES
IN SCHEMA public
TO $runtimeUser;


GRANT
    USAGE,
    SELECT
ON ALL SEQUENCES
IN SCHEMA public
TO $runtimeUser;


GRANT EXECUTE
ON ALL FUNCTIONS
IN SCHEMA public
TO $runtimeUser;


ALTER DEFAULT PRIVILEGES
FOR ROLE $dbAdminUser
IN SCHEMA public
GRANT
    SELECT,
    INSERT,
    UPDATE,
    DELETE
ON TABLES
TO $runtimeUser;


ALTER DEFAULT PRIVILEGES
FOR ROLE $dbAdminUser
IN SCHEMA public
GRANT
    USAGE,
    SELECT
ON SEQUENCES
TO $runtimeUser;


ALTER DEFAULT PRIVILEGES
FOR ROLE $dbAdminUser
IN SCHEMA public
GRANT EXECUTE
ON FUNCTIONS
TO $runtimeUser;
"@


$sql |
    docker exec `
        -i `
        -e PGPASSWORD=$dbAdminPassword `
        fieldsync-postgres `
        psql `
        -X `
        -v ON_ERROR_STOP=1 `
        -U $dbAdminUser `
        -d $dbName |
    Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "Unable to configure FieldSync runtime role."
}


$status = (
    docker exec `
        -e PGPASSWORD=$dbAdminPassword `
        fieldsync-postgres `
        psql `
        -X `
        -A `
        -t `
        -U $dbAdminUser `
        -d $dbName `
        -c "SELECT rolname || '|' || rolsuper || '|' || rolbypassrls FROM pg_roles WHERE rolname = '$runtimeUser';"
).Trim()


if ($status -ne "$runtimeUser|false|false") {
    throw "Runtime role security verification failed."
}


Write-Host "FieldSync runtime role configured."
Write-Host "Runtime role is not superuser and cannot bypass RLS."


$dbAdminPassword = $null
$runtimePassword = $null
$runtimePasswordSql = $null
$sql = $null