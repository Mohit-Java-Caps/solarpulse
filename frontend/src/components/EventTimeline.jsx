import { History } from "lucide-react";

const STATE_COLOR = {
  CLOSED: "var(--color-success)",
  OPEN: "var(--color-danger)",
  HALF_OPEN: "var(--color-warning)",
};

function timeAgo(iso) {
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
  if (seconds < 60) return `${seconds}s ago`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  return `${Math.floor(seconds / 3600)}h ago`;
}

// Renders /api/status/events — a real audit trail sourced from
// Resilience4j's own event publisher (see CircuitBreakerEventListener),
// not a UI-only log.
export default function EventTimeline({ events }) {
  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
      <div className="mb-3 flex items-center gap-2">
        <History className="h-4 w-4 text-[var(--color-secondary)]" aria-hidden="true" />
        <h2 className="text-sm font-semibold text-[var(--color-text)]">Circuit breaker events</h2>
      </div>
      {!events || events.length === 0 ? (
        <p className="text-xs text-[var(--color-muted)]">
          No state transitions yet — click "Inject failure" to see one happen live.
        </p>
      ) : (
        <ul className="space-y-2.5">
          {events.map((e, i) => (
            <li key={i} className="flex items-center gap-2.5 text-xs">
              <span className="h-1.5 w-1.5 flex-none rounded-full" style={{ background: STATE_COLOR[e.toState] }} />
              <span className="flex-1 text-[var(--color-text)]">
                <span className="text-[var(--color-muted)]">{e.fromState}</span>
                {" → "}
                <span style={{ color: STATE_COLOR[e.toState] }}>{e.toState}</span>
              </span>
              <span className="font-mono text-[10px] text-[var(--color-muted)]">{timeAgo(e.timestamp)}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
