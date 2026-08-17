import type { TraceReport } from '../types';
import { escapeHtml, formatMillis } from '../utils/format';
import { formatTickPercent } from '../utils/metrics';
import { resolveListenerTimings } from '../utils/listenerData';
import { sessionStateBannerHtml } from '../utils/sessionState';

const PRIORITY_COLUMNS = ['LOWEST', 'LOW', 'NORMAL', 'HIGH', 'MONITOR'];

interface PluginCard {
  plugin: string;
  priority: string;
  durationNanos: number;
  status: 'critical' | 'warn' | 'ok' | 'monitor';
  readOnly: boolean;
}

export function renderPluginGraph(container: HTMLElement, report: TraceReport | null): void {
  if (!report?.dispatches.length) {
    const banner = report ? sessionStateBannerHtml(report.session.state) : '';
    container.innerHTML = `${banner ? `<section class="page"><header class="page-header"><h1>Plugin graph</h1></header>${banner}<p class="empty">No dispatch data for plugin graph.</p></section>` : '<section class="page"><p class="empty">No dispatch data for plugin graph.</p></section>'}`;
    return;
  }

  const dispatch = [...report.dispatches].sort((a, b) => b.durationNanos - a.durationNanos)[0];
  const cards = buildPluginCards(dispatch);

  const columnBlocks = PRIORITY_COLUMNS.map((priority) => {
    const items = cards.filter((c) => c.priority === priority || (priority === 'HIGH' && c.priority === 'HIGHEST'));
    return `
      <div class="priority-column">
        <div class="priority-label">${priorityLabel(priority)}</div>
        ${
          items.length
            ? items
                .map(
                  (card) => `
              <div class="plugin-card card-${card.status}">
                <div class="plugin-name">${escapeHtml(card.plugin)}</div>
                <div class="plugin-time">${formatMillis(card.durationNanos)}</div>
                <div class="plugin-tick">${formatTickPercent(card.durationNanos)} tick${card.status === 'critical' ? ' · flagged' : ''}${card.readOnly ? card.status !== 'critical' ? ' · read-only' : '' : ''}</div>
              </div>`,
                )
                .join('')
            : ''
        }
      </div>`;
  });

  const gridHtml = columnBlocks
    .map((column, index) => (index === 0 ? column : `<div class="priority-arrow">›</div>${column}`))
    .join('');

  container.innerHTML = `
    <section class="page">
      <header class="page-header">
        <h1>Plugin graph</h1>
        <p class="page-subtitle">Execution order for this trace, grouped by listener priority. Node color = severity.</p>
      </header>
      ${sessionStateBannerHtml(report.session.state)}
      <div class="plugin-panel">
        <div class="priority-grid">${gridHtml}</div>
      </div>
    </section>
  `;
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
  if (priority === 'HIGH') {
    return 'High';
  }
  return 'Monitor';
}

function buildPluginCards(dispatch: TraceReport['dispatches'][0]): PluginCard[] {
  const cards: PluginCard[] = [];

  for (const timing of resolveListenerTimings(dispatch)) {
    const priority = normalizePriority(timing.priority);
    const tickPercent = (timing.durationNanos / 50_000_000) * 100;
    const monitor = priority === 'MONITOR';
    const status = monitor
      ? 'monitor'
      : timing.exceedsSlowThreshold || tickPercent >= 10
        ? 'critical'
        : tickPercent >= 3
          ? 'warn'
          : 'ok';
    const readOnly = monitor || timing.pluginName.toLowerCase().includes('eventlens');

    cards.push({
      plugin: timing.pluginName,
      priority,
      durationNanos: timing.durationNanos,
      status,
      readOnly,
    });
  }

  return cards.sort((a, b) => {
    const pi = PRIORITY_COLUMNS.indexOf(a.priority === 'HIGHEST' ? 'HIGH' : a.priority) -
      PRIORITY_COLUMNS.indexOf(b.priority === 'HIGHEST' ? 'HIGH' : b.priority);
    if (pi !== 0) {
      return pi;
    }
    return b.durationNanos - a.durationNanos;
  });
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
