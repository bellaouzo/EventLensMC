import type { DashboardGraph, DashboardGraphEdge, DashboardGraphNode, TraceReport } from '../types';
import { escapeHtml, simpleEventName } from '../utils/format';
import { resolveListenerTimings } from '../utils/listenerData';
import { sessionStateBannerHtml } from '../utils/sessionState';

export function renderEventGraph(
  container: HTMLElement,
  graph: DashboardGraph,
  report: TraceReport | null,
): void {
  const embedded = embeddedEventGraph();
  const layout = embedded
    ? { nodes: embedded.nodes, edges: embedded.edges, eventLabel: embedded.title }
    : report
      ? graphFromReport(report)
      : filterGraph(graph, null);
  const eventName = report ? simpleEventName(report.session.eventClassName) : layout.eventLabel;

  if (!layout.nodes.length) {
    container.innerHTML = '<section class="page"><p class="empty">No event graph data available.</p></section>';
    return;
  }

  const nodes = layout.nodes
    .map((node) => {
      const count = node.kind === 'PLUGIN' ? ` x${node.weight}` : '';
      return `<div class="graph-pill${node.kind === 'EVENT' ? ' event' : ''}">${escapeHtml(node.label)}${count}<span class="graph-pill-kind">${node.kind}</span></div>`;
    })
    .join('');

  const edges = layout.edges
    .map((edge) => {
      const from = layout.nodes.find((node) => node.id === edge.sourceId);
      const to = layout.nodes.find((node) => node.id === edge.targetId);
      if (!from || !to) {
        return '';
      }
      return `<div class="graph-edge"><span>${escapeHtml(from.label)}</span><span class="graph-edge-action">+ ${escapeHtml(edge.label)} →</span><span>${escapeHtml(to.label)}</span><span class="graph-edge-weight">x${edge.weight}</span></div>`;
    })
    .join('');

  container.innerHTML = `
    <section class="page">
      ${report ? sessionStateBannerHtml(report.session.state) : ''}
      <header class="page-header">
        <h1>${escapeHtml(eventName || 'Event graph')} — listener relationships</h1>
      </header>
      <div>
        <div class="graph-section-label">Nodes</div>
        <div class="graph-nodes">${nodes}</div>
        <div class="graph-section-label">Edges</div>
        <div class="graph-edges">${edges || '<p class="empty">No listener edges for this event.</p>'}</div>
      </div>
    </section>
  `;
}

function embeddedEventGraph(): DashboardGraph | null {
  const graph = (window as Window & { __EVENTLENS_EVENT_GRAPH__?: DashboardGraph }).__EVENTLENS_EVENT_GRAPH__;
  if (graph && Array.isArray(graph.nodes) && graph.nodes.length > 0) {
    return graph;
  }
  return null;
}

function graphFromReport(report: TraceReport): {
  nodes: DashboardGraphNode[];
  edges: DashboardGraphEdge[];
  eventLabel: string;
} {
  const eventLabel = simpleEventName(report.session.eventClassName);
  const counts = new Map<string, number>();

  for (const dispatch of report.dispatches) {
    for (const timing of resolveListenerTimings(dispatch)) {
      counts.set(timing.pluginName, (counts.get(timing.pluginName) ?? 0) + 1);
    }
  }

  const eventId = `event:${eventLabel}`;
  const nodes: DashboardGraphNode[] = [{ id: eventId, label: eventLabel, kind: 'EVENT', weight: report.session.capturedEvents }];
  const edges: DashboardGraphEdge[] = [];

  for (const [plugin, count] of [...counts.entries()].sort((a, b) => b[1] - a[1])) {
    const pluginId = `plugin:${plugin}`;
    nodes.push({ id: pluginId, label: plugin, kind: 'PLUGIN', weight: count });
    edges.push({ sourceId: pluginId, targetId: eventId, weight: count, label: 'listens' });
  }

  return { nodes, edges, eventLabel };
}

function filterGraph(
  graph: DashboardGraph,
  rootEvent: string | null,
): { nodes: DashboardGraphNode[]; edges: DashboardGraphEdge[]; eventLabel: string } {
  const events = graph.nodes.filter((node) => node.kind === 'EVENT');
  const root =
    events.find((node) => rootEvent && node.label === rootEvent) ??
    [...events].sort((a, b) => b.weight - a.weight)[0] ??
    null;
  if (!root) {
    return { nodes: graph.nodes.slice(0, 8), edges: graph.edges.slice(0, 8), eventLabel: 'Events' };
  }

  const related = graph.edges.filter((edge) => edge.sourceId === root.id || edge.targetId === root.id);
  const ids = new Set<string>([root.id, ...related.flatMap((edge) => [edge.sourceId, edge.targetId])]);
  const nodes = graph.nodes.filter((node) => ids.has(node.id)).sort((a, b) => (a.kind === 'EVENT' ? -1 : b.kind === 'EVENT' ? 1 : b.weight - a.weight));
  return { nodes, edges: related, eventLabel: root.label };
}
