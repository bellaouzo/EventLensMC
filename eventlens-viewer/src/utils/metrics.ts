import type { ListenerTiming, TraceDispatch, TraceReport } from '../types';
import { formatMillis, simpleEventName } from './format';
import { resolveListenerTimings } from './listenerData';

export interface TopOffender {
  plugin: string;
  event: string;
  timeNanos: number;
  tickPercent: number;
  status: 'critical' | 'warn' | 'ok';
}

export interface RecentTraceEntry {
  sequence: number;
  label: string;
  detail: string | null;
  startedAtMillis: number;
  durationNanos: number;
  severity: 'critical' | 'warn' | 'ok';
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

export function computeSessionMetrics(report: TraceReport): SessionMetrics {
  let listenersInvoked = 0;
  let flaggedListeners = 0;
  const offenderMap = new Map<string, TopOffender>();

  for (const dispatch of report.dispatches) {
    for (const timing of resolveListenerTimings(dispatch)) {
      listenersInvoked += 1;
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
        });
      }
    }
  }

  const topOffenders = [...offenderMap.values()]
    .sort((a, b) => b.timeNanos - a.timeNanos)
    .slice(0, 8);

  const recentTraces = [...report.dispatches]
    .sort((a, b) => b.sequence - a.sequence)
    .slice(0, 8)
    .map((dispatch) => traceEntryFromDispatch(dispatch));

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

function severityFor(timing: ListenerTiming, tickPercent: number): 'critical' | 'warn' | 'ok' {
  if (timing.exceedsSlowThreshold || tickPercent >= 10) {
    return 'critical';
  }
  if (tickPercent >= 3) {
    return 'warn';
  }
  return 'ok';
}

function traceEntryFromDispatch(dispatch: TraceDispatch): RecentTraceEntry {
  const slowest = [...dispatch.listenerTimings].sort((a, b) => b.durationNanos - a.durationNanos)[0];
  const tickPercent = (dispatch.durationNanos / TICK_BUDGET_NANOS) * 100;
  let severity: 'critical' | 'warn' | 'ok' = 'ok';
  if (tickPercent >= 15 || slowest?.exceedsSlowThreshold) {
    severity = 'critical';
  } else if (tickPercent >= 5) {
    severity = 'warn';
  }
  const eventLabel = simpleEventName(dispatch.eventClassName);
  const detail = dispatch.blockMaterial ?? dispatch.playerName ?? null;
  return {
    sequence: dispatch.sequence,
    label: eventLabel,
    detail,
    startedAtMillis: dispatch.startedAtMillis,
    durationNanos: dispatch.durationNanos,
    severity,
  };
}

export function statusLabel(status: 'critical' | 'warn' | 'ok'): string {
  if (status === 'critical') {
    return 'critical';
  }
  if (status === 'warn') {
    return 'warn';
  }
  return 'ok';
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
