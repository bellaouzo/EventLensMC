import type { TraceDispatch, TraceReport } from '../types';
import { escapeHtml, formatMillis, simpleEventName } from '../utils/format';
import { formatTickPercent } from '../utils/metrics';
import { resolveListenerTimings } from '../utils/listenerData';
import { sessionStateBannerHtml } from '../utils/sessionState';

export function renderFlameGraph(container: HTMLElement, report: TraceReport): void {
  const banner = sessionStateBannerHtml(report.session.state);
  const dispatch = pickSlowestDispatch(report.dispatches);
  if (!dispatch) {
    container.innerHTML = `<section class="page"><header class="page-header"><h1>Flame graph</h1></header>${banner}<p class="empty">No dispatches captured.</p></section>`;
    return;
  }

  if (!resolveListenerTimings(dispatch).length) {
    container.innerHTML =
      `<section class="page"><header class="page-header"><h1>Flame graph</h1></header>${banner}<p class="empty">No listener timing data. Attach the EventLens agent for flame view, or wait for the full trace sync.</p></section>`;
    return;
  }

  const eventName = simpleEventName(dispatch.eventClassName);
  const sorted = resolveListenerTimings(dispatch).sort((a, b) => a.invocationOrder - b.invocationOrder);
  const dominant = [...sorted].sort((a, b) => b.durationNanos - a.durationNanos)[0];
  const level1 = buildLevel1Blocks(sorted, dispatch.durationNanos);
  const level2 = buildLevel2Blocks(sorted, dispatch.durationNanos, dominant);
  const insight = buildInsight(dominant);

  container.innerHTML = `
    <section class="page">
      <header class="page-header">
        <h1>Flame graph</h1>
        <p class="page-subtitle mono">Call stack for the same trace · widths scaled to ${formatMillis(dispatch.durationNanos)}</p>
      </header>
      ${banner}
      <div class="flame-panel">
        <div class="flame-stack">
          <div class="flame-row">
            <div class="flame-block block-root">
              <span>${escapeHtml(eventName)} · ${formatMillis(dispatch.durationNanos)} · 100%</span>
            </div>
          </div>
          <div class="flame-row">${level1}</div>
          ${level2 ? `<div class="flame-row">${level2}</div>` : ''}
        </div>
        ${insight ? `<div class="flame-insight">${insight}</div>` : ''}
      </div>
    </section>
  `;
}

function pickSlowestDispatch(dispatches: TraceDispatch[]): TraceDispatch | null {
  if (!dispatches.length) {
    return null;
  }
  return [...dispatches].sort((a, b) => b.durationNanos - a.durationNanos)[0];
}

function buildLevel1Blocks(
  sorted: TraceDispatch['listenerTimings'],
  totalNanos: number,
): string {
  let cursorPct = 0;
  return sorted
    .map((timing) => {
      const widthPct = Math.max(0.2, (timing.durationNanos / totalNanos) * 100);
      const leftPct = cursorPct;
      cursorPct += widthPct;
      const monitor = timing.priority.toUpperCase().includes('MONITOR');
      const severity = blockSeverity(timing, timing.durationNanos / totalNanos);
      const tone = monitor ? 'monitor' : severity;
      const label =
        monitor || widthPct < 3
          ? ''
          : `<span>${escapeHtml(timing.pluginName)}${widthPct >= 8 ? ` · ${formatMillis(timing.durationNanos)}` : ''}</span>`;
      return `
        <div class="flame-block block-${tone}" style="left:${leftPct.toFixed(2)}%;width:${widthPct.toFixed(2)}%;" title="${escapeHtml(timing.pluginName)}.${escapeHtml(timing.methodName)}">
          ${label}
        </div>`;
    })
    .join('');
}

function buildLevel2Blocks(
  sorted: TraceDispatch['listenerTimings'],
  totalNanos: number,
  dominant: TraceDispatch['listenerTimings'][0] | undefined,
): string {
  if (!dominant || dominant.durationNanos / totalNanos < 0.2) {
    return '';
  }

  const dominantStart = sorted
    .slice(0, sorted.indexOf(dominant))
    .reduce((sum, t) => sum + t.durationNanos, 0);
  const dominantLeft = (dominantStart / totalNanos) * 100;
  const dominantWidth = (dominant.durationNanos / totalNanos) * 100;

  const segments = splitMethodSegments(dominant);
  if (segments.length > 1) {
    let offset = dominantLeft;
    return segments
      .map((segment) => {
        const width = (segment.nanos / totalNanos) * 100;
        const block = `
          <div class="flame-block block-sub block-sub-crit" style="left:${offset.toFixed(2)}%;width:${Math.max(width, 2).toFixed(2)}%;">
            <span>${escapeHtml(segment.label)} ${formatMillis(segment.nanos)}</span>
          </div>`;
        offset += width;
        return block;
      })
      .join('');
  }

  const siblings = sorted.filter((t) => t !== dominant && t.durationNanos >= totalNanos * 0.03);
  if (!siblings.length) {
    return `
      <div class="flame-block block-sub block-sub-crit" style="left:${dominantLeft.toFixed(2)}%;width:${Math.max(dominantWidth * 0.4, 8).toFixed(2)}%;">
        <span>${escapeHtml(dominant.methodName)} · ${formatMillis(dominant.durationNanos)}</span>
      </div>`;
  }

  let offset = dominantLeft;
  return siblings
    .slice(0, 4)
    .map((timing) => {
      const width = (timing.durationNanos / totalNanos) * 100;
      const border = timing.exceedsSlowThreshold ? 'crit' : 'ok';
      const block = `
        <div class="flame-block block-sub block-sub-${border}" style="left:${offset.toFixed(2)}%;width:${Math.max(width, 2).toFixed(2)}%;">
          <span>${escapeHtml(timing.methodName)} · ${formatMillis(timing.durationNanos)}</span>
        </div>`;
      offset += width;
      return block;
    })
    .join('');
}

function splitMethodSegments(timing: { methodName: string; durationNanos: number }): Array<{ label: string; nanos: number }> {
  const parts = timing.methodName.split(/(?=[A-Z])|_/).filter(Boolean);
  if (parts.length <= 1) {
    return [];
  }
  const slice = Math.ceil(timing.durationNanos / parts.length);
  return parts.map((part, index) => ({
    label: part,
    nanos: index === parts.length - 1 ? timing.durationNanos - slice * (parts.length - 1) : slice,
  }));
}

function blockSeverity(
  timing: { exceedsSlowThreshold: boolean },
  share: number,
): 'critical' | 'warn' | 'ok' {
  if (timing.exceedsSlowThreshold || share > 0.4) {
    return 'critical';
  }
  if (share > 0.15) {
    return 'warn';
  }
  return 'ok';
}

function buildInsight(
  dominant: { pluginName: string; durationNanos: number; exceedsSlowThreshold: boolean } | undefined,
): string {
  if (!dominant) {
    return '';
  }
  const pct = formatTickPercent(dominant.durationNanos);
  const color = dominant.exceedsSlowThreshold ? 'severity-crit' : 'severity-warn';
  return `<div><span class="${color}">${escapeHtml(dominant.pluginName)}'s ${formatMillis(dominant.durationNanos)}</span> (${pct} tick) is the dominant cost in this dispatch.</div>`;
}
