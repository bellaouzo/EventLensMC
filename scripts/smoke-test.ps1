# Optional helper: build the plugin, print a manual smoke-test checklist, then start Paper.
param(
    [switch]$SkipBuild
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

Write-Host ""
Write-Host "=== EventLens manual smoke test ==="
Write-Host ""
Write-Host "Automated checks (run before starting the server):"
Write-Host "  .\gradlew.bat check --warning-mode all"
Write-Host "  (includes file length limits, SonarLint, Spotless, tests)"
Write-Host ""
Write-Host "When the server shows 'Done', run these in the server console:"
Write-Host "  eventlens status"
Write-Host "  eventlens listeners PlayerJoinEvent"
Write-Host "  eventlens listeners BlockBreakEvent"
Write-Host "  eventlens trace start BlockBreakEvent --max-events 50 --max-duration 60s"
Write-Host "  eventlens trace list"
Write-Host "  eventlens trace view <sessionId> --unchanged"
Write-Host "  eventlens trace stop"
Write-Host ""
Write-Host "In-game (op): same commands with a leading slash, e.g. /eventlens status"
Write-Host "Tab completion: /eventlens <tab> then /eventlens listeners <tab>"
Write-Host ""
Write-Host "Expected:"
Write-Host "  - status shows version, platform, tracing flag, session count"
Write-Host "  - listeners shows event class, paginated rows with plugin, class, method,"
Write-Host "    priority, registration order (#N), and ignoreCancelled"
Write-Host "  - trace start rejects unsupported events (e.g. PlayerJoinEvent)"
Write-Host "  - trace start accepts supported events (BlockBreakEvent, PlayerInteractEvent, ...)"
Write-Host ""
Write-Host "Finish with: stop"
Write-Host "If runServer fails with 'Unable to delete latest.log' or world lock errors,"
Write-Host "run: .\scripts\stop-server.ps1 -Force"
Write-Host "Then confirm run\logs\latest.log has no stack traces and 'EventLens disabled'."
Write-Host ""

if (-not $SkipBuild) {
    Write-Host "Running automated checks..."
    & $gradlew check --warning-mode all | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle check failed with exit code $LASTEXITCODE"
    }
}

Write-Host "Starting Paper dev server..."
& $gradlew runServer
