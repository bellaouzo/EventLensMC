import type { TraceReport } from '../types';
import { formatDispatchTime, formatMillis, formatUptime, escapeHtml } from '../utils/format';
import {
  computeSessionMetrics,
  formatTickPercent,
  liveSessionDuration,
  statusLabel,
  type RecentTraceEntry,
} from '../utils/metrics';
import { sessionStateBannerHtml } from '../utils/sessionState';

const DISPATCH_PAGE_SIZE = 8;
let dispatchPage = 0;

export function resetOverviewDispatchPage(): void {
  dispatchPage = 0;
}

function tickClass(status: 'critical' | 'warn' | 'ok' | 'warmup'): string {
  if (status === 'critical') {
    return 'offender-tick-crit';
  }
  if (status === 'warn') {
    return 'offender-tick-warn';
  }
  if (status === 'warmup') {
    return 'offender-tick-warmup';
  }
  return 'offender-tick-ok';
}

export function renderOverview(
  container: HTMLElement,
  report: TraceReport,
  onOpenDispatch?: (sequence: number) => void,
): void {
  const metrics = computeSessionMetrics(report);
  const flagged = metrics.flaggedListeners;

  const offenderRows =
    metrics.topOffenders.length > 0
      ? metrics.topOffenders
          .map(
            (row) => `
          <button type="button" class="offender-row row-clickable" data-sequence="${row.sequence}">
            <div class="offender-plugin">${escapeHtml(row.plugin)}</div>
            <div class="offender-event">${escapeHtml(row.event)}</div>
            <div class="offender-time">${formatMillis(row.timeNanos)}</div>
            <div class="${tickClass(row.status)}">${formatTickPercent(row.timeNanos)}</div>
            <div class="row-status">
              <span class="pill pill-${row.status}">${statusLabel(row.status)}</span>
              <span class="row-open">View</span>
            </div>
          </button>`,
          )
          .join('')
      : `<div class="empty-cell">No listener timing data yet. Attach the EventLens agent for per-listener metrics.</div>`;

  const traces = metrics.recentTraces;
  const totalPages = Math.max(1, Math.ceil(traces.length / DISPATCH_PAGE_SIZE));
  dispatchPage = Math.min(dispatchPage, totalPages - 1);
  const start = dispatchPage * DISPATCH_PAGE_SIZE;
  const pageTraces = traces.slice(start, start + DISPATCH_PAGE_SIZE);
  const feedItems =
    pageTraces.length > 0
      ? pageTraces.map((entry) => feedRowHtml(entry)).join('')
      : '<div class="empty-cell">No dispatches captured yet.</div>';
  const rangeEnd = start + pageTraces.length;
  const pager =
    traces.length > DISPATCH_PAGE_SIZE
      ? `
          <div class="feed-pager">
            <button type="button" class="btn-ghost" id="feed-newer"${dispatchPage <= 0 ? ' disabled' : ''}>← Newer</button>
            <span class="mono status-muted">${start + 1}–${rangeEnd} of ${traces.length}</span>
            <button type="button" class="btn-ghost" id="feed-older"${dispatchPage >= totalPages - 1 ? ' disabled' : ''}>Older →</button>
          </div>`
      : '';

  container.innerHTML = `
    <section class="page">
      ${sessionStateBannerHtml(report.session.state)}
      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-label">Uptime</div>
          <div class="stat-value" id="stat-session-uptime">${formatUptime(liveSessionDuration(report.session))}</div>
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
          ${pager}
        </div>
      </div>
    </section>
  `;

  container.querySelectorAll<HTMLButtonElement>('[data-sequence]').forEach((row) => {
    row.addEventListener('click', () => {
      const sequence = Number(row.dataset.sequence);
      if (Number.isFinite(sequence)) {
        onOpenDispatch?.(sequence);
      }
    });
  });
  container.querySelector('#feed-newer')?.addEventListener('click', () => {
    dispatchPage = Math.max(0, dispatchPage - 1);
    renderOverview(container, report, onOpenDispatch);
  });
  container.querySelector('#feed-older')?.addEventListener('click', () => {
    dispatchPage = Math.min(totalPages - 1, dispatchPage + 1);
    renderOverview(container, report, onOpenDispatch);
  });
}

function feedRowHtml(entry: RecentTraceEntry): string {
  return `
          <button type="button" class="feed-row row-clickable" data-sequence="${entry.sequence}">
            <div class="feed-time">${formatDispatchTime(entry.startedAtMillis)}</div>
            <div class="feed-event">${escapeHtml(entry.label)}</div>
            <div class="feed-detail">${entry.detail ? escapeHtml(entry.detail) : '—'}</div>
            <div class="feed-ms ${tickClass(entry.severity)}">${formatMillis(entry.durationNanos)}</div>
            <div class="row-status">
              <span class="pill pill-${entry.severity}">${statusLabel(entry.severity)}</span>
              <span class="row-open">View</span>
            </div>
          </button>`;
}

export function updateOverviewUptime(container: HTMLElement, uptimeMillis: number): void {
  const element = container.querySelector('#stat-session-uptime');
  if (element) {
    element.textContent = formatUptime(uptimeMillis);
  }
}
