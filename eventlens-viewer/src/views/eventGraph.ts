import type { DashboardGraph, TraceReport } from '../types';
import { escapeHtml, simpleEventName } from '../utils/format';
import { sessionStateBannerHtml } from '../utils/sessionState';

interface FlowNode {
  id: string;
  label: string;
  sublabel: string;
  primary: boolean;
  x: number;
  y: number;
}

interface FlowEdge {
  from: string;
  to: string;
  primary: boolean;
}

export function renderEventGraph(
  container: HTMLElement,
  graph: DashboardGraph,
  report: TraceReport | null,
): void {
  const rootEvent = report ? simpleEventName(report.session.eventClassName) : null;
  const layout = buildFlowLayout(graph, rootEvent);

  if (!layout.nodes.length) {
    container.innerHTML = '<section class="page"><p class="empty">No event graph data available.</p></section>';
    return;
  }

  const width = 960;
  const height = 420;

  const edgesSvg = layout.edges
    .map((edge) => {
      const from = layout.nodes.find((n) => n.id === edge.from);
      const to = layout.nodes.find((n) => n.id === edge.to);
      if (!from || !to) {
        return '';
      }
      const x1 = from.x + 70;
      const y1 = from.y + 30;
      const x2 = to.x + 70;
      const y2 = to.y + 30;
      return `<line class="flow-edge${edge.primary ? ' primary' : ''}" x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" />`;
    })
    .join('');

  const nodesHtml = layout.nodes
    .map(
      (node) => `
      <div class="flow-node${node.primary ? ' primary' : ''}" style="left:${node.x}px;top:${node.y}px">
        <div class="flow-node-title">${escapeHtml(node.label)}</div>
        <div class="flow-node-sub">${escapeHtml(node.sublabel)}</div>
      </div>`,
    )
    .join('');

  const subtitle = rootEvent
    ? `Cascades observed downstream of ${rootEvent} this session. Node size = frequency.`
    : 'Event and plugin relationships across active trace sessions.';

  container.innerHTML = `
    <section class="page">
      <header class="page-header">
        <h1>Event graph</h1>
        <p class="page-subtitle">${subtitle}</p>
      </header>
      ${report ? sessionStateBannerHtml(report.session.state) : ''}
      <div class="flow-panel">
        <div class="flow-canvas" style="width:${width}px;height:${height}px">
          <svg class="flow-edges" width="${width}" height="${height}">${edgesSvg}</svg>
          ${nodesHtml}
        </div>
      </div>
    </section>
  `;
}

function buildFlowLayout(graph: DashboardGraph, rootEvent: string | null): { nodes: FlowNode[]; edges: FlowEdge[] } {
  const eventNodes = graph.nodes.filter((n) => n.kind === 'EVENT');
  const pluginNodes = graph.nodes.filter((n) => n.kind === 'PLUGIN');

  if (!eventNodes.length && !pluginNodes.length) {
    return { nodes: [], edges: [] };
  }

  const root =
    eventNodes.find((n) => rootEvent && n.label === rootEvent) ??
    eventNodes.sort((a, b) => b.weight - a.weight)[0] ??
    null;

  const downstream = eventNodes.filter((n) => n !== root).sort((a, b) => b.weight - a.weight).slice(0, 3);
  const chain = eventNodes.filter((n) => n !== root && !downstream.includes(n)).sort((a, b) => b.weight - a.weight).slice(0, 1);

  const nodes: FlowNode[] = [];
  const edges: FlowEdge[] = [];

  if (root) {
    nodes.push({
      id: root.id,
      label: root.label,
      sublabel: `${root.weight.toLocaleString()} / session`,
      primary: true,
      x: 20,
      y: 190,
    });
  }

  downstream.forEach((event, index) => {
    const yPositions = [60, 190, 320];
    nodes.push({
      id: event.id,
      label: event.label,
      sublabel: `${event.weight.toLocaleString()} observed`,
      primary: index === 0 && chain.length > 0,
      x: 270,
      y: yPositions[index] ?? 190,
    });
    if (root) {
      edges.push({ from: root.id, to: event.id, primary: index === 0 });
    }
  });

  if (chain.length && downstream[0]) {
    nodes.push({
      id: chain[0].id,
      label: chain[0].label,
      sublabel: `${chain[0].weight.toLocaleString()} observed`,
      primary: true,
      x: 530,
      y: 60,
    });
    edges.push({ from: downstream[0].id, to: chain[0].id, primary: true });

    const terminal = eventNodes.find((n) => !nodes.some((existing) => existing.id === n.id));
    if (terminal) {
      nodes.push({
        id: terminal.id,
        label: terminal.label,
        sublabel: `${terminal.weight.toLocaleString()} observed`,
        primary: false,
        x: 790,
        y: 60,
      });
      edges.push({ from: chain[0].id, to: terminal.id, primary: true });
    }
  }

  if (!downstream.length) {
    pluginNodes
      .sort((a, b) => b.weight - a.weight)
      .slice(0, 4)
      .forEach((plugin, index) => {
        const yPositions = [60, 190, 320, 190];
        nodes.push({
          id: plugin.id,
          label: plugin.label,
          sublabel: `${plugin.weight.toLocaleString()} invocations`,
          primary: index === 0,
          x: 270 + Math.floor(index / 2) * 260,
          y: yPositions[index] ?? 190,
        });
        if (root) {
          edges.push({ from: root.id, to: plugin.id, primary: index === 0 });
        }
      });
  }

  return { nodes, edges };
}
