import type { TraceReport } from '../types';
import { compareReports } from '../utils/traceCompare';
import { escapeHtml, simpleEventName } from '../utils/format';

export function renderCompare(
  container: HTMLElement,
  left: TraceReport | null,
  right: TraceReport | null,
  reports: Array<{ fileName: string }>,
  onRightFile?: (file: File) => void,
  onRightReport?: (fileName: string) => void,
): void {
  if (!left) {
    container.innerHTML = `<section class="page"><p class="empty">Load a session or report first, then choose a second JSON to compare.</p></section>`;
    return;
  }

  if (!right) {
    const choices = reports
      .filter((report) => report.fileName !== left.session.sessionId)
      .map(
        (report) =>
          `<button type="button" class="compare-choice" data-report="${escapeHtml(report.fileName)}">${escapeHtml(report.fileName)}</button>`,
      )
      .join('');
    container.innerHTML = `
      <section class="page">
        <div class="compare-banner">Only one report is loaded. Choose a second report to compare.</div>
        <div class="compare-choices">
          <button type="button" class="compare-choice" disabled>${escapeHtml(simpleEventName(left.session.eventClassName))} — current ${left.session.sessionId ? 'session' : 'report'}</button>
          ${choices}
        </div>
        <label class="file-upload">Load second JSON<input type="file" accept="application/json,.json" /></label>
      </section>`;
    bindRightFile(container, onRightFile);
    bindReportChoices(container, onRightReport);
    return;
  }

  const rows = compareReports(left, right)
    .map(
      (row) =>
        `<tr><th>${escapeHtml(row.label)}</th><td>${escapeHtml(row.left)}</td><td>${escapeHtml(row.right)}</td></tr>`,
    )
    .join('');
  container.innerHTML = `
    <section class="page">
      <label class="file-upload">Replace right report<input type="file" accept="application/json,.json" /></label>
      <table class="data-table">
        <thead><tr><th></th><th>Left</th><th>Right</th></tr></thead>
        <tbody>${rows}</tbody>
      </table>
    </section>`;
  bindRightFile(container, onRightFile);
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

function bindReportChoices(container: HTMLElement, onRightReport?: (fileName: string) => void): void {
  if (!onRightReport) {
    return;
  }
  container.querySelectorAll<HTMLButtonElement>('.compare-choice[data-report]').forEach((button) => {
    button.addEventListener('click', () => {
      const fileName = button.dataset.report;
      if (fileName) {
        onRightReport(fileName);
      }
    });
  });
}
