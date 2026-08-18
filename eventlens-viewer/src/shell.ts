import type { DashboardSession, DashboardStatus, TraceReport } from './types';
import { escapeHtml, shortSessionId, simpleEventName } from './utils/format';
import { describeSessionState } from './utils/sessionState';

export type ViewId = 'overview' | 'timeline' | 'flame' | 'events' | 'plugins' | 'compare';
export type SourceMode = 'live' | 'offline';

export interface ShellContext {
  activeView: ViewId;
  liveAvailable: boolean;
  sourceMode: SourceMode;
  streamConnected: boolean;
  agentPresent: boolean;
  protocolVersion: number;
  paperVersion: string;
  eventLensVersion: string;
  serverStatus: DashboardStatus | null;
  report: TraceReport | null;
  sessionState: string | null;
  selectedSessionId: string;
  selectedReportFile: string;
  dataSource: 'session' | 'report' | 'offline';
  onNavigate: (view: ViewId) => void;
  onSourceModeChange: (mode: SourceMode) => void;
  onSessionChange: (sessionId: string) => void;
  onReportChange: (fileName: string) => void;
  onFileLoad: (file: File) => void;
}

const NAV: Array<{ id: ViewId; label: string }> = [
  { id: 'overview', label: 'Overview' },
  { id: 'timeline', label: 'Timeline' },
  { id: 'flame', label: 'Flame graph' },
  { id: 'events', label: 'Event graph' },
  { id: 'plugins', label: 'Plugin graph' },
  { id: 'compare', label: 'Compare' },
];

export function renderShell(app: HTMLElement, ctx: ShellContext): void {
  app.className = 'app-shell';
  app.innerHTML = `
    <header class="app-header">
      <div class="brand">
        <div class="brand-row">
          <span class="brand-name">EventLens</span>
          <span class="brand-version" id="brand-version">v${escapeHtml(ctx.eventLensVersion)}</span>
        </div>
        <span class="brand-sub">diagnostics — observer only</span>
      </div>
      <nav class="top-nav">
        ${NAV.map(
          (item) => `
          <button type="button" class="nav-item${ctx.activeView === item.id ? ' active' : ''}" data-view="${item.id}">
            ${item.label}
          </button>`,
        ).join('')}
      </nav>
      <div class="header-session" id="header-session">${headerSessionHtml(ctx)}</div>
    </header>
    <div class="app-body">
      <aside class="sidebar">
        <div class="mode-toggle">
          <button type="button" class="mode-btn${ctx.sourceMode === 'live' ? ' active' : ''}" data-mode="live"${ctx.liveAvailable ? '' : ' disabled'}>Live</button>
          <button type="button" class="mode-btn${ctx.sourceMode === 'offline' ? ' active' : ''}" data-mode="offline">Offline</button>
        </div>
        <section class="sidebar-section">
          <div class="sidebar-heading">Live sessions</div>
          <div id="stream-status">${streamStatusHtml(ctx.streamConnected, ctx.liveAvailable)}</div>
          <div id="session-list" class="session-list"></div>
        </section>
        <section class="sidebar-section">
          <div class="sidebar-heading">Saved reports</div>
          <div id="report-list" class="report-list"></div>
          <label class="file-upload">
            <input type="file" id="file-input" accept=".json,application/json" />
            Load JSON
          </label>
        </section>
        <section class="sidebar-section context-section">
          <div class="sidebar-heading">Context</div>
          <dl id="context-list" class="context-list">${contextRowsHtml(ctx)}</dl>
        </section>
      </aside>
      <main id="view-root" class="view-root"></main>
    </div>
  `;

  app.querySelectorAll<HTMLButtonElement>('.nav-item[data-view]').forEach((button) => {
    button.addEventListener('click', () => {
      ctx.onNavigate(button.dataset.view as ViewId);
    });
  });

  app.querySelectorAll<HTMLButtonElement>('.mode-btn[data-mode]').forEach((button) => {
    button.addEventListener('click', () => {
      if (button.disabled) {
        return;
      }
      ctx.onSourceModeChange(button.dataset.mode as SourceMode);
    });
  });

  app.querySelector('#session-list')?.addEventListener('click', (event) => {
    const card = (event.target as HTMLElement).closest<HTMLElement>('[data-session-id]');
    if (card?.dataset.sessionId) {
      ctx.onSessionChange(card.dataset.sessionId);
    }
  });

  app.querySelector('#report-list')?.addEventListener('click', (event) => {
    const item = (event.target as HTMLElement).closest<HTMLElement>('[data-report]');
    if (item?.dataset.report) {
      ctx.onReportChange(item.dataset.report);
    }
  });

  app.querySelector<HTMLInputElement>('#file-input')?.addEventListener('change', (event) => {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      ctx.onFileLoad(file);
    }
  });
}

export function updateSessionCaptureCount(
  app: HTMLElement,
  sessionId: string,
  capturedEvents: number,
  eventClassName: string,
  state?: string,
): void {
  const card = app.querySelector<HTMLElement>(`[data-session-id="${cssEscape(sessionId)}"]`);
  if (!card) {
    return;
  }
  if (state) {
    card.dataset.state = state;
    const badge = card.querySelector('.session-badge');
    if (badge) {
      const info = describeSessionState(state);
      badge.textContent = info.shortLabel;
      badge.className = `session-badge badge-${info.tone}`;
    }
  }
  const sub = card.querySelector('.session-card-sub');
  if (sub) {
    sub.textContent = `${simpleEventName(eventClassName)} — ${capturedEvents} captured`;
  }
}

export function setSessionSelectValue(app: HTMLElement, sessionId: string, reportFile: string): void {
  app.querySelectorAll<HTMLElement>('[data-session-id]').forEach((card) => {
    card.classList.toggle('selected', card.dataset.sessionId === sessionId);
  });
  app.querySelectorAll<HTMLElement>('[data-report]').forEach((item) => {
    item.classList.toggle('selected', !!reportFile && item.dataset.report === reportFile);
  });
}

export function updateLiveStatusLabel(app: HTMLElement, streamConnected: boolean, liveAvailable: boolean): void {
  const status = app.querySelector('#stream-status');
  if (status) {
    status.innerHTML = streamStatusHtml(streamConnected, liveAvailable);
  }
}

export function updateSessionOptions(
  app: HTMLElement,
  sessions: DashboardSession[],
  reports: Array<{ fileName: string }>,
  selectedSessionId: string,
  selectedReport: string,
): void {
  const sessionList = app.querySelector('#session-list');
  const reportList = app.querySelector('#report-list');
  if (sessionList) {
    sessionList.innerHTML = sessions.length
      ? sessions
          .map((session) => sessionCardHtml(session, session.sessionId === selectedSessionId && !selectedReport))
          .join('')
      : '<p class="sidebar-empty">No live sessions</p>';
  }
  if (reportList) {
    reportList.innerHTML = reports.length
      ? reports.map((report) => reportItemHtml(report.fileName, report.fileName === selectedReport)).join('')
      : '<p class="sidebar-empty">No saved reports</p>';
  }
}

export function updateSourceMode(app: HTMLElement, mode: SourceMode): void {
  app.querySelectorAll<HTMLButtonElement>('.mode-btn[data-mode]').forEach((button) => {
    button.classList.toggle('active', button.dataset.mode === mode);
  });
}

export function updateStatusBar(app: HTMLElement, ctx: ShellContext): void {
  const header = app.querySelector('#header-session');
  if (header) {
    header.innerHTML = headerSessionHtml(ctx);
  }
  const version = app.querySelector('#brand-version');
  if (version) {
    version.textContent = `v${ctx.eventLensVersion}`;
  }
  const context = app.querySelector('#context-list');
  if (context) {
    context.innerHTML = contextRowsHtml(ctx);
  }
  updateLiveStatusLabel(app, ctx.streamConnected, ctx.liveAvailable);
  updateSourceMode(app, ctx.sourceMode);
  setSessionSelectValue(app, ctx.selectedSessionId, ctx.selectedReportFile);
}

function headerSessionHtml(ctx: ShellContext): string {
  const sessionId = resolveSessionLabel(ctx);
  const state = ctx.sessionState;
  if (!sessionId || sessionId === '—') {
    return '<span class="header-session-id">—</span>';
  }
  const info = state ? describeSessionState(state) : null;
  const badge = info
    ? `<span class="header-state-badge badge-${info.tone}">${escapeHtml(info.shortLabel)}</span>`
    : '';
  return `<span class="header-session-id">${escapeHtml(shortSessionId(sessionId))}</span>${badge}`;
}

function streamStatusHtml(streamConnected: boolean, liveAvailable: boolean): string {
  if (!liveAvailable) {
    return `<div class="stream-status"><span class="stream-dot off"></span>offline viewer</div>`;
  }
  return `<div class="stream-status${streamConnected ? ' connected' : ''}"><span class="stream-dot${streamConnected ? '' : ' off'}"></span>${streamConnected ? 'stream connected' : 'stream disconnected'}</div>`;
}

function sessionCardHtml(session: DashboardSession, selected: boolean): string {
  const info = describeSessionState(session.state);
  return `
    <button type="button" class="session-card${selected ? ' selected' : ''}" data-session-id="${escapeAttr(session.sessionId)}" data-state="${escapeAttr(session.state)}">
      <div class="session-card-top">
        <span class="session-id">${escapeHtml(shortSessionId(session.sessionId))}</span>
        <span class="session-badge badge-${info.tone}">${escapeHtml(info.shortLabel)}</span>
      </div>
      <div class="session-card-sub">${escapeHtml(simpleEventName(session.eventClassName))} — ${session.capturedEvents} captured</div>
    </button>`;
}

function reportItemHtml(fileName: string, selected: boolean): string {
  return `<button type="button" class="report-item${selected ? ' selected' : ''}" data-report="${escapeAttr(fileName)}">${escapeHtml(fileName)}</button>`;
}

function contextRowsHtml(ctx: ShellContext): string {
  const report = ctx.report;
  const status = ctx.serverStatus;
  const session = report?.session;
  const source =
    ctx.dataSource === 'session' ? 'live session' : ctx.dataSource === 'report' ? 'saved report' : 'offline file';
  const world = report?.dispatches.find((dispatch) => dispatch.worldName)?.worldName ?? status?.defaultWorldName ?? '—';
  const tps = status?.tps != null ? status.tps.toFixed(1) : report?.dispatches.find((d) => d.tps != null)?.tps?.toFixed(1) ?? '—';
  const agent = ctx.agentPresent ? 'present' : 'absent';
  const protocol = ctx.protocolVersion > 0 ? `v${ctx.protocolVersion}` : '—';
  const mode = report?.instrumentation?.mode ?? (ctx.agentPresent ? 'precise' : 'dispatch');
  const rows: Array<[string, string]> = [
    ['Source', source],
    ['Session ID', session?.sessionId ?? (ctx.selectedSessionId || '—')],
    ['State', session?.state ?? ctx.sessionState ?? '—'],
    ['Event', session ? simpleEventName(session.eventClassName) : '—'],
    [
      'Captured / dropped / sampled out',
      session ? `${session.capturedEvents} / ${session.droppedEvents} / ${session.sampledOutEvents}` : '—',
    ],
    ['Owner', session?.ownerName || '—'],
    ['Filters', session?.filters || '—'],
    ['World', world],
    ['Game mode', status?.defaultGameMode || '—'],
    ['Players online', status ? String(status.onlinePlayers) : '—'],
    ['Server TPS', tps],
    ['Tick budget', '50 ms'],
    ['Runtime', ctx.paperVersion],
    ['EventLens', `v${ctx.eventLensVersion}`],
    ['Agent', agent],
    ['Protocol', protocol],
    ['Mode', mode],
    ['Redaction', report?.redactionMode || '—'],
  ];
  return rows
    .map(
      ([label, value]) => `
      <div class="context-row">
        <dt>${escapeHtml(label)}</dt>
        <dd>${escapeHtml(value)}</dd>
      </div>`,
    )
    .join('');
}

function resolveSessionLabel(ctx: ShellContext): string {
  return (
    ctx.report?.session.sessionId ??
    ctx.selectedSessionId ??
    ctx.serverStatus?.activeTraceSessionId ??
    '—'
  );
}

function escapeAttr(value: string): string {
  return escapeHtml(value).replaceAll("'", '&#39;');
}

function cssEscape(value: string): string {
  return value.replaceAll('\\', '\\\\').replaceAll('"', '\\"');
}
