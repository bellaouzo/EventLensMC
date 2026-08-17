import { escapeHtml, simpleEventName } from './format';

export type SessionStateTone = 'active' | 'stopped' | 'warn';

export interface SessionStateInfo {
  label: string;
  shortLabel: string;
  tone: SessionStateTone;
  isActive: boolean;
}

export function isLiveSessionState(state: string): boolean {
  return state === 'ACTIVE' || state === 'THROTTLED';
}

export function describeSessionState(state: string): SessionStateInfo {
  switch (state) {
    case 'ACTIVE':
      return { label: 'Active', shortLabel: 'ACTIVE', tone: 'active', isActive: true };
    case 'THROTTLED':
      return { label: 'Throttled', shortLabel: 'THROTTLED', tone: 'warn', isActive: true };
    case 'STOPPED':
      return { label: 'Stopped', shortLabel: 'STOPPED', tone: 'stopped', isActive: false };
    case 'FULL':
      return { label: 'Full', shortLabel: 'FULL', tone: 'stopped', isActive: false };
    case 'EXPIRED':
      return { label: 'Expired', shortLabel: 'EXPIRED', tone: 'stopped', isActive: false };
    case 'ABANDONED':
      return { label: 'Abandoned', shortLabel: 'ABANDONED', tone: 'stopped', isActive: false };
    default:
      return { label: state, shortLabel: state, tone: 'stopped', isActive: false };
  }
}

export function sessionOptionClass(state: string): string {
  const tone = describeSessionState(state).tone;
  if (tone === 'active') {
    return 'session-option-active';
  }
  if (tone === 'warn') {
    return 'session-option-warn';
  }
  return 'session-option-stopped';
}

export function sessionSelectClass(state: string | null | undefined): string {
  if (!state) {
    return '';
  }
  const tone = describeSessionState(state).tone;
  if (tone === 'active') {
    return 'session-select-active';
  }
  if (tone === 'warn') {
    return 'session-select-warn';
  }
  return 'session-select-stopped';
}

export function formatSessionOptionText(
  sessionId: string,
  eventClassName: string,
  capturedEvents: number,
  state: string,
): string {
  const info = describeSessionState(state);
  const simple = simpleEventName(eventClassName);
  return `${sessionId} · ${simple} (${capturedEvents}) · ${info.shortLabel}`;
}

export function sessionStateBannerHtml(state: string): string {
  const info = describeSessionState(state);
  if (info.tone !== 'stopped') {
    return '';
  }

  const message = 'This trace has ended. You are viewing a frozen snapshot — data will not update.';

  return `
    <div class="page-notice page-notice-stopped" role="status">
      <span class="page-notice-dot"></span>
      <span class="page-notice-label">${escapeHtml(info.shortLabel)}</span>
      <span class="page-notice-text">${escapeHtml(message)}</span>
    </div>`;
}

export function traceStatusIndicatorHtml(state: string | null, streamConnected: boolean): string {
  const refreshLabel = streamConnected ? 'live stream' : 'refreshes every 2s';
  const info = state ? describeSessionState(state) : null;

  if (!info || info.isActive) {
    const throttlePill =
      state === 'THROTTLED' ? '<span class="trace-throttle-pill">throttled</span>' : '';
    return `
      <div id="trace-status-indicator" class="trace-status-group trace-status-live">
        <span class="trace-status-dot"></span>
        <span class="trace-status-label">LIVE</span>
        ${throttlePill}
        <span class="status-muted">· ${escapeHtml(refreshLabel)}</span>
      </div>`;
  }

  return `
    <div id="trace-status-indicator" class="trace-status-group trace-status-stopped">
      <span class="trace-status-dot"></span>
      <span class="trace-status-label">${escapeHtml(info.shortLabel)}</span>
      <span class="status-muted">· trace ended</span>
    </div>`;
}
