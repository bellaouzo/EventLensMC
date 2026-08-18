import type { TraceReport } from '../types';
import { compareReports } from '../utils/traceCompare';
import { escapeHtml } from '../utils/format';

export function renderCompare(
  container: HTMLElement,
  left: TraceReport | null,
  right: TraceReport | null,
  onRightFile?: (file: File) => void,
): void {
  const upload = `<label class="file-load">Right report
      <input type="file" accept="application/json,.json" /></label>`;
  if (!left || !right) {
    container.innerHTML = `<section class="page"><header class="page-header"><h1>Compare</h1></header>
      <p class="empty">Load a left report (or keep the current session) and choose a second JSON to compare.</p>
      ${upload}</section>`;
    bindRightFile(container, onRightFile);
    return;
  }
  const rows = compareReports(left, right)
    .map(
      (row) =>
        `<tr><th>${escapeHtml(row.label)}</th><td>${escapeHtml(row.left)}</td><td>${escapeHtml(row.right)}</td></tr>`,
    )
    .join('');
  container.innerHTML = `<section class="page"><header class="page-header"><h1>Compare</h1></header>
    ${upload}
    <table class="data-table"><thead><tr><th></th><th>Left</th><th>Right</th></tr></thead><tbody>${rows}</tbody></table>
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
