# Stops a stuck EventLens dev Paper server and releases run/ file locks on Windows.
param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$patterns = @(
    "run-task-jars[/\\]paper",
    "org\.bukkit\.craftbukkit\.Main",
    "io\.papermc\.paperclip",
    "paper-26\.2"
)

$excludePatterns = @(
    "GradleDaemon",
    "gradle\.launcher\.daemon",
    "GradleWrapperMain",
    "gradlew",
    "org\.gradle",
    "fml\.modFolders",
    "forgeclientdev",
    "net\.neoforged",
    "net\.fabricmc",
    "FabricLoader",
    "runClient"
)

function Get-EventLensPaperProcesses {
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
        $commandLine = $_.CommandLine
        if (-not $commandLine) { return $false }
        foreach ($exclude in $excludePatterns) {
            if ($commandLine -match $exclude) { return $false }
        }
        foreach ($pattern in $patterns) {
            if ($commandLine -match $pattern) { return $true }
        }
        return $false
    }
}

$javaProcesses = Get-EventLensPaperProcesses

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

$remaining = Get-EventLensPaperProcesses

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
