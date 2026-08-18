import type { TraceReport } from '../types';
import { escapeHtml, formatMillis, simpleEventName } from '../utils/format';
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
    container.innerHTML = `<section class="page">${banner}<p class="empty">No dispatch data for plugin graph.</p></section>`;
    return;
  }

  const dispatch = [...report.dispatches].sort((a, b) => b.durationNanos - a.durationNanos)[0];
  const cards = buildPluginCards(dispatch);
  const eventName = simpleEventName(dispatch.eventClassName);

  const columnBlocks = PRIORITY_COLUMNS.map((priority) => {
    const items = cards.filter((card) => card.priority === priority || (priority === 'HIGH' && card.priority === 'HIGHEST'));
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
                <div class="plugin-tick">${formatTickPercent(card.durationNanos)}</div>
                ${card.readOnly ? '<div class="plugin-note">read-only</div>' : ''}
              </div>`,
                )
                .join('')
            : ''
        }
      </div>`;
  }).join('');

  container.innerHTML = `
    <section class="page">
      ${sessionStateBannerHtml(report.session.state)}
      <header class="page-header">
        <h1>${escapeHtml(eventName)}</h1>
        <p class="page-subtitle">slowest dispatch, by priority</p>
      </header>
      <div class="priority-grid">${columnBlocks}</div>
    </section>
  `;
}

function priorityLabel(priority: string): string {
  if (priority === 'HIGH') {
    return 'HIGH / HIGHEST';
  }
  return priority;
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
    const mappedA = a.priority === 'HIGHEST' ? 'HIGH' : a.priority;
    const mappedB = b.priority === 'HIGHEST' ? 'HIGH' : b.priority;
    const pi = PRIORITY_COLUMNS.indexOf(mappedA) - PRIORITY_COLUMNS.indexOf(mappedB);
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
