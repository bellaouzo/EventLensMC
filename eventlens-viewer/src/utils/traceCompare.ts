import type { TraceReport } from '../types';
import { formatMillis, simpleEventName } from './format';
import { computeSessionMetrics } from './metrics';

export interface CompareRow {
  label: string;
  left: string;
  right: string;
  delta?: string;
  tone?: 'up' | 'down' | 'same';
}

export function compareReports(left: TraceReport, right: TraceReport): CompareRow[] {
  const leftMetrics = computeSessionMetrics(left);
  const rightMetrics = computeSessionMetrics(right);
  const leftAvg = averageDispatchNanos(left);
  const rightAvg = averageDispatchNanos(right);
  const paired = pairByCorrelation(left, right);

  return [
    stringRow('Event', simpleEventName(left.session.eventClassName), simpleEventName(right.session.eventClassName)),
    numericRow('Captured', left.session.capturedEvents, right.session.capturedEvents),
    numericRow('Dispatches', left.dispatches.length, right.dispatches.length),
    numericRow('Flagged listeners', leftMetrics.flaggedListeners, rightMetrics.flaggedListeners, true),
    durationRow('Avg dispatch', leftAvg, rightAvg),
    durationRow('Slowest listener', leftMetrics.topOffenders[0]?.timeNanos ?? 0, rightMetrics.topOffenders[0]?.timeNanos ?? 0),
    stringRow('Slowest plugin', leftMetrics.topOffenders[0]?.plugin ?? '—', rightMetrics.topOffenders[0]?.plugin ?? '—'),
    numericRow('Correlated pairs', paired, paired),
  ];
}

function stringRow(label: string, left: string, right: string): CompareRow {
  return {
    label,
    left,
    right,
    tone: left === right ? 'same' : undefined,
  };
}

function numericRow(label: string, left: number, right: number, lowerIsBetter = false): CompareRow {
  const delta = right - left;
  const tone = delta === 0 ? 'same' : lowerIsBetter ? (delta < 0 ? 'down' : 'up') : delta > 0 ? 'up' : 'down';
  return {
    label,
    left: String(left),
    right: String(right),
    delta: delta === 0 ? '0' : `${delta > 0 ? '+' : ''}${delta}`,
    tone,
  };
}

function durationRow(label: string, leftNanos: number, rightNanos: number): CompareRow {
  const delta = rightNanos - leftNanos;
  const tone = delta === 0 ? 'same' : delta > 0 ? 'up' : 'down';
  return {
    label,
    left: formatMillis(leftNanos),
    right: formatMillis(rightNanos),
    delta: delta === 0 ? '0.00 ms' : `${delta > 0 ? '+' : '−'}${formatMillis(Math.abs(delta))}`,
    tone,
  };
}

function averageDispatchNanos(report: TraceReport): number {
  if (!report.dispatches.length) {
    return 0;
  }
  const total = report.dispatches.reduce((sum, dispatch) => sum + dispatch.durationNanos, 0);
  return total / report.dispatches.length;
}

export interface CorrelatedPair {
  leftSequence: number;
  rightSequence: number;
  correlationKey: string;
  leftCancelled: boolean;
  rightCancelled: boolean;
  leftDurationNanos: number;
  rightDurationNanos: number;
  leftEvent: string;
  rightEvent: string;
}

export function pairDispatches(left: TraceReport, right: TraceReport): CorrelatedPair[] {
  const used = new Set<number>();
  const pairs: CorrelatedPair[] = [];
  for (const leftDispatch of left.dispatches) {
    const key = leftDispatch.correlationKey;
    if (!key) {
      continue;
    }
    const matchIndex = right.dispatches.findIndex(
      (dispatch, index) => !used.has(index) && dispatch.correlationKey === key,
    );
    if (matchIndex < 0) {
      continue;
    }
    used.add(matchIndex);
    const match = right.dispatches[matchIndex];
    pairs.push({
      leftSequence: leftDispatch.sequence,
      rightSequence: match.sequence,
      correlationKey: key,
      leftCancelled: leftDispatch.cancelledAtEnd,
      rightCancelled: match.cancelledAtEnd,
      leftDurationNanos: leftDispatch.durationNanos,
      rightDurationNanos: match.durationNanos,
      leftEvent: leftDispatch.eventClassName,
      rightEvent: match.eventClassName,
    });
  }
  return pairs;
}

function pairByCorrelation(left: TraceReport, right: TraceReport): number {
  return pairDispatches(left, right).length;
}
