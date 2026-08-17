import type { DashboardSession, DashboardStatus, TraceReport } from './types';
import { icons } from './utils/icons';
import {
  describeSessionState,
  formatSessionOptionText,
  sessionOptionClass,
  sessionSelectClass,
  traceStatusIndicatorHtml,
} from './utils/sessionState';

export type ViewId = 'overview' | 'timeline' | 'flame' | 'events' | 'plugins';

export interface ShellContext {
  activeView: ViewId;
  liveMode: boolean;
  streamConnected: boolean;
  agentPresent: boolean;
  protocolVersion: number;
  paperVersion: string;
  serverStatus: DashboardStatus | null;
  report: TraceReport | null;
  sessionState: string | null;
  selectedSessionId: string;
  onNavigate: (view: ViewId) => void;
  onSessionChange: (sessionId: string) => void;
  onReportChange: (fileName: string) => void;
  onFileLoad: (file: File) => void;
}

const NAV: Array<{ id: ViewId; label: string; icon: string }> = [
  { id: 'overview', label: 'Overview', icon: icons.overview },
  { id: 'timeline', label: 'Timeline', icon: icons.timeline },
  { id: 'flame', label: 'Flame graph', icon: icons.flame },
  { id: 'events', label: 'Event graph', icon: icons.events },
  { id: 'plugins', label: 'Plugin graph', icon: icons.plugins },
];

export function renderShell(app: HTMLElement, ctx: ShellContext): void {
  const sessionId = resolveSessionLabel(ctx);
  const sessionState = ctx.sessionState;
  const worldLine = resolveWorldLine(ctx);
  const agentLabel = ctx.agentPresent ? `agent v${ctx.protocolVersion}` : 'agent absent';
  const tps = ctx.serverStatus?.tps;
  const tpsLabel = tps != null ? tps.toFixed(1) : '—';
  const tpsClass = tps != null && tps >= 18 ? 'tps-ok' : 'status-highlight';

  app.className = 'app-shell';
  app.innerHTML = `
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">EL</div>
        <div class="brand-text">
          <span class="brand-name">EventLens</span>
          <span class="brand-sub">DIAGNOSTICS</span>
        </div>
      </div>
      <nav class="sidebar-nav">
        ${NAV.map(
          (item) => `
          <button type="button" class="nav-item${ctx.activeView === item.id ? ' active' : ''}" data-view="${item.id}">
            <span class="nav-icon">${item.icon}</span>
            <span>${item.label}</span>
          </button>`,
        ).join('')}
      </nav>
      <div class="sidebar-footer">
        <span>${escapeAttr(ctx.paperVersion)}</span><br>
        <span>${escapeAttr(agentLabel)}</span>
      </div>
    </aside>
    <div class="main-column">
      <header class="status-bar">
        <div class="status-left">
          ${
            ctx.liveMode
              ? `${traceStatusIndicatorHtml(sessionState, ctx.streamConnected)}<div class="status-divider"></div>`
              : `<span class="status-muted">Offline report</span><div class="status-divider"></div>`
          }
          <div class="status-mono">session <span class="status-highlight">${escapeHtml(sessionId)}</span></div>
          <div class="status-mono">world <span class="status-highlight">${escapeHtml(worldLine)}</span></div>
        </div>
        <div class="status-right">
          <div class="status-mono">TPS <span id="status-tps" class="${tpsClass}">${escapeHtml(tpsLabel)}</span></div>
          <div class="status-mono">tick budget <span class="status-highlight">50ms</span></div>
          <span class="agent-pill${ctx.agentPresent ? '' : ' absent'}">${escapeHtml(agentLabel)}</span>
        </div>
      </header>
      ${
        ctx.liveMode
          ? `<div class="source-bar">
              <select id="session-select" class="source-select ${sessionSelectClass(sessionState)}">
                <option value="">Live session…</option>
              </select>
              <select id="report-select" class="source-select">
                <option value="">Saved report…</option>
              </select>
            </div>`
          : `<div class="source-bar">
              <label class="file-upload">
                <input type="file" id="file-input" accept=".json,application/json" />
                Load trace report JSON
              </label>
            </div>`
      }
      <main id="view-root" class="view-root"></main>
    </div>
  `;

  app.querySelectorAll<HTMLButtonElement>('.nav-item[data-view]').forEach((button) => {
    button.addEventListener('click', () => {
      ctx.onNavigate(button.dataset.view as ViewId);
    });
  });

  if (ctx.liveMode) {
    app.querySelector<HTMLSelectElement>('#session-select')?.addEventListener('change', (event) => {
      const value = (event.target as HTMLSelectElement).value;
      if (value) {
        ctx.onSessionChange(value);
      }
    });
    app.querySelector<HTMLSelectElement>('#report-select')?.addEventListener('change', (event) => {
      const value = (event.target as HTMLSelectElement).value;
      if (value) {
        ctx.onReportChange(value);
      }
    });
  } else {
    app.querySelector<HTMLInputElement>('#file-input')?.addEventListener('change', async (event) => {
      const input = event.target as HTMLInputElement;
      const file = input.files?.[0];
      if (file) {
        ctx.onFileLoad(file);
      }
    });
  }
}

export function updateSessionCaptureCount(
  app: HTMLElement,
  sessionId: string,
  capturedEvents: number,
  eventClassName: string,
  state?: string,
): void {
  const sessionSelect = app.querySelector<HTMLSelectElement>('#session-select');
  if (!sessionSelect) {
    return;
  }
  for (const option of sessionSelect.options) {
    if (option.value !== sessionId) {
      continue;
    }
    const optionState = state ?? option.dataset.state ?? 'ACTIVE';
    option.className = sessionOptionClass(optionState);
    option.dataset.state = optionState;
    option.textContent = formatSessionOptionText(sessionId, eventClassName, capturedEvents, optionState);
    break;
  }
}

export function setSessionSelectValue(app: HTMLElement, sessionId: string, reportFile: string): void {
  const sessionSelect = app.querySelector<HTMLSelectElement>('#session-select');
  const reportSelect = app.querySelector<HTMLSelectElement>('#report-select');
  if (sessionSelect && sessionId) {
    sessionSelect.value = sessionId;
  }
  if (reportSelect && reportFile) {
    reportSelect.value = reportFile;
  } else if (reportSelect && sessionId) {
    reportSelect.value = '';
  }
}

export function updateLiveStatusLabel(
  app: HTMLElement,
  streamConnected: boolean,
  sessionState: string | null,
): void {
  const indicator = app.querySelector('#trace-status-indicator');
  if (indicator) {
    indicator.outerHTML = traceStatusIndicatorHtml(sessionState, streamConnected);
    return;
  }
  const label = app.querySelector('.trace-status-group .status-muted');
  if (label) {
    label.textContent =
      sessionState && !describeSessionState(sessionState).isActive
        ? '· trace ended'
        : `· ${streamConnected ? 'live stream' : 'refreshes every 2s'}`;
  }
}

export function updateSessionOptions(
  app: HTMLElement,
  sessions: DashboardSession[],
  reports: Array<{ fileName: string }>,
  selectedSessionId: string,
  selectedReport: string,
  selectedSessionState?: string | null,
): void {
  const sessionSelect = app.querySelector<HTMLSelectElement>('#session-select');
  const reportSelect = app.querySelector<HTMLSelectElement>('#report-select');
  if (!sessionSelect || !reportSelect) {
    return;
  }
  const previousSession = sessionSelect.value;
  const previousReport = reportSelect.value;
  sessionSelect.innerHTML =
    '<option value="">Live session…</option>' +
    sessions
      .map((s) => {
        const optionClass = sessionOptionClass(s.state);
        const label = formatSessionOptionText(s.sessionId, s.eventClassName, s.capturedEvents, s.state);
        return `<option value="${escapeAttr(s.sessionId)}" class="${optionClass}" data-state="${escapeAttr(s.state)}">${escapeHtml(label)}</option>`;
      })
      .join('');
  reportSelect.innerHTML =
    '<option value="">Saved report…</option>' +
    reports
      .map((r) => `<option value="${escapeAttr(r.fileName)}">${escapeHtml(r.fileName)}</option>`)
      .join('');
  const nextSession = selectedSessionId || previousSession;
  const nextReport = selectedReport || previousReport;
  if (nextSession && sessions.some((s) => s.sessionId === nextSession)) {
    sessionSelect.value = nextSession;
  }
  const activeState =
    selectedSessionState ??
    sessions.find((s) => s.sessionId === sessionSelect.value)?.state ??
    null;
  sessionSelect.classList.remove('session-select-active', 'session-select-warn', 'session-select-stopped');
  const selectClass = sessionSelectClass(activeState);
  if (selectClass) {
    sessionSelect.classList.add(selectClass);
  }
  if (nextReport && reports.some((r) => r.fileName === nextReport)) {
    reportSelect.value = nextReport;
  } else if (nextSession) {
    reportSelect.value = '';
  }
}

export function updateStatusBar(app: HTMLElement, ctx: ShellContext): void {
  const sessionId = resolveSessionLabel(ctx);
  const sessionState = ctx.sessionState;
  const worldLine = resolveWorldLine(ctx);
  const agentLabel = ctx.agentPresent ? `agent v${ctx.protocolVersion}` : 'agent absent';
  const tps = ctx.serverStatus?.tps;
  const tpsLabel = tps != null ? tps.toFixed(1) : '—';
  const tpsClass = tps != null && tps >= 18 ? 'tps-ok' : 'status-highlight';

  const statusLeft = app.querySelector('.status-left');
  const existingIndicator = statusLeft?.querySelector('.trace-status-group');
  if (statusLeft && ctx.liveMode) {
    const indicatorHtml = traceStatusIndicatorHtml(sessionState, ctx.streamConnected);
    if (existingIndicator) {
      existingIndicator.outerHTML = indicatorHtml;
    }
  }

  const sessionSelect = app.querySelector<HTMLSelectElement>('#session-select');
  if (sessionSelect) {
    sessionSelect.classList.remove('session-select-active', 'session-select-warn', 'session-select-stopped');
    const selectClass = sessionSelectClass(sessionState);
    if (selectClass) {
      sessionSelect.classList.add(selectClass);
    }
  }

  const sessionHighlight = app.querySelector('.status-left .status-highlight');
  if (sessionHighlight) {
    sessionHighlight.textContent = sessionId;
  }
  const worldHighlights = app.querySelectorAll('.status-left .status-highlight');
  if (worldHighlights.length >= 2) {
    worldHighlights[1].textContent = worldLine;
  }
  const tpsElement = app.querySelector('#status-tps');
  if (tpsElement) {
    tpsElement.textContent = tpsLabel;
    tpsElement.classList.toggle('tps-ok', tps != null && tps >= 18);
    tpsElement.classList.toggle('status-highlight', tps == null || tps < 18);
  }
  const agentPill = app.querySelector('.agent-pill');
  if (agentPill) {
    agentPill.textContent = agentLabel;
    agentPill.classList.toggle('absent', !ctx.agentPresent);
  }

  const sidebarFooter = app.querySelector('.sidebar-footer');
  if (sidebarFooter) {
    sidebarFooter.innerHTML = `<span>${escapeHtml(ctx.paperVersion)}</span><br><span>${escapeHtml(agentLabel)}</span>`;
  }
}

function resolveSessionLabel(ctx: ShellContext): string {
  return (
    ctx.report?.session.sessionId ??
    ctx.selectedSessionId ??
    ctx.serverStatus?.activeTraceSessionId ??
    '—'
  );
}

function resolveWorldLine(ctx: ShellContext): string {
  const dispatchWorld = ctx.report?.dispatches.find((dispatch) => dispatch.worldName)?.worldName;
  if (dispatchWorld) {
    return dispatchWorld;
  }
  const status = ctx.serverStatus;
  if (!status?.defaultWorldName) {
    return '—';
  }
  return `${status.defaultWorldName} · ${status.defaultGameMode} · ${status.onlinePlayers} players`;
}

function escapeHtml(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;');
}

function escapeAttr(value: string): string {
  return escapeHtml(value).replaceAll("'", '&#39;');
}
