function formatUptime(seconds) {
  if (!seconds) return "—";
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
}

// Surfaces real /api/system/info values — nothing here is hardcoded in
// the UI, so it can never silently drift from what the backend is
// actually configured with.
export default function SystemInfoFooter({ info }) {
  return (
    <footer className="mt-2 flex flex-wrap items-center justify-center gap-x-6 gap-y-1 border-t border-[var(--color-border)] px-6 py-3 text-[11px] text-[var(--color-muted)]">
      <span>v{info?.version ?? "—"}</span>
      <span>Profile: {info?.activeProfile ?? "—"}</span>
      <span>Uptime: {formatUptime(info?.uptimeSeconds)}</span>
      <span>
        Circuit breaker: {info?.resilience?.failureRateThreshold ?? "—"}% threshold · {info?.resilience?.slidingWindowSize ?? "—"}-call window · {info?.resilience?.waitDurationInOpenState ?? "—"} recovery wait
      </span>
    </footer>
  );
}
