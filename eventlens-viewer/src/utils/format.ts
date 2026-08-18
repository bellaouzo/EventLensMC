export function formatNanos(nanos: number): string {
  if (nanos >= 1_000_000) {
    return `${(nanos / 1_000_000).toFixed(2)}ms`;
  }
  if (nanos >= 1_000) {
    return `${(nanos / 1_000).toFixed(1)}µs`;
  }
  return `${nanos}ns`;
}

export function formatMillis(nanos: number): string {
  return `${(nanos / 1_000_000).toFixed(2)} ms`;
}

export function formatUptimeMs(millis: number): string {
  return `${Math.max(0, Math.round(millis))}ms`;
}

export function shortSessionId(sessionId: string): string {
  if (!sessionId || sessionId === '—') {
    return '—';
  }
  return sessionId.length > 10 ? sessionId.slice(0, 8) : sessionId;
}

export function simpleEventName(className: string): string {
  const dot = className.lastIndexOf('.');
  return dot >= 0 ? className.substring(dot + 1) : className;
}

export function formatUptime(millis: number): string {
  const totalSeconds = Math.floor(millis / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  if (hours > 0) {
    return `${hours}h ${minutes}m ${seconds}s`;
  }
  if (minutes > 0) {
    return `${minutes}m ${seconds}s`;
  }
  return `${seconds}s`;
}

export function escapeHtml(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;');
}

export function formatUpdatedAt(date: Date): string {
  return date.toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

export function formatDispatchTime(millis: number): string {
  const date = new Date(millis);
  const base = formatUpdatedAt(date);
  const ms = String(date.getMilliseconds()).padStart(3, '0');
  return `${base}.${ms}`;
}
