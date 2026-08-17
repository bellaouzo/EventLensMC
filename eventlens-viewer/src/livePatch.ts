import type { ListenerChainEntry, ListenerTiming, TraceDispatch, TraceReport } from './types';

export interface LiveSessionUpdate {
  sessionId: string;
  eventClassName: string;
  state: string;
  capturedEvents: number;
  durationMillis: number;
  startedAtMillis: number;
  dispatch?: TraceDispatch;
}

export function parseLiveSessionUpdate(data: Record<string, unknown>): LiveSessionUpdate | null {
  if (typeof data.sessionId !== 'string') {
    return null;
  }
  const update: LiveSessionUpdate = {
    sessionId: data.sessionId,
    eventClassName: typeof data.eventClassName === 'string' ? data.eventClassName : '',
    state: typeof data.state === 'string' ? data.state : 'ACTIVE',
    capturedEvents: typeof data.capturedEvents === 'number' ? data.capturedEvents : 0,
    durationMillis: typeof data.durationMillis === 'number' ? data.durationMillis : 0,
    startedAtMillis: typeof data.startedAtMillis === 'number' ? data.startedAtMillis : Date.now(),
  };
  if (data.dispatch && typeof data.dispatch === 'object') {
    update.dispatch = parseLiveDispatch(data.dispatch as Record<string, unknown>);
  }
  return update;
}

function parseLiveDispatch(raw: Record<string, unknown>): TraceDispatch {
  const listenerTimings = Array.isArray(raw.listenerTimings)
    ? raw.listenerTimings
        .filter((entry): entry is Record<string, unknown> => typeof entry === 'object' && entry !== null)
        .map(parseListenerTiming)
    : [];

  const listenerChain = Array.isArray(raw.listenerChain)
    ? raw.listenerChain
        .filter((entry): entry is Record<string, unknown> => typeof entry === 'object' && entry !== null)
        .map(parseListenerChainEntry)
    : undefined;

  return {
    sequence: typeof raw.sequence === 'number' ? raw.sequence : 0,
    startedAtMillis: typeof raw.startedAtMillis === 'number' ? raw.startedAtMillis : 0,
    durationNanos: typeof raw.durationNanos === 'number' ? raw.durationNanos : 0,
    durationMillis: typeof raw.durationMillis === 'string' ? raw.durationMillis : '0ms',
    eventLensOverheadNanos: typeof raw.eventLensOverheadNanos === 'number' ? raw.eventLensOverheadNanos : 0,
    eventClassName: typeof raw.eventClassName === 'string' ? raw.eventClassName : '',
    cancelledAtStart: raw.cancelledAtStart === true,
    cancelledAtEnd: raw.cancelledAtEnd === true,
    playerName: typeof raw.playerName === 'string' ? raw.playerName : null,
    worldName: typeof raw.worldName === 'string' ? raw.worldName : null,
    blockX: typeof raw.blockX === 'number' ? raw.blockX : null,
    blockY: typeof raw.blockY === 'number' ? raw.blockY : null,
    blockZ: typeof raw.blockZ === 'number' ? raw.blockZ : null,
    listenerChain,
    listenerTimings,
  };
}

function parseListenerChainEntry(raw: Record<string, unknown>): ListenerChainEntry {
  return {
    registrationOrder: typeof raw.registrationOrder === 'number' ? raw.registrationOrder : undefined,
    pluginName: typeof raw.pluginName === 'string' ? raw.pluginName : '',
    listenerClassName: typeof raw.listenerClassName === 'string' ? raw.listenerClassName : '',
    methodName: typeof raw.methodName === 'string' ? raw.methodName : '',
    priority: typeof raw.priority === 'string' ? raw.priority : 'NORMAL',
  };
}

function parseListenerTiming(raw: Record<string, unknown>): ListenerTiming {
  return {
    invocationOrder: typeof raw.invocationOrder === 'number' ? raw.invocationOrder : 0,
    pluginName: typeof raw.pluginName === 'string' ? raw.pluginName : '',
    listenerClassName: typeof raw.listenerClassName === 'string' ? raw.listenerClassName : '',
    methodName: typeof raw.methodName === 'string' ? raw.methodName : '',
    priority: typeof raw.priority === 'string' ? raw.priority : 'NORMAL',
    durationNanos: typeof raw.durationNanos === 'number' ? raw.durationNanos : 0,
    durationMillis:
      typeof raw.durationMillis === 'string'
        ? raw.durationMillis
        : typeof raw.durationMillis === 'number'
          ? `${raw.durationMillis}ms`
          : '0ms',
    exceedsSlowThreshold: raw.exceedsSlowThreshold === true,
    threwException: raw.threwException === true,
    exceptionType: typeof raw.exceptionType === 'string' ? raw.exceptionType : null,
  };
}

export function applyLiveSessionUpdate(
  report: TraceReport | null,
  update: LiveSessionUpdate,
): TraceReport {
  const dispatch = update.dispatch;
  const base =
    report && report.session.sessionId === update.sessionId
      ? report
      : createShellReport(update);

  const dispatches = [...base.dispatches];
  if (dispatch) {
    const existingIndex = dispatches.findIndex((entry) => entry.sequence === dispatch.sequence);
    if (existingIndex >= 0) {
      dispatches[existingIndex] = mergeDispatch(dispatches[existingIndex], dispatch);
    } else {
      dispatches.push(dispatch);
    }
  }

  const capturedEvents = Math.max(update.capturedEvents, dispatches.length);

  return {
    ...base,
    session: {
      ...base.session,
      sessionId: update.sessionId,
      eventClassName: update.eventClassName || base.session.eventClassName,
      state: update.state,
      capturedEvents,
      durationMillis: update.durationMillis,
      startedAtMillis: update.startedAtMillis,
    },
    dispatches,
  };
}

function mergeDispatch(existing: TraceDispatch, incoming: TraceDispatch): TraceDispatch {
  const listenerTimings =
    incoming.listenerTimings.length > 0 ? incoming.listenerTimings : existing.listenerTimings;
  const listenerChain =
    incoming.listenerChain && incoming.listenerChain.length > 0
      ? incoming.listenerChain
      : existing.listenerChain;

  return {
    ...incoming,
    listenerTimings,
    listenerChain,
  };
}

function createShellReport(update: LiveSessionUpdate): TraceReport {
  return {
    reportVersion: 'live',
    redactionMode: 'live',
    session: {
      sessionId: update.sessionId,
      eventClassName: update.eventClassName,
      state: update.state,
      ownerName: '',
      startedAtMillis: update.startedAtMillis,
      durationMillis: update.durationMillis,
      capturedEvents: update.capturedEvents,
      droppedEvents: 0,
      sampledOutEvents: 0,
      filters: '',
    },
    warnings: [],
    dispatches: update.dispatch ? [update.dispatch] : [],
  };
}
