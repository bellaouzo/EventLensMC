# Stops a stuck EventLens dev Paper server and releases run/ file locks on Windows.
param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$patterns = @(
    [regex]::Escape($repoRoot),
    "run-task-jars\\paper",
    "EventLens-0\.1-SNAPSHOT"
)

$javaProcesses = Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
    $commandLine = $_.CommandLine
    if (-not $commandLine) { return $false }
    foreach ($pattern in $patterns) {
        if ($commandLine -match $pattern) { return $true }
    }
    return $false
}

if ($javaProcesses.Count -eq 0) {
    Write-Host "No EventLens dev server Java processes found."
} else {
    foreach ($process in $javaProcesses) {
        $preview = $process.CommandLine
        if ($preview.Length -gt 100) {
            $preview = $preview.Substring(0, 100) + "..."
        }
        Write-Host "Stopping PID $($process.ProcessId): $preview"
        if ($Force) {
            Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
        } else {
            Stop-Process -Id $process.ProcessId -ErrorAction SilentlyContinue
        }
    }
    Start-Sleep -Seconds 2
}

$remaining = Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
    $commandLine = $_.CommandLine
    if (-not $commandLine) { return $false }
    foreach ($pattern in $patterns) {
        if ($commandLine -match $pattern) { return $true }
    }
    return $false
}

if ($remaining.Count -gt 0) {
    Write-Host "Some processes are still running. Re-run with -Force if needed."
    exit 1
}

$lockFiles = Get-ChildItem -Path (Join-Path $repoRoot "run") -Filter "session.lock" -Recurse -ErrorAction SilentlyContinue
foreach ($lockFile in $lockFiles) {
    Write-Host "Removing stale lock: $($lockFile.FullName)"
    Remove-Item -LiteralPath $lockFile.FullName -Force -ErrorAction SilentlyContinue
}

Write-Host "Dev server stopped. You can run .\gradlew.bat runServer again."
