# EventLens Privacy

EventLens collects **local diagnostic data** that can still be sensitive. Defaults favor redaction and opt-in disclosure.

## Default posture

- No external telemetry
- No network export unless explicitly configured in a future release
- Shareable exports redact by default

## Do not collect or export by default

- IP addresses
- Passwords, tokens, private keys
- Webhook URLs and database credentials
- Chat, books, signs, private messages
- Full command arguments
- Arbitrary plugin configuration
- Environment or system-property dumps

## Sensitive fields (redact in shareable exports)

- Player names and raw UUIDs
- World names and exact coordinates
- Server hostnames and identifiers
- Local file paths
- Command arguments and message text

Administrators may opt in to richer exports when a future export command supports it.

## Safe by default (when implemented)

- Event class name
- Plugin name
- Listener priority
- Cancellation boolean
- Bounded durations
- Enum values and namespaced keys
- Generalized location when configured

## Metrics and logging

Do not use player names, UUIDs, coordinates, trace IDs, or free-form text as metric labels.

Do not log every listener invocation at INFO.

Rate-limit repeated warnings.

## File output (future exports)

- Write only under the plugin data directory unless explicitly configured
- Prevent path traversal
- Use safe generated file names
- Prefer temp file + atomic move
- Do not silently overwrite existing exports

## Current status

`/eventlens status` exposes only version, platform, tracing flag, and session count. No export or snapshot capture exists yet.
