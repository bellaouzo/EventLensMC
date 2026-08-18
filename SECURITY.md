# Security

EventLens is an observation-only diagnostics plugin. It must not cancel, reorder, or hide listener exceptions merely to trace.

## Supported versions

| Version | Supported |
|---|---|
| `1.11.x` | Yes |
| `1.10.x-beta` | Fixes land on the latest stable only |

## Reporting a vulnerability

Do not open a public GitHub issue for an exploitable bug.

1. Use [GitHub private vulnerability reporting](https://github.com/bellaouzo/EventLensMC/security/advisories/new) if it is enabled on the repository.
2. Otherwise email the maintainer through the GitHub profile on [bellaouzo](https://github.com/bellaouzo).

Include the EventLens version, Paper (or client loader) version, and a minimal reproduction.

## Defaults that matter

- The live dashboard binds to `127.0.0.1` unless an operator changes `dashboard.bind-address`.
- Shareable exports redact player names, world names, exact coordinates, and paths unless `--full` is used.
- There is no external telemetry.
