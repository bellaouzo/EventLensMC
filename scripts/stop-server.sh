#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

patterns=(
  "run-task-jars/paper"
  "run-task-jars\\paper"
  "org.bukkit.craftbukkit.Main"
  "io.papermc.paperclip"
  "paper-26.2"
)

exclude_patterns=(
  "GradleDaemon"
  "gradle.launcher.daemon"
  "GradleWrapperMain"
  "gradlew"
  "org.gradle"
  "fml.modFolders"
  "forgeclientdev"
  "net.neoforged"
  "net.fabricmc"
  "FabricLoader"
  "runClient"
)

stopped=0
while IFS= read -r pid; do
  if [[ -z "$pid" ]]; then
    continue
  fi
  cmd="$(ps -p "$pid" -o command= 2>/dev/null || true)"
  skip=0
  for exclude in "${exclude_patterns[@]}"; do
    if [[ "$cmd" == *"$exclude"* ]]; then
      skip=1
      break
    fi
  done
  if [[ "$skip" -eq 1 ]]; then
    continue
  fi
  for pattern in "${patterns[@]}"; do
    if [[ "$cmd" == *"$pattern"* ]]; then
      echo "Stopping PID $pid"
      kill "$pid" 2>/dev/null || kill -9 "$pid" 2>/dev/null || true
      stopped=1
      break
    fi
  done
done < <(pgrep -f java || true)

if [[ "$stopped" -eq 1 ]]; then
  sleep 2
fi

find "$repo_root/run" -name session.lock -type f -delete 2>/dev/null || true
echo "Dev server stopped."
