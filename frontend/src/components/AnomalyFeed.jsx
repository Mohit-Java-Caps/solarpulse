import { AlertTriangle, CheckCircle2 } from "lucide-react";

export default function AnomalyFeed({ anomalies }) {
  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
      <div className="mb-3 flex items-center gap-2">
        <AlertTriangle className="h-4 w-4 text-[var(--color-warning)]" aria-hidden="true" />
        <h2 className="text-sm font-semibold text-[var(--color-text)]">Anomaly flags</h2>
      </div>
      {!anomalies || anomalies.length === 0 ? (
        <div className="flex items-center gap-2 text-xs text-[var(--color-muted)]">
          <CheckCircle2 className="h-4 w-4 text-[var(--color-success)]" aria-hidden="true" />
          No anomalies flagged — every site is within its rolling baseline.
        </div>
      ) : (
        <ul className="space-y-2">
          {anomalies.map((a, i) => (
            <li key={i} className="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-2)] p-2.5 text-xs">
              <div className="mb-0.5 flex items-center justify-between">
                <span className="font-semibold text-[var(--color-text)]">{a.siteId}</span>
                <span className="font-mono text-[10px] text-[var(--color-muted)]">z={a.zScore.toFixed(2)}</span>
              </div>
              <p className="text-[var(--color-muted)]">{a.reason}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
