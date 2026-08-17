import type { ListenerTiming, TraceDispatch, TraceReport } from '../types';
import { escapeHtml, formatMillis, simpleEventName } from '../utils/format';
import { resolveListenerTimings } from '../utils/listenerData';
import { sessionStateBannerHtml } from '../utils/sessionState';
import { TICK_BUDGET_NANOS } from '../utils/metrics';

const PRIORITY_ORDER = ['LOWEST', 'LOW', 'NORMAL', 'HIGH', 'HIGHEST', 'MONITOR'];

let selectedDispatchIndex = 0;

export function resetTimelineSelection(): void {
  selectedDispatchIndex = 0;
}

export function renderTimeline(container: HTMLElement, report: TraceReport): void {
  const banner = sessionStateBannerHtml(report.session.state);

  if (!report.dispatches.length) {
    container.innerHTML = `<section class="page"><header class="page-header"><h1>Timeline</h1></header>${banner}<p class="empty">No dispatches captured.</p></section>`;
    return;
  }

  if (selectedDispatchIndex >= report.dispatches.length) {
    selectedDispatchIndex = 0;
  }

  const dispatch = pickHighlightDispatch(report.dispatches, selectedDispatchIndex);
  const dispatchIndex = report.dispatches.indexOf(dispatch);
  const maxMs = Math.max(12, Math.ceil(dispatch.durationNanos / 1_000_000) + 1);
  const tickPercent = (dispatch.durationNanos / TICK_BUDGET_NANOS) * 100;
  const severity = tickPercent >= 15 ? 'crit' : tickPercent >= 5 ? 'warn' : 'ok';

  const location = formatLocation(dispatch);
  const lanes = buildPriorityLanes(dispatch, maxMs);

  container.innerHTML = `
    <section class="page">
      <header class="page-header">
        <h1>Timeline</h1>
        <p class="page-subtitle mono">
          ${escapeHtml(simpleEventName(dispatch.eventClassName))}${location ? ` · ${escapeHtml(location)}` : ''} · tick #${dispatch.sequence} · total <span class="severity-${severity}">${formatMillis(dispatch.durationNanos)} (${tickPercent.toFixed(1)}% of tick budget)</span>
        </p>
      </header>
      ${banner}
      <div class="timeline-panel">
        <div class="timeline-controls">
          <button type="button" class="btn-ghost" id="timeline-prev"${dispatchIndex <= 0 ? ' disabled' : ''}>← Previous</button>
          <span class="mono status-muted">Dispatch ${dispatchIndex + 1} of ${report.dispatches.length}</span>
          <button type="button" class="btn-ghost" id="timeline-next"${dispatchIndex >= report.dispatches.length - 1 ? ' disabled' : ''}>Next →</button>
        </div>
        <div class="timeline-axis">
          ${axisLabels(maxMs)
            .map((ms) => `<span>${ms}ms</span>`)
            .join('')}
        </div>
        <div class="timeline-lanes">${lanes}</div>
      </div>
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

function pickHighlightDispatch(dispatches: TraceDispatch[], index: number): TraceDispatch {
  if (index >= 0 && index < dispatches.length) {
    return dispatches[index];
  }
  return [...dispatches].sort((a, b) => b.durationNanos - a.durationNanos)[0];
}

function formatLocation(dispatch: TraceDispatch): string {
  const parts: string[] = [];
  if (dispatch.blockMaterial) {
    parts.push(dispatch.blockMaterial);
  }
  if (dispatch.blockX != null && dispatch.blockY != null && dispatch.blockZ != null) {
    parts.push(`@ ${dispatch.blockX},${dispatch.blockY},${dispatch.blockZ}`);
  }
  return parts.join(' ');
}

function axisLabels(maxMs: number): number[] {
  const step = maxMs <= 12 ? 2 : Math.ceil(maxMs / 6);
  const labels: number[] = [];
  for (let ms = 0; ms <= maxMs; ms += step) {
    labels.push(ms);
  }
  if (labels[labels.length - 1] !== maxMs) {
    labels.push(maxMs);
  }
  return labels;
}

function buildPriorityLanes(dispatch: TraceDispatch, maxMs: number): string {
  const timings = resolveListenerTimings(dispatch);
  const grouped = new Map<string, ListenerTiming[]>();
  for (const timing of timings) {
    const key = normalizePriority(timing.priority);
    const list = grouped.get(key) ?? [];
    list.push(timing);
    grouped.set(key, list);
  }

  let cursorMs = 0;
  const lanes: string[] = [];

  for (const priority of PRIORITY_ORDER) {
    const timings = grouped.get(priority);
    if (!timings?.length) {
      continue;
    }
    timings.sort((a, b) => a.invocationOrder - b.invocationOrder);

    const isMonitor = priority === 'MONITOR';
    const bars = timings
      .map((timing) => {
        const startMs = cursorMs;
        const widthMs = timing.durationNanos / 1_000_000;
        cursorMs += widthMs;
        const leftPct = (startMs / maxMs) * 100;
        const widthPct = Math.max(isMonitor ? 0.6 : 0.8, (widthMs / maxMs) * 100);
        const severity = barSeverity(timing, widthMs);
        const tone = isMonitor ? 'monitor' : severity;
        const shortName = shortenPlugin(timing.pluginName);
        const flagged = timing.exceedsSlowThreshold ? ' · flagged' : '';
        const label = isMonitor
          ? ''
          : `<span>${escapeHtml(shortName)} ${formatMillis(timing.durationNanos)}${flagged}</span>`;
        return `
          <div class="timeline-bar bar-${tone}" style="left:${leftPct.toFixed(2)}%;width:${widthPct.toFixed(2)}%;" title="${escapeHtml(timing.pluginName)}.${escapeHtml(timing.methodName)} · ${formatMillis(timing.durationNanos)}">
            ${label}
          </div>`;
      })
      .join('');

    const monitorNote =
      isMonitor && timings.length
        ? `<div class="lane-note">${timings.map((t) => `${escapeHtml(t.pluginName)} ${formatMillis(t.durationNanos)}`).join(', ')} (read-only observers)</div>`
        : '';

    lanes.push(`
      <div class="timeline-lane-block">
        <div class="lane-label">${priorityLabel(priority)}</div>
        <div class="lane-track">${bars}</div>
        ${monitorNote}
      </div>`);
  }

  if (!lanes.length) {
    const hasAgentTimings = dispatch.listenerTimings.length > 0;
    return `<div class="empty">${hasAgentTimings ? 'No listeners matched the timeline filters.' : 'No per-listener timings for this dispatch. Attach the EventLens agent for measured timings, or wait for the full trace sync.'}</div>`;
  }
  return lanes.join('');
}

function priorityLabel(priority: string): string {
  if (priority === 'LOWEST') {
    return 'Lowest';
  }
  if (priority === 'LOW') {
    return 'Low';
  }
  if (priority === 'NORMAL') {
    return 'Normal';
  }
  if (priority === 'HIGH' || priority === 'HIGHEST') {
    return 'High';
  }
  return 'Monitor';
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

function shortenPlugin(name: string): string {
  if (name.length <= 14) {
    return name;
  }
  return name.slice(0, 12) + '…';
}
