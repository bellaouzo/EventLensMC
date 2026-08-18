import type { TraceReport } from '../types';
import { formatDispatchTime, formatMillis, formatUptimeMs, escapeHtml } from '../utils/format';
import { computeSessionMetrics, formatTickPercent, liveSessionDuration, statusLabel } from '../utils/metrics';
import { sessionStateBannerHtml } from '../utils/sessionState';

function tickClass(status: 'critical' | 'warn' | 'ok'): string {
  if (status === 'critical') {
    return 'offender-tick-crit';
  }
  if (status === 'warn') {
    return 'offender-tick-warn';
  }
  return 'offender-tick-ok';
}

export function renderOverview(container: HTMLElement, report: TraceReport): void {
  const metrics = computeSessionMetrics(report);
  const flagged = metrics.flaggedListeners;

  const offenderRows =
    metrics.topOffenders.length > 0
      ? metrics.topOffenders
          .map(
            (row) => `
          <div class="offender-row">
            <div class="offender-plugin">${escapeHtml(row.plugin)}</div>
            <div class="offender-event">${escapeHtml(row.event)}</div>
            <div class="offender-time">${formatMillis(row.timeNanos)}</div>
            <div class="${tickClass(row.status)}">${formatTickPercent(row.timeNanos)}</div>
            <div><span class="pill pill-${row.status}">${statusLabel(row.status)}</span></div>
          </div>`,
          )
          .join('')
      : `<div class="empty-cell">No listener timing data yet. Attach the EventLens agent for per-listener metrics.</div>`;

  const feedItems =
    metrics.recentTraces.length > 0
      ? metrics.recentTraces
          .map(
            (entry) => `
          <div class="feed-row">
            <div class="feed-time">${formatDispatchTime(entry.startedAtMillis)}</div>
            <div class="feed-event">${escapeHtml(entry.label)}</div>
            <div class="feed-detail">${entry.detail ? escapeHtml(entry.detail) : '—'}</div>
            <div class="feed-ms ${tickClass(entry.severity)}">${formatMillis(entry.durationNanos)}</div>
            <div><span class="pill pill-${entry.severity}">${statusLabel(entry.severity)}</span></div>
          </div>`,
          )
          .join('')
      : '<div class="empty-cell">No dispatches captured yet.</div>';

  container.innerHTML = `
    <section class="page">
      ${sessionStateBannerHtml(report.session.state)}
      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-label">Uptime</div>
          <div class="stat-value" id="stat-session-uptime">${formatUptimeMs(liveSessionDuration(report.session))}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Events traced</div>
          <div class="stat-value">${metrics.eventsTraced.toLocaleString()}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Listeners invoked</div>
          <div class="stat-value">${metrics.listenersInvoked.toLocaleString()}</div>
          <div class="stat-hint">avg ${metrics.avgListenersPerEvent.toFixed(1)}/event</div>
        </div>
        <div class="stat-card${flagged > 0 ? ' stat-card-alert' : ''}">
          <div class="stat-label">Flagged listeners</div>
          <div class="stat-value${flagged > 0 ? ' stat-value-danger' : ''}">${flagged}</div>
        </div>
      </div>
      <div class="stack-panels">
        <div class="panel">
          <div class="panel-head">Top offenders</div>
          <div class="offender-grid">
            <div>Name</div>
            <div>Event type</div>
            <div>Execution time</div>
            <div>%</div>
            <div>Status</div>
          </div>
          ${offenderRows}
        </div>
        <div class="panel">
          <div class="panel-head">Recent dispatches</div>
          <div class="feed-head">
            <div>Timestamp</div>
            <div>Event type</div>
            <div>Object</div>
            <div>Time</div>
            <div>Status</div>
          </div>
          ${feedItems}
        </div>
      </div>
    </section>
  `;
}

export function updateOverviewUptime(container: HTMLElement, uptimeMillis: number): void {
  const element = container.querySelector('#stat-session-uptime');
  if (element) {
    element.textContent = formatUptimeMs(uptimeMillis);
  }
}
