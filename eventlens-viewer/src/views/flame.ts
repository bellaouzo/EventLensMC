import type { TraceDispatch, TraceReport } from '../types';
import { escapeHtml, formatMillis, simpleEventName } from '../utils/format';
import { resolveListenerTimings } from '../utils/listenerData';
import { pickSteadyDispatch } from '../utils/metrics';
import { sessionStateBannerHtml } from '../utils/sessionState';

export function renderFlameGraph(container: HTMLElement, report: TraceReport): void {
  const banner = sessionStateBannerHtml(report.session.state);
  const dispatch = pickSlowestDispatch(report.dispatches);
  if (!dispatch) {
    container.innerHTML = `<section class="page">${banner}<p class="empty">No dispatches captured.</p></section>`;
    return;
  }

  const timings = resolveListenerTimings(dispatch);
  if (!timings.length) {
    container.innerHTML = `<section class="page">${banner}<p class="empty">No listener timing data. Attach the EventLens agent for flame view, or wait for the full trace sync.</p></section>`;
    return;
  }

  const eventName = simpleEventName(dispatch.eventClassName);
  const total = Math.max(dispatch.durationNanos, 1);
  const ranked = [...timings].sort((a, b) => a.invocationOrder - b.invocationOrder);
  const dominant = [...ranked].sort((a, b) => b.durationNanos - a.durationNanos)[0];

  const rows = ranked
    .map((timing) => {
      const pct = (timing.durationNanos / total) * 100;
      const isDominant = timing === dominant && (timing.exceedsSlowThreshold || pct >= 40);
      return `
        <div class="flame-item">
          <div class="flame-item-head">
            <span class="flame-item-name">${escapeHtml(timing.pluginName)}</span>
            <span class="flame-item-meta">${formatMillis(timing.durationNanos)} — ${pct.toFixed(0)}%</span>
          </div>
          <div class="flame-item-track">
            <div class="flame-item-bar${isDominant ? ' dominant' : ''}" style="width:${Math.max(pct, 0.6).toFixed(1)}%"></div>
            ${isDominant ? '<span class="flame-dominant">DOMINANT</span>' : ''}
          </div>
        </div>`;
    })
    .join('');

  container.innerHTML = `
    <section class="page">
      ${banner}
      <header class="page-header">
        <h1>${escapeHtml(eventName)}</h1>
        <p class="page-subtitle">slowest dispatch — ${formatMillis(dispatch.durationNanos)} total</p>
      </header>
      <div class="flame-list">${rows}</div>
    </section>
  `;
}

function pickSlowestDispatch(dispatches: TraceDispatch[]): TraceDispatch | null {
  return pickSteadyDispatch(dispatches, (left, right) => right.durationNanos - left.durationNanos);
}
