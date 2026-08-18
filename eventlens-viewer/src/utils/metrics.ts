import type { ListenerTiming, TraceDispatch, TraceReport } from '../types';
import { formatMillis, simpleEventName } from './format';
import { resolveListenerTimings } from './listenerData';

export type Severity = 'critical' | 'warn' | 'ok' | 'warmup';

export interface TopOffender {
  plugin: string;
  event: string;
  timeNanos: number;
  tickPercent: number;
  status: Severity;
  sequence: number;
}

export interface RecentTraceEntry {
  sequence: number;
  label: string;
  detail: string | null;
  startedAtMillis: number;
  durationNanos: number;
  severity: Severity;
}

export interface SessionMetrics {
  uptimeMillis: number;
  eventsTraced: number;
  listenersInvoked: number;
  avgListenersPerEvent: number;
  flaggedListeners: number;
  topOffenders: TopOffender[];
  recentTraces: RecentTraceEntry[];
  primaryWorld: string | null;
}

const TICK_BUDGET_NANOS = 50_000_000;

export function isWarmupDispatch(dispatch: TraceDispatch, dispatches: TraceDispatch[]): boolean {
  const same = dispatches.filter((entry) => entry.eventClassName === dispatch.eventClassName);
  if (same.length < 2) {
    return false;
  }
  const firstSequence = Math.min(...same.map((entry) => entry.sequence));
  return dispatch.sequence === firstSequence;
}

export function pickSteadyDispatch(
  dispatches: TraceDispatch[],
  compare: (left: TraceDispatch, right: TraceDispatch) => number,
): TraceDispatch | null {
  if (!dispatches.length) {
    return null;
  }
  const steady = dispatches.filter((dispatch) => !isWarmupDispatch(dispatch, dispatches));
  const pool = steady.length ? steady : dispatches;
  return [...pool].sort(compare)[0];
}

export function computeSessionMetrics(report: TraceReport): SessionMetrics {
  let listenersInvoked = 0;
  let flaggedListeners = 0;
  const offenderMap = new Map<string, TopOffender>();

  for (const dispatch of report.dispatches) {
    const warmup = isWarmupDispatch(dispatch, report.dispatches);
    for (const timing of resolveListenerTimings(dispatch)) {
      listenersInvoked += 1;
      if (warmup) {
        continue;
      }
      if (timing.exceedsSlowThreshold) {
        flaggedListeners += 1;
      }
      const key = `${timing.pluginName}::${simpleEventName(dispatch.eventClassName)}`;
      const existing = offenderMap.get(key);
      const tickPercent = (timing.durationNanos / TICK_BUDGET_NANOS) * 100;
      const status = severityFor(timing, tickPercent);
      if (!existing || timing.durationNanos > existing.timeNanos) {
        offenderMap.set(key, {
          plugin: timing.pluginName,
          event: simpleEventName(dispatch.eventClassName),
          timeNanos: timing.durationNanos,
          tickPercent,
          status,
          sequence: dispatch.sequence,
        });
      }
    }
  }

  const topOffenders = [...offenderMap.values()]
    .sort((a, b) => b.timeNanos - a.timeNanos)
    .slice(0, 8);

  const recentTraces = [...report.dispatches]
    .sort((a, b) => b.sequence - a.sequence)
    .map((dispatch) => traceEntryFromDispatch(dispatch, report.dispatches));

  const captured = report.session.capturedEvents;
  const primaryWorld = report.dispatches.find((d) => d.worldName)?.worldName ?? null;

  return {
    uptimeMillis: liveSessionDuration(report.session),
    eventsTraced: captured,
    listenersInvoked,
    avgListenersPerEvent: captured > 0 ? listenersInvoked / captured : 0,
    flaggedListeners,
    topOffenders,
    recentTraces,
    primaryWorld,
  };
}

function severityFor(timing: ListenerTiming, tickPercent: number): Severity {
  if (timing.exceedsSlowThreshold || tickPercent >= 10) {
    return 'critical';
  }
  if (tickPercent >= 3) {
    return 'warn';
  }
  return 'ok';
}

function traceEntryFromDispatch(dispatch: TraceDispatch, dispatches: TraceDispatch[]): RecentTraceEntry {
  if (isWarmupDispatch(dispatch, dispatches)) {
    return {
      sequence: dispatch.sequence,
      label: simpleEventName(dispatch.eventClassName),
      detail: dispatch.blockMaterial ?? dispatch.playerName ?? null,
      startedAtMillis: dispatch.startedAtMillis,
      durationNanos: dispatch.durationNanos,
      severity: 'warmup',
    };
  }

  const slowest = [...(dispatch.listenerTimings ?? [])].sort((a, b) => b.durationNanos - a.durationNanos)[0];
  const tickPercent = (dispatch.durationNanos / TICK_BUDGET_NANOS) * 100;
  let severity: Severity = 'ok';
  if (tickPercent >= 15 || slowest?.exceedsSlowThreshold) {
    severity = 'critical';
  } else if (tickPercent >= 5) {
    severity = 'warn';
  }
  return {
    sequence: dispatch.sequence,
    label: simpleEventName(dispatch.eventClassName),
    detail: dispatch.blockMaterial ?? dispatch.playerName ?? null,
    startedAtMillis: dispatch.startedAtMillis,
    durationNanos: dispatch.durationNanos,
    severity,
  };
}

export function statusLabel(status: Severity): string {
  if (status === 'critical') {
    return 'CRITICAL';
  }
  if (status === 'warn') {
    return 'WARN';
  }
  if (status === 'warmup') {
    return 'WARMUP';
  }
  return 'OK';
}

export function formatTickPercent(nanos: number): string {
  return `${((nanos / TICK_BUDGET_NANOS) * 100).toFixed(1)}%`;
}

export function liveSessionDuration(
  session: { startedAtMillis: number; durationMillis: number; state: string },
  nowMillis: number = Date.now(),
): number {
  if (session.state === 'ACTIVE' || session.state === 'THROTTLED') {
    return Math.max(0, nowMillis - session.startedAtMillis);
  }
  return session.durationMillis;
}

export { formatMillis, TICK_BUDGET_NANOS };
