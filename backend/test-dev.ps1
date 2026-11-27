param([string]$TestName)
$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Missing $envFile. Copy .env.example to .env first."
}

foreach ($line in Get-Content -LiteralPath $envFile) {
    if ($line -match '^\s*([^#=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1].Trim(), $Matches[2].Trim(), 'Process')
    }
}

$driveLetter = @('Z', 'Y', 'X', 'W', 'V') | Where-Object { -not (Get-PSDrive $_ -ErrorAction SilentlyContinue) } | Select-Object -First 1
if (-not $driveLetter) { throw 'No free drive letter is available for Maven.' }
$drive = "${driveLetter}:"
& subst $drive $projectRoot
if ($LASTEXITCODE -ne 0) { throw "Could not map project to $drive." }

try {
    Set-Location "$drive\backend"
    if ($TestName) {
        & mvn "-Dtest=$TestName" test
    } else {
        & mvn test
    }
    exit $LASTEXITCODE
}
finally {
    & subst $drive /D
}
