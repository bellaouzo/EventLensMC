import type { DashboardSession, TraceReport } from '../types';
import { compareReports, pairDispatches } from '../utils/traceCompare';
import { escapeHtml, formatMillis, shortSessionId, simpleEventName } from '../utils/format';

export interface CompareSources {
  reports: Array<{ fileName: string; format?: string }>;
  sessions: DashboardSession[];
  currentReportFile: string;
  currentSessionId: string;
}

export function renderCompare(
  container: HTMLElement,
  left: TraceReport | null,
  right: TraceReport | null,
  sources: CompareSources,
  onRightFile?: (file: File) => void,
  onRightReport?: (fileName: string) => void,
  onRightSession?: (sessionId: string) => void,
  onClearRight?: () => void,
): void {
  if (!left) {
    container.innerHTML =
      '<section class="page"><p class="empty">Select a live session or saved report first. That becomes the left side of the compare.</p></section>';
    return;
  }

  const leftCard = sourceCard(
    'Left',
    simpleEventName(left.session.eventClassName),
    `${shortSessionId(left.session.sessionId)} · ${left.session.capturedEvents} captured`,
    'Current view',
    true,
  );

  const rightCard = right
    ? sourceCard(
        'Right',
        simpleEventName(right.session.eventClassName),
        `${shortSessionId(right.session.sessionId)} · ${right.session.capturedEvents} captured`,
        'Comparison',
        true,
      )
    : sourceCard('Right', 'Not selected', 'Choose a live session or a .json report', 'Waiting', false);

  const sessionChoices = sources.sessions
    .filter((session) => session.sessionId !== sources.currentSessionId && session.sessionId !== right?.session.sessionId)
    .map(
      (session) =>
        `<button type="button" class="compare-pick" data-session="${escapeHtml(session.sessionId)}">
          <span class="compare-pick-title">${escapeHtml(simpleEventName(session.eventClassName))}</span>
          <span class="compare-pick-meta">${escapeHtml(shortSessionId(session.sessionId))} · ${session.capturedEvents} captured · live</span>
        </button>`,
    )
    .join('');

  const reportChoices = sources.reports
    .filter((report) => isJsonReport(report) && report.fileName !== sources.currentReportFile)
    .map(
      (report) =>
        `<button type="button" class="compare-pick" data-report="${escapeHtml(report.fileName)}">
          <span class="compare-pick-title">${escapeHtml(report.fileName)}</span>
          <span class="compare-pick-meta">JSON report</span>
        </button>`,
    )
    .join('');

  const comparison = right
    ? `<table class="data-table">
        <thead><tr><th>Metric</th><th>Left</th><th>Right</th><th>Delta</th></tr></thead>
        <tbody>${compareReports(left, right)
          .map(
            (row) =>
              `<tr>
                <th>${escapeHtml(row.label)}</th>
                <td>${escapeHtml(row.left)}</td>
                <td>${escapeHtml(row.right)}</td>
                <td class="compare-delta${row.tone ? ` delta-${row.tone}` : ''}">${escapeHtml(row.delta ?? '—')}</td>
              </tr>`,
          )
          .join('')}
        </tbody>
      </table>`
    : '<p class="compare-hint">Pick a counterpart on the right. Left stays as the session or report you already have open.</p>';

  container.innerHTML = `
    <section class="page">
      <header class="page-header">
        <h1>Compare</h1>
        <p class="page-subtitle">Left is the current view. The right side must be a live session or a <span class="mono">.json</span> report — HTML exports cannot be compared.</p>
      </header>
      <div class="compare-sides">
        ${leftCard}
        <div class="compare-vs">vs</div>
        ${rightCard}
      </div>
      ${
        right
          ? '<button type="button" class="btn-ghost" id="compare-clear">Change right side</button>'
          : `<div class="compare-picker">
        <div>
          <div class="sidebar-heading">Live sessions</div>
          <div class="compare-picks">${sessionChoices || '<p class="sidebar-empty">No other live sessions</p>'}</div>
        </div>
        <div>
          <div class="sidebar-heading">JSON reports</div>
          <div class="compare-picks">${reportChoices || '<p class="sidebar-empty">No other JSON reports</p>'}</div>
        </div>
        <label class="file-upload compare-upload">Load JSON file<input type="file" accept="application/json,.json" /></label>
      </div>`
      }
      ${comparison}
      ${right ? pairedTable(left, right) : ''}
    </section>`;

  bindRightFile(container, onRightFile);
  container.querySelectorAll<HTMLButtonElement>('.compare-pick[data-report]').forEach((button) => {
    button.addEventListener('click', () => {
      const fileName = button.dataset.report;
      if (fileName) {
        onRightReport?.(fileName);
      }
    });
  });
  container.querySelectorAll<HTMLButtonElement>('.compare-pick[data-session]').forEach((button) => {
    button.addEventListener('click', () => {
      const sessionId = button.dataset.session;
      if (sessionId) {
        onRightSession?.(sessionId);
      }
    });
  });
  container.querySelector('#compare-clear')?.addEventListener('click', () => {
    onClearRight?.();
  });
}

function pairedTable(left: TraceReport, right: TraceReport): string {
  const pairs = pairDispatches(left, right);
  if (!pairs.length) {
    return '<p class="compare-hint">No correlated dispatch pairs. Use matching correlation keys or /eventlens trace correlate first.</p>';
  }
  const rows = pairs
    .map(
      (pair) =>
        `<tr>
          <td>#${pair.leftSequence} ${escapeHtml(simpleEventName(pair.leftEvent))}</td>
          <td>${pair.leftCancelled ? 'cancelled' : 'ok'} · ${escapeHtml(formatMillis(pair.leftDurationNanos))}</td>
          <td>#${pair.rightSequence} ${escapeHtml(simpleEventName(pair.rightEvent))}</td>
          <td>${pair.rightCancelled ? 'cancelled' : 'ok'} · ${escapeHtml(formatMillis(pair.rightDurationNanos))}</td>
        </tr>`,
    )
    .join('');
  return `<h2 class="page-subtitle">Paired dispatches (${pairs.length})</h2>
    <table class="data-table">
      <thead><tr><th>Left</th><th>Cancel / time</th><th>Right</th><th>Cancel / time</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>`;
}

function isJsonReport(report: { fileName: string; format?: string }): boolean {
  if (report.format) {
    return report.format.toLowerCase() === 'json';
  }
  return report.fileName.toLowerCase().endsWith('.json');
}

function sourceCard(side: string, title: string, meta: string, tag: string, filled: boolean): string {
  return `
    <div class="compare-card${filled ? ' filled' : ''}">
      <div class="compare-card-side">${escapeHtml(side)}</div>
      <div class="compare-card-title">${escapeHtml(title)}</div>
      <div class="compare-card-meta">${escapeHtml(meta)}</div>
      <span class="compare-card-tag">${escapeHtml(tag)}</span>
    </div>`;
}

function bindRightFile(container: HTMLElement, onRightFile?: (file: File) => void): void {
  const input = container.querySelector<HTMLInputElement>('input[type="file"]');
  if (!input || !onRightFile) {
    return;
  }
  input.addEventListener('change', () => {
    const file = input.files?.[0];
    if (file) {
      onRightFile(file);
    }
  });
}
