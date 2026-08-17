import type { DashboardGraph, DashboardReportEntry, DashboardSession, DashboardStatus, TraceReport } from './types';

const liveApi = (): boolean =>
  window.location.protocol.startsWith('http') && window.location.port !== '';

export async function fetchStatus(): Promise<DashboardStatus> {
  const response = await fetch('/api/status');
  return response.json();
}

export async function fetchSessions(): Promise<DashboardSession[]> {
  const response = await fetch('/api/sessions');
  const payload = await response.json();
  return payload.sessions ?? [];
}

export async function fetchReports(): Promise<DashboardReportEntry[]> {
  const response = await fetch('/api/reports');
  const payload = await response.json();
  return payload.reports ?? [];
}

export async function fetchSessionReport(sessionId: string): Promise<TraceReport> {
  const response = await fetch(`/api/sessions/${encodeURIComponent(sessionId)}/report`);
  if (!response.ok) {
    throw new Error('Session report unavailable');
  }
  return response.json();
}

export async function fetchReportFile(fileName: string): Promise<TraceReport> {
  const response = await fetch(`/api/reports/${encodeURIComponent(fileName)}`);
  if (!response.ok) {
    throw new Error('Report file unavailable');
  }
  return response.json();
}

export async function fetchEventGraph(): Promise<DashboardGraph> {
  const response = await fetch('/api/graph/events');
  return response.json();
}

export async function fetchPluginGraph(sessionId?: string): Promise<DashboardGraph> {
  const query = sessionId ? `?session=${encodeURIComponent(sessionId)}` : '';
  const response = await fetch(`/api/graph/plugins${query}`);
  return response.json();
}

export function isLiveMode(): boolean {
  return liveApi();
}

export function parseReportJson(text: string): TraceReport {
  return JSON.parse(text) as TraceReport;
}

export { formatNanos, simpleEventName } from './utils/format';
