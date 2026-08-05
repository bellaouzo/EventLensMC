param(
    [switch]$SkipCheck,
    [switch]$SkipManualServer
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$javaHome = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
if ($javaHome) {
    $env:JAVA_HOME = $javaHome
    $env:Path = "$env:JAVA_HOME\bin;" + $env:Path
}

$gradlew = Join-Path $repoRoot "gradlew.bat"

if (-not $SkipCheck) {
    Write-Host "Running full check..."
    & $gradlew check --warning-mode all | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle check failed with exit code $LASTEXITCODE"
    }
}

Write-Host "Running automated paper smoke test..."
& $gradlew paperSmokeTest --warning-mode all | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw "paperSmokeTest failed with exit code $LASTEXITCODE"
}

Write-Host ""
Write-Host "Automated smoke test passed."
Write-Host "Log inspected: run\logs\latest.log"
Write-Host ""
Write-Host "Manual follow-up checklist:"
Write-Host "  1) .\gradlew.bat runServer"
Write-Host "  2) Run: eventlens status"
Write-Host "  3) Run: eventlens listeners BlockBreakEvent"
Write-Host "  4) Run: eventlens trace start BlockBreakEvent --max-events 50 --max-duration 60s"
Write-Host "  5) Run: eventlens trace list"
Write-Host "  6) Run: eventlens trace stop"
Write-Host "  7) Stop cleanly with: stop"
Write-Host ""

if (-not $SkipManualServer) {
    Write-Host "Starting Paper server for manual verification..."
    & $gradlew runServer
}
