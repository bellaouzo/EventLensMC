import type { TraceReport } from '../types';

export interface CompareRow {
  label: string;
  left: string;
  right: string;
}

export function compareReports(left: TraceReport, right: TraceReport): CompareRow[] {
  const rows: CompareRow[] = [
    {
      label: 'Event',
      left: left.session.eventClassName,
      right: right.session.eventClassName,
    },
    {
      label: 'Captured',
      left: String(left.session.capturedEvents),
      right: String(right.session.capturedEvents),
    },
    {
      label: 'Dispatches',
      left: String(left.dispatches.length),
      right: String(right.dispatches.length),
    },
  ];
  const paired = pairByCorrelation(left, right);
  rows.push({
    label: 'Correlated pairs',
    left: String(paired),
    right: String(paired),
  });
  return rows;
}

function pairByCorrelation(left: TraceReport, right: TraceReport): number {
  const rightKeys = new Set(
    right.dispatches.map((dispatch) => dispatch.correlationKey).filter((key): key is string => !!key),
  );
  return left.dispatches.filter((dispatch) => dispatch.correlationKey && rightKeys.has(dispatch.correlationKey)).length;
}
