import type { ListenerTiming, TraceDispatch, TraceReport } from '../types';
import { escapeHtml, formatMillis, simpleEventName } from '../utils/format';
import { resolveListenerTimings } from '../utils/listenerData';
import { sessionStateBannerHtml } from '../utils/sessionState';
import { statusLabel, TICK_BUDGET_NANOS } from '../utils/metrics';

const PRIORITY_ORDER = ['LOWEST', 'LOW', 'NORMAL', 'HIGH', 'HIGHEST', 'MONITOR'];

let selectedDispatchIndex = 0;

export function resetTimelineSelection(): void {
  selectedDispatchIndex = 0;
}

export function renderTimeline(container: HTMLElement, report: TraceReport): void {
  const banner = sessionStateBannerHtml(report.session.state);

  if (!report.dispatches.length) {
    container.innerHTML = `<section class="page">${banner}<p class="empty">No dispatches captured.</p></section>`;
    return;
  }

  if (selectedDispatchIndex >= report.dispatches.length) {
    selectedDispatchIndex = 0;
  }

  const dispatch = report.dispatches[selectedDispatchIndex];
  const dispatchIndex = selectedDispatchIndex;
  const tickPercent = (dispatch.durationNanos / TICK_BUDGET_NANOS) * 100;
  const location = formatLocation(dispatch);
  const handlers = buildHandlerGroups(dispatch);

  container.innerHTML = `
    <section class="page">
      ${banner}
      <div class="timeline-controls">
        <button type="button" class="btn-ghost" id="timeline-prev"${dispatchIndex <= 0 ? ' disabled' : ''}>← Previous</button>
        <span class="mono status-muted">dispatch ${dispatchIndex + 1} / ${report.dispatches.length}</span>
        <button type="button" class="btn-ghost" id="timeline-next"${dispatchIndex >= report.dispatches.length - 1 ? ' disabled' : ''}>Next →</button>
      </div>
      <div class="dispatch-summary">
        <h1 class="dispatch-title">${escapeHtml(simpleEventName(dispatch.eventClassName))}</h1>
        <div class="dispatch-facts">
          ${fact('Sequence', `#${dispatch.sequence}`)}
          ${fact('Duration', `${formatMillis(dispatch.durationNanos)} (${tickPercent.toFixed(1)}% of tick)`)}
          ${fact('Location', location || '—')}
          ${fact('Player', dispatch.playerName || '—')}
          ${fact('World', dispatch.worldName || '—')}
          ${fact('Server tick', dispatch.serverTick != null ? String(dispatch.serverTick) : '—')}
          ${fact('MSPT', dispatch.msptMillis != null ? `${dispatch.msptMillis.toFixed(1)} ms` : '—')}
        </div>
      </div>
      <div class="handler-groups">${handlers}</div>
    </section>
  `;

  container.querySelector('#timeline-prev')?.addEventListener('click', () => {
    selectedDispatchIndex = Math.max(0, dispatchIndex - 1);
    renderTimeline(container, report);
  });
  container.querySelector('#timeline-next')?.addEventListener('click', () => {
    selectedDispatchIndex = Math.min(report.dispatches.length - 1, dispatchIndex + 1);
    renderTimeline(container, report);
  });
}

function fact(label: string, value: string): string {
  return `<div><div class="dispatch-fact-label">${escapeHtml(label)}</div><div class="dispatch-fact-value">${escapeHtml(value)}</div></div>`;
}

function formatLocation(dispatch: TraceDispatch): string {
  const parts: string[] = [];
  if (dispatch.blockMaterial) {
    parts.push(dispatch.blockMaterial);
  }
  if (dispatch.blockX != null && dispatch.blockY != null && dispatch.blockZ != null) {
    parts.push(`(${dispatch.blockX}, ${dispatch.blockY}, ${dispatch.blockZ})`);
  }
  return parts.join(' ');
}

function buildHandlerGroups(dispatch: TraceDispatch): string {
  const timings = resolveListenerTimings(dispatch);
  if (!timings.length) {
    const hasAgentTimings = dispatch.listenerTimings.length > 0;
    return `<p class="empty">${hasAgentTimings ? 'No listeners matched the timeline filters.' : 'No per-listener timings for this dispatch. Attach the EventLens agent for measured timings, or wait for the full trace sync.'}</p>`;
  }

  const grouped = new Map<string, ListenerTiming[]>();
  for (const timing of timings) {
    const key = normalizePriority(timing.priority);
    const list = grouped.get(key) ?? [];
    list.push(timing);
    grouped.set(key, list);
  }

  const maxNanos = Math.max(...timings.map((timing) => timing.durationNanos), 1);
  const groups: string[] = [];

  for (const priority of PRIORITY_ORDER) {
    const list = grouped.get(priority);
    if (!list?.length) {
      continue;
    }
    list.sort((a, b) => a.invocationOrder - b.invocationOrder);
    const rows = list
      .map((timing) => {
        const widthMs = timing.durationNanos / 1_000_000;
        const severity = barSeverity(timing, widthMs);
        const widthPct = Math.max(18, (timing.durationNanos / maxNanos) * 100);
        const method = formatListenerPath(timing);
        return `
          <div class="handler-row">
            <div class="handler-plugin">${escapeHtml(timing.pluginName)}</div>
            <div class="handler-method" title="${escapeHtml(method)}">${escapeHtml(method)}</div>
            <div class="handler-time">${formatMillis(timing.durationNanos)}</div>
            <div class="handler-bar ${severity}" style="width:${widthPct.toFixed(0)}%">${statusLabel(severity)}</div>
          </div>`;
      })
      .join('');
    groups.push(`<div><div class="handler-priority">${priorityLabel(priority)}</div>${rows}</div>`);
  }

  return groups.join('');
}

function formatListenerPath(timing: ListenerTiming): string {
  const className = simpleEventName(timing.listenerClassName);
  return `${timing.pluginName}.${className}#${timing.methodName}`;
}

function priorityLabel(priority: string): string {
  if (priority === 'HIGH' || priority === 'HIGHEST') {
    return priority === 'HIGHEST' ? 'HIGHEST' : 'HIGH';
  }
  return priority;
}

function barSeverity(timing: ListenerTiming, widthMs: number): 'critical' | 'warn' | 'ok' {
  if (timing.exceedsSlowThreshold || widthMs >= 5) {
    return 'critical';
  }
  if (widthMs >= 2) {
    return 'warn';
  }
  return 'ok';
}

function normalizePriority(priority: string): string {
  const upper = priority.toUpperCase();
  if (upper.includes('MONITOR')) {
    return 'MONITOR';
  }
  if (upper.includes('LOWEST')) {
    return 'LOWEST';
  }
  if (upper.includes('LOW')) {
    return 'LOW';
  }
  if (upper.includes('HIGHEST')) {
    return 'HIGHEST';
  }
  if (upper.includes('HIGH')) {
    return 'HIGH';
  }
  return 'NORMAL';
}
