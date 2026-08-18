import './styles.css';
import {
  fetchEventGraph,
  fetchReportFile,
  fetchSessionReport,
  fetchSessions,
  fetchReports,
  fetchStatus,
  isLiveMode,
  parseReportJson,
} from './api';
import { applyLiveSessionUpdate, parseLiveSessionUpdate } from './livePatch';
import { startLiveStream } from './liveStream';
import {
  renderShell,
  setSessionSelectValue,
  updateLiveStatusLabel,
  updateSessionCaptureCount,
  updateSessionOptions,
  updateSourceMode,
  updateStatusBar,
  type ShellContext,
  type SourceMode,
  type ViewId,
} from './shell';
import type { DashboardSession, DashboardStatus, TraceReport } from './types';
import { renderOverview, updateOverviewUptime } from './views/overview';
import { renderTimeline, resetTimelineSelection } from './views/timeline';
import { renderFlameGraph } from './views/flame';
import { renderEventGraph } from './views/eventGraph';
import { renderPluginGraph } from './views/pluginGraph';
import { renderCompare } from './views/compare';
import { liveSessionDuration } from './utils/metrics';
import { isLiveSessionState } from './utils/sessionState';
import { formatUptimeMs } from './utils/format';

const app = document.querySelector<HTMLDivElement>('#app')!;

let currentReport: TraceReport | null = null;
let compareReport: TraceReport | null = null;
let activeView: ViewId = 'overview';
let selectedSessionId = '';
let selectedReportFile = '';
let dataSource: 'session' | 'report' | 'offline' = 'offline';
let userPickedSession = false;
let streamConnected = false;
let stopLiveStream: (() => void) | null = null;
let agentPresent = false;
let protocolVersion = 0;
let paperVersion = 'Paper —';
let eventLensVersion = '1.3.0';
let sourceMode: SourceMode = isLiveMode() ? 'live' : 'offline';
let cachedReports: Array<{ fileName: string }> = [];
let serverStatus: DashboardStatus | null = null;
let shellInitialized = false;
let backgroundSyncTimer: number | null = null;
let serverStatusTimer: number | null = null;
let sessionClockTimer: number | null = null;
let cachedSessions: DashboardSession[] = [];

function trackedSessionId(): string {
  return selectedSessionId || currentReport?.session.sessionId || '';
}

function applyReportMetadata(report: TraceReport): void {
  const env = report.environment;
  if (env) {
    const kind = env.runtimeKind ?? 'paper';
    if (kind === 'paper') {
      paperVersion = env.paperVersion ?? env.platformLabel ?? 'Paper server';
    } else {
      const loader = env.loaderVersion ?? env.platformLabel ?? kind;
      paperVersion = `${kind} client · ${loader}`;
    }
  }
  if (env?.eventLensVersion) {
    eventLensVersion = env.eventLensVersion;
  }
  if (report.instrumentation) {
    agentPresent = report.instrumentation.agentPresent;
    protocolVersion = report.instrumentation.protocolVersion;
  }
}

function applyServerStatus(status: DashboardStatus): void {
  serverStatus = status;
  agentPresent = status.agentPresent;
  protocolVersion = status.protocolVersion;
  if (status.paperVersion) {
    paperVersion = status.paperVersion;
  }
  if (status.eventLensVersion) {
    eventLensVersion = status.eventLensVersion;
  }
  if (
    !selectedSessionId &&
    !userPickedSession &&
    status.activeTraceSessionId &&
    dataSource !== 'report'
  ) {
    selectedSessionId = status.activeTraceSessionId;
  }
}

function resolvedSessionState(): string | null {
  const sessionId = trackedSessionId();
  if (!sessionId) {
    return currentReport?.session.state ?? null;
  }
  return (
    currentReport?.session.sessionId === sessionId
      ? currentReport.session.state
      : null
  ) ?? cachedSessions.find((session) => session.sessionId === sessionId)?.state ?? null;
}

function shellContext(): ShellContext {
  return {
    activeView,
    liveAvailable: isLiveMode(),
    sourceMode,
    streamConnected,
    agentPresent,
    protocolVersion,
    paperVersion,
    eventLensVersion,
    serverStatus,
    report: currentReport,
    sessionState: resolvedSessionState(),
    selectedSessionId,
    selectedReportFile,
    dataSource,
    onNavigate: (view) => {
      activeView = view;
      setActiveNav(view);
      void renderActiveView();
    },
    onSourceModeChange: (mode) => {
      sourceMode = mode;
      updateSourceMode(app, mode);
      if (mode === 'live' && isLiveMode() && !selectedSessionId) {
        void maybeAutoSelectSession(cachedSessions);
      }
      updateStatusBar(app, shellContext());
    },
    onSessionChange: (sessionId) => {
      void selectSession(sessionId, true);
    },
    onReportChange: (fileName) => {
      void selectReport(fileName);
    },
    onFileLoad: (file) => {
      void loadOfflineFile(file);
    },
  };
}

function ensureShell(): void {
  if (shellInitialized) {
    return;
  }
  renderShell(app, shellContext());
  shellInitialized = true;
}

function setActiveNav(view: ViewId): void {
  app.querySelectorAll<HTMLButtonElement>('.nav-item[data-view]').forEach((button) => {
    button.classList.toggle('active', button.dataset.view === view);
  });
}

function applyInstantLiveUpdate(data: Record<string, unknown>): boolean {
  const update = parseLiveSessionUpdate(data);
  if (!update) {
    return false;
  }

  const activeId = trackedSessionId();
  if (activeId && activeId !== update.sessionId) {
    return false;
  }

  if (!selectedSessionId) {
    selectedSessionId = update.sessionId;
    dataSource = 'session';
    setSessionSelectValue(app, update.sessionId, '');
  }

  currentReport = applyLiveSessionUpdate(currentReport, update);
  updateStatusBar(app, shellContext());
  updateSessionCaptureCount(
    app,
    update.sessionId,
    update.capturedEvents,
    update.eventClassName,
    update.state,
  );
  renderActiveView(false);
  scheduleBackgroundSync();
  return true;
}

function scheduleBackgroundSync(): void {
  if (backgroundSyncTimer !== null) {
    window.clearTimeout(backgroundSyncTimer);
  }
  backgroundSyncTimer = window.setTimeout(() => {
    backgroundSyncTimer = null;
    void syncFullReportInBackground();
  }, 400);
}

async function ensureFullSessionReport(): Promise<void> {
  if (dataSource !== 'session' || !trackedSessionId()) {
    return;
  }
  try {
    const report = await fetchSessionReport(trackedSessionId());
    if (trackedSessionId() !== report.session.sessionId) {
      return;
    }
    currentReport = report;
    applyReportMetadata(report);
    updateStatusBar(app, shellContext());
    updateSessionCaptureCount(
      app,
      report.session.sessionId,
      report.session.capturedEvents,
      report.session.eventClassName,
      report.session.state,
    );
  } catch {
    // keep live-patched report
  }
}

function reportNeedsDetail(): boolean {
  if (!currentReport?.dispatches.length) {
    return true;
  }
  return currentReport.dispatches.some(
    (dispatch) => dispatch.listenerTimings.length === 0 && (dispatch.listenerChain?.length ?? 0) === 0,
  );
}

async function syncFullReportInBackground(): Promise<void> {
  if (dataSource !== 'session' || !trackedSessionId()) {
    return;
  }
  try {
    const report = await fetchSessionReport(trackedSessionId());
    if (trackedSessionId() !== report.session.sessionId) {
      return;
    }
    currentReport = report;
    applyReportMetadata(report);
    updateStatusBar(app, shellContext());
    updateSessionCaptureCount(
      app,
      report.session.sessionId,
      report.session.capturedEvents,
      report.session.eventClassName,
      report.session.state,
    );
    renderActiveView(false);
  } catch {
    // keep live-patched report
  }
}

async function refreshSessionOptions(): Promise<DashboardSession[]> {
  const [sessions, reports] = await Promise.all([fetchSessions(), fetchReports()]);
  cachedSessions = sessions;
  cachedReports = reports;
  updateSessionOptions(app, sessions, reports, selectedSessionId, selectedReportFile);
  return sessions;
}

async function selectSession(sessionId: string, userInitiated: boolean): Promise<void> {
  selectedSessionId = sessionId;
  selectedReportFile = '';
  dataSource = 'session';
  sourceMode = 'live';
  userPickedSession = userInitiated;
  resetTimelineSelection();
  setSessionSelectValue(app, sessionId, '');
  currentReport = await fetchSessionReport(sessionId);
  applyReportMetadata(currentReport);
  updateStatusBar(app, shellContext());
  await renderActiveView(false);
}

async function selectReport(fileName: string): Promise<void> {
  selectedReportFile = fileName;
  selectedSessionId = '';
  dataSource = 'report';
  sourceMode = 'offline';
  userPickedSession = false;
  resetTimelineSelection();
  setSessionSelectValue(app, '', fileName);
  currentReport = await fetchReportFile(fileName);
  applyReportMetadata(currentReport);
  updateStatusBar(app, shellContext());
  await renderActiveView(false);
}

async function maybeLoadBundledReport(): Promise<void> {
  if (isLiveMode()) {
    return;
  }
  const injected = (window as Window & { __EVENTLENS_REPORT__?: unknown }).__EVENTLENS_REPORT__;
  if (injected && typeof injected === 'object') {
    currentReport = injected as TraceReport;
    dataSource = 'offline';
    applyReportMetadata(currentReport);
    ensureShell();
    await renderActiveView(false);
    return;
  }
  const query = new URLSearchParams(window.location.search).get('report');
  const url = query && query.length > 0 ? query : './report.json';
  try {
    const response = await fetch(url);
    if (!response.ok) {
      return;
    }
    currentReport = parseReportJson(await response.text());
    dataSource = 'offline';
    applyReportMetadata(currentReport);
    ensureShell();
    await renderActiveView(false);
  } catch {
    // file picker remains available
  }
}

async function loadOfflineFile(file: File): Promise<void> {
  dataSource = 'offline';
  sourceMode = 'offline';
  selectedSessionId = '';
  selectedReportFile = '';
  userPickedSession = false;
  resetTimelineSelection();
  currentReport = parseReportJson(await file.text());
  applyReportMetadata(currentReport);
  activeView = 'overview';
  ensureShell();
  updateStatusBar(app, shellContext());
  setActiveNav(activeView);
  await renderActiveView(false);
}

async function maybeAutoSelectSession(sessions: DashboardSession[]): Promise<boolean> {
  const activeSessions = sessions.filter((session) => session.state === 'ACTIVE');
  if (!activeSessions.length) {
    return false;
  }

  const currentStillActive =
    selectedSessionId !== '' && activeSessions.some((session) => session.sessionId === selectedSessionId);
  if (currentStillActive) {
    return false;
  }

  if (!selectedSessionId || !userPickedSession || dataSource !== 'session') {
    const newest = activeSessions[0];
    await selectSession(newest.sessionId, false);
    return true;
  }
  return false;
}

async function refreshSelectedSessionReport(): Promise<void> {
  if (dataSource !== 'session' || !trackedSessionId()) {
    return;
  }
  currentReport = await fetchSessionReport(trackedSessionId());
  applyReportMetadata(currentReport);
  updateStatusBar(app, shellContext());
  await renderActiveView(false);
}

async function refreshServerStatus(): Promise<void> {
  if (!isLiveMode()) {
    return;
  }
  try {
    const status = await fetchStatus();
    applyServerStatus(status);
    updateStatusBar(app, shellContext());
  } catch {
    // keep previous status
  }
}

function activeSessionStartedAtMillis(): number | null {
  const state = resolvedSessionState();
  if (currentReport && state && isLiveSessionState(state)) {
    return currentReport.session.startedAtMillis;
  }
  if (serverStatus?.activeTraceSessionId && serverStatus.activeTraceStartedAtMillis > 0) {
    const activeId = serverStatus.activeTraceSessionId;
    if (!trackedSessionId() || trackedSessionId() === activeId) {
      return serverStatus.activeTraceStartedAtMillis;
    }
  }
  const listed = cachedSessions.find((session) => session.sessionId === trackedSessionId());
  if (listed && isLiveSessionState(listed.state)) {
    return listed.startedAtMillis;
  }
  return null;
}

function tickSessionClock(): void {
  const startedAtMillis = activeSessionStartedAtMillis();
  if (startedAtMillis == null) {
    return;
  }
  const uptimeMillis = Math.max(0, Date.now() - startedAtMillis);
  const root = app.querySelector<HTMLElement>('#view-root');
  if (root && activeView === 'overview') {
    updateOverviewUptime(root, uptimeMillis);
  }
}

function startSessionClock(): void {
  if (sessionClockTimer !== null) {
    return;
  }
  sessionClockTimer = window.setInterval(tickSessionClock, 1000);
}

function startServerStatusPolling(): void {
  if (serverStatusTimer !== null) {
    return;
  }
  serverStatusTimer = window.setInterval(() => {
    void refreshServerStatus();
  }, 2000);
}

async function refreshLiveData(): Promise<void> {
  if (!isLiveMode()) {
    return;
  }

  await refreshServerStatus();

  const sessions = await refreshSessionOptions();
  const switched = await maybeAutoSelectSession(sessions);
  if (switched) {
    return;
  }

  if (dataSource === 'session' && selectedSessionId) {
    await refreshSelectedSessionReport();
  }
}

function handleStreamEvent(event: string, data: Record<string, unknown>): void {
  if (event === 'session-started') {
    if (applyInstantLiveUpdate(data)) {
      return;
    }
    void (async () => {
      await refreshSessionOptions();
      const sessionId = typeof data.sessionId === 'string' ? data.sessionId : '';
      if (sessionId && (!trackedSessionId() || !userPickedSession)) {
        await selectSession(sessionId, false);
      } else {
        await maybeAutoSelectSession(await fetchSessions());
      }
    })();
    return;
  }

  if (event === 'dispatch') {
    if (applyInstantLiveUpdate(data)) {
      return;
    }
    void refreshSelectedSessionReport();
    return;
  }

  if (event === 'poll') {
    void refreshLiveData();
    return;
  }

  if (event === 'session-stopped') {
    if (applyInstantLiveUpdate(data)) {
      void refreshSessionOptions();
      return;
    }
    void refreshLiveData();
  }
}

async function bootstrapLiveData(): Promise<void> {
  ensureShell();

  if (!isLiveMode()) {
    return;
  }

  await refreshServerStatus();
  updateStatusBar(app, shellContext());
  startServerStatusPolling();
  startSessionClock();

  const sessions = await refreshSessionOptions();
  const reports = await fetchReports();

  if (sessions.length) {
    await maybeAutoSelectSession(sessions);
    if (!selectedSessionId && serverStatus?.activeTraceSessionId) {
      selectedSessionId = serverStatus.activeTraceSessionId;
      dataSource = 'session';
    }
    if (selectedSessionId && !currentReport) {
      try {
        currentReport = await fetchSessionReport(selectedSessionId);
        applyReportMetadata(currentReport);
        setSessionSelectValue(app, selectedSessionId, '');
      } catch {
        currentReport = null;
      }
    }
  } else if (reports.length && reports[0].format === 'json') {
    await selectReport(reports[0].fileName);
  }

  const stream = startLiveStream((event, data) => {
    handleStreamEvent(event, data);
  });
  stopLiveStream = stream.stop;

  const syncStreamLabel = () => {
    const connected = stream.connected();
    if (connected !== streamConnected) {
      streamConnected = connected;
      updateLiveStatusLabel(app, streamConnected, isLiveMode());
    }
  };
  syncStreamLabel();
  window.setInterval(syncStreamLabel, 1000);
}

function renderActiveView(showLoading = true): Promise<void> {
  ensureShell();
  const root = app.querySelector<HTMLDivElement>('#view-root');
  if (!root) {
    return Promise.resolve();
  }

  setActiveNav(activeView);

  if (showLoading && isLiveMode() && dataSource === 'session' && !currentReport) {
    root.innerHTML = '<p class="empty">Refreshing live trace data…</p>';
    return Promise.resolve();
  }

  const prepare =
    isLiveMode() && dataSource === 'session' && reportNeedsDetail()
      ? ensureFullSessionReport()
      : Promise.resolve();

  return prepare.then(() => {
    if (activeView === 'events') {
      if (currentReport) {
        renderEventGraph(root, { title: '', truncated: false, nodes: [], edges: [] }, currentReport);
        return;
      }
      void fetchEventGraph()
        .then((graph) => renderEventGraph(root, graph, currentReport))
        .catch(() => {
          root.innerHTML = '<section class="page"><p class="empty">Failed to load event graph.</p></section>';
        });
      return;
    }

    if (activeView === 'plugins') {
      renderPluginGraph(root, currentReport);
      return;
    }

    if (!currentReport) {
      if (!isLiveMode()) {
        root.innerHTML =
          '<section class="page"><p class="empty">No report loaded. Open <span class="mono">report.json</span> with the file picker, or re-export the bundle.</p></section>';
        return;
      }
      if (serverStatus?.activeTraceSessionId) {
        const uptimeMillis =
          serverStatus.activeTraceStartedAtMillis > 0
            ? Math.max(0, Date.now() - serverStatus.activeTraceStartedAtMillis)
            : 0;
        root.innerHTML = `
          <section class="page">
            <div class="stat-grid">
              <div class="stat-card">
                <div class="stat-label">Uptime</div>
                <div class="stat-value" id="stat-session-uptime">${formatUptimeMs(uptimeMillis)}</div>
              </div>
            </div>
            <p class="empty">Interact in-game to capture events, or wait for the live feed to populate.</p>
          </section>`;
      } else {
        root.innerHTML =
          '<section class="page"><p class="empty">No active trace session. Start one with <span class="mono">/eventlens trace start &lt;Event&gt;</span>.</p></section>';
      }
      return;
    }

    if (activeView === 'overview') {
      renderOverview(root, currentReport);
    } else if (activeView === 'timeline') {
      renderTimeline(root, currentReport);
    } else if (activeView === 'flame') {
      renderFlameGraph(root, currentReport);
    } else if (activeView === 'compare') {
      renderCompare(
        root,
        currentReport,
        compareReport,
        cachedReports,
        (file) => {
          void file.text().then((text) => {
            compareReport = parseReportJson(text);
            void renderActiveView(false);
          });
        },
        (fileName) => {
          void fetchReportFile(fileName).then((report) => {
            compareReport = report;
            void renderActiveView(false);
          });
        },
      );
    }
  });
}

ensureShell();
void maybeLoadBundledReport()
  .then(() => bootstrapLiveData())
  .then(() => renderActiveView(false))
  .catch((error) => {
    const root = app.querySelector('#view-root');
    if (root) {
      root.innerHTML = `<p class="empty">Failed to load dashboard: ${error}</p>`;
    }
  });

window.addEventListener('beforeunload', () => {
  stopLiveStream?.();
  if (backgroundSyncTimer !== null) {
    window.clearTimeout(backgroundSyncTimer);
  }
  if (serverStatusTimer !== null) {
    window.clearInterval(serverStatusTimer);
  }
  if (sessionClockTimer !== null) {
    window.clearInterval(sessionClockTimer);
  }
});
