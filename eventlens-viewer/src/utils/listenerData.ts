import type { ListenerChainEntry, ListenerTiming, TraceDispatch } from '../types';
import { formatMillis } from './format';

export type { ListenerChainEntry };

export function resolveListenerTimings(dispatch: TraceDispatch): ListenerTiming[] {
  if ((dispatch.listenerTimings?.length ?? 0) > 0) {
    return dispatch.listenerTimings;
  }

  const chain = dispatch.listenerChain ?? [];
  if (chain.length === 0) {
    return [];
  }

  const total = Math.max(dispatch.durationNanos, chain.length);
  const share = Math.floor(total / chain.length);

  return chain.map((entry, index) => {
    const durationNanos =
      index === chain.length - 1 ? total - share * (chain.length - 1) : share;
    return {
      invocationOrder: entry.registrationOrder ?? index + 1,
      pluginName: entry.pluginName,
      listenerClassName: entry.listenerClassName,
      methodName: entry.methodName,
      priority: entry.priority,
      durationNanos,
      durationMillis: formatMillis(durationNanos),
      exceedsSlowThreshold: false,
      threwException: false,
      exceptionType: null,
    };
  });
}

export function dispatchHasListenerDetail(dispatch: TraceDispatch): boolean {
  return dispatch.listenerTimings.length > 0 || (dispatch.listenerChain?.length ?? 0) > 0;
}
