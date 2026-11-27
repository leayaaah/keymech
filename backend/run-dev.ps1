$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Missing $envFile. Copy .env.example to .env first."
}

foreach ($line in Get-Content -LiteralPath $envFile) {
    if ($line -match '^\s*([^#=]+)=(.*)$') {
        $name = $Matches[1].Trim()
        $value = $Matches[2].Trim()
        if ($value.Length -ge 2 -and $value.StartsWith('"') -and $value.EndsWith('"')) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
}

if (-not $env:DB_URL) { throw 'DB_URL is required in .env.' }
if (-not $env:DB_USERNAME) { throw 'DB_USERNAME is required in .env.' }
if (-not $env:DB_PASSWORD) { throw 'DB_PASSWORD is required in .env.' }
if (-not $env:MAIL_HOST) { $env:MAIL_HOST = 'localhost' }
if (-not $env:MAIL_PORT) { $env:MAIL_PORT = '1025' }
if (-not $env:MAIL_SMTP_AUTH) { $env:MAIL_SMTP_AUTH = 'false' }
if (-not $env:MAIL_SMTP_STARTTLS) { $env:MAIL_SMTP_STARTTLS = 'false' }
if (-not $env:APP_CHATBOT_MODE) { $env:APP_CHATBOT_MODE = 'rule' }

$driveLetter = @('Z', 'Y', 'X', 'W', 'V') | Where-Object { -not (Get-PSDrive $_ -ErrorAction SilentlyContinue) } | Select-Object -First 1
if (-not $driveLetter) { throw 'No free drive letter is available for Maven.' }
$drive = "${driveLetter}:"
& subst $drive $projectRoot
if ($LASTEXITCODE -ne 0) { throw "Could not map project to $drive." }
try {
    Set-Location "$drive\backend"
    & mvn spring-boot:run
    exit $LASTEXITCODE
}
finally {
    & subst $drive /D
}
