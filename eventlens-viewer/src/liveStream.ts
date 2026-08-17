export type StreamEventHandler = (event: string, data: Record<string, unknown>) => void;

const FALLBACK_POLL_MS = 2000;

export function startLiveStream(onEvent: StreamEventHandler): {
  stop: () => void;
  connected: () => boolean;
} {
  let active = true;
  let connected = false;
  let source: EventSource | null = null;
  let fallbackTimer: number | null = null;

  const markConnected = () => {
    connected = true;
    if (fallbackTimer !== null) {
      window.clearInterval(fallbackTimer);
      fallbackTimer = null;
    }
  };

  const startFallbackPoll = () => {
    if (!active || fallbackTimer !== null) {
      return;
    }
    fallbackTimer = window.setInterval(() => {
      onEvent('poll', {});
    }, FALLBACK_POLL_MS);
  };

  const connect = () => {
    if (!active || source) {
      return;
    }
    source = new EventSource('/api/stream');

    source.onopen = () => {
      markConnected();
    };

    source.addEventListener('connected', () => {
      markConnected();
    });

    source.addEventListener('dispatch', (message) => {
      parseAndEmit(onEvent, 'dispatch', message.data);
    });

    source.addEventListener('session-started', (message) => {
      parseAndEmit(onEvent, 'session-started', message.data);
    });

    source.addEventListener('session-stopped', (message) => {
      parseAndEmit(onEvent, 'session-stopped', message.data);
    });

    source.onerror = () => {
      connected = false;
      source?.close();
      source = null;
      startFallbackPoll();
      if (active) {
        window.setTimeout(connect, 1000);
      }
    };
  };

  connect();

  return {
    stop: () => {
      active = false;
      connected = false;
      source?.close();
      source = null;
      if (fallbackTimer !== null) {
        window.clearInterval(fallbackTimer);
      }
    },
    connected: () => connected,
  };
}

function parseAndEmit(onEvent: StreamEventHandler, event: string, raw: string): void {
  try {
    onEvent(event, JSON.parse(raw) as Record<string, unknown>);
  } catch {
    onEvent(event, {});
  }
}
