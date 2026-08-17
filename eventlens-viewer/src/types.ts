export interface DurationStats {
  count: number;
  minNanos: number;
  maxNanos: number;
  avgNanos: number;
  p50Nanos: number;
  p95Nanos: number;
  p99Nanos: number;
}

export interface ListenerChainEntry {
  registrationOrder?: number;
  pluginName: string;
  listenerClassName: string;
  methodName: string;
  priority: string;
}

export interface ListenerTiming {
  invocationOrder: number;
  pluginName: string;
  listenerClassName: string;
  methodName: string;
  priority: string;
  durationNanos: number;
  durationMillis: string;
  exceedsSlowThreshold: boolean;
  threwException: boolean;
  exceptionType?: string | null;
}

export interface TraceDispatch {
  sequence: number;
  startedAtMillis: number;
  durationNanos: number;
  durationMillis: string;
  eventLensOverheadNanos: number;
  eventClassName: string;
  cancelledAtStart: boolean;
  cancelledAtEnd: boolean;
  playerName?: string | null;
  worldName?: string | null;
  blockMaterial?: string | null;
  blockX?: number | null;
  blockY?: number | null;
  blockZ?: number | null;
  listenerChain?: ListenerChainEntry[];
  listenerTimings: ListenerTiming[];
}

export interface TraceReport {
  reportVersion: string;
  redactionMode: string;
  instrumentation?: {
    agentPresent: boolean;
    protocolVersion: number;
    mode: string;
  };
  environment?: {
    paperVersion?: string;
    serverVersion: string;
    eventLensVersion: string;
    platformLabel?: string;
    runtimeKind?: 'paper' | 'neoforge' | 'forge' | 'fabric';
    loaderVersion?: string;
    modVersions?: Record<string, string>;
    pluginVersions?: Record<string, string>;
  };
  session: {
    sessionId: string;
    eventClassName: string;
    state: string;
    ownerName: string;
    startedAtMillis: number;
    durationMillis: number;
    capturedEvents: number;
    droppedEvents: number;
    sampledOutEvents: number;
    filters: string;
  };
  sessionTimingSummary?: {
    dispatchStats: DurationStats;
    eventLensOverheadStats: DurationStats;
    slowestListeners: Array<{
      identity: { pluginName: string; listenerClassName: string; methodName: string };
      stats: DurationStats;
    }>;
    slowestPlugins: Array<{ pluginName: string; stats: DurationStats }>;
    frequentListenerWarnings: string[];
  } | null;
  warnings: string[];
  dispatches: TraceDispatch[];
}

export interface DashboardGraphNode {
  id: string;
  label: string;
  kind: 'EVENT' | 'PLUGIN';
  weight: number;
}

export interface DashboardGraphEdge {
  sourceId: string;
  targetId: string;
  weight: number;
  label: string;
}

export interface DashboardGraph {
  title: string;
  truncated: boolean;
  nodes: DashboardGraphNode[];
  edges: DashboardGraphEdge[];
}

export interface DashboardSession {
  sessionId: string;
  eventClassName: string;
  state: string;
  capturedEvents: number;
  startedAtMillis: number;
}

export interface DashboardReportEntry {
  fileName: string;
  lastModifiedMillis: number;
  sizeBytes: number;
  format: string;
}

export interface DashboardStatus {
  dashboardEnabled: boolean;
  port: number;
  bindAddress: string;
  agentPresent: boolean;
  protocolVersion: number;
  paperVersion: string;
  eventLensVersion: string;
  defaultWorldName: string;
  defaultGameMode: string;
  onlinePlayers: number;
  tps: number;
  serverTimeMillis: number;
  activeTraceSessionId: string;
  activeTraceStartedAtMillis: number;
  activeTraceCapturedEvents: number;
  activeTraceEventClassName: string;
}
