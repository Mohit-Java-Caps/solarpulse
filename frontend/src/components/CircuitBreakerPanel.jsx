import { ShieldAlert, ShieldCheck, ShieldQuestion, Zap, RotateCcw } from "lucide-react";
import { motion } from "framer-motion";

const STATE_META = {
  CLOSED: { color: "var(--color-success)", icon: ShieldCheck, label: "Healthy" },
  OPEN: { color: "var(--color-danger)", icon: ShieldAlert, label: "Open — degraded" },
  HALF_OPEN: { color: "var(--color-warning)", icon: ShieldQuestion, label: "Probing recovery" },
};

export default function CircuitBreakerPanel({ status, onInjectFailure, onReset }) {
  const state = status?.circuitBreakerState ?? "CLOSED";
  const meta = STATE_META[state] ?? STATE_META.CLOSED;
  const Icon = meta.icon;

  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="text-sm font-semibold text-[var(--color-text)]">Resilience4j Circuit Breaker</h2>
          <p className="text-xs text-[var(--color-muted)]">Guarding the live Open-Meteo dependency</p>
        </div>
        <motion.span
          key={state}
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          className="flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold"
          style={{ background: `${meta.color}1f`, color: meta.color, border: `1px solid ${meta.color}55` }}
        >
          <Icon className="h-3.5 w-3.5" aria-hidden="true" /> {state}
        </motion.span>
      </div>

      <div className="mb-4 grid grid-cols-3 gap-3 text-center">
        <div>
          <p className="text-lg font-bold text-[var(--color-text)] tabular-nums">
            {status?.failureRate >= 0 ? `${status.failureRate.toFixed(0)}%` : "—"}
          </p>
          <p className="text-[10px] uppercase text-[var(--color-muted)]">Failure rate</p>
        </div>
        <div>
          <p className="text-lg font-bold text-[var(--color-text)] tabular-nums">{status?.bufferedCalls ?? 0}</p>
          <p className="text-[10px] uppercase text-[var(--color-muted)]">Buffered calls</p>
        </div>
        <div>
          <p className="text-lg font-bold text-[var(--color-text)] tabular-nums">{status?.successfulCalls ?? 0}</p>
          <p className="text-[10px] uppercase text-[var(--color-muted)]">Successful</p>
        </div>
      </div>

      <p className="mb-3 text-[11px] leading-relaxed text-[var(--color-muted)]">
        {meta.label} — {state === "OPEN"
          ? "requests are being served from the last-known-good cache."
          : "requests are hitting the live public API."}
      </p>

      <div className="flex gap-2">
        <button
          onClick={onInjectFailure}
          className="flex flex-1 items-center justify-center gap-1.5 rounded-xl border border-[var(--color-danger)]/40 bg-[var(--color-danger)]/10 px-3 py-2 text-xs font-semibold text-[var(--color-danger)] transition hover:bg-[var(--color-danger)]/20"
        >
          <Zap className="h-3.5 w-3.5" aria-hidden="true" /> Inject failure
        </button>
        <button
          onClick={onReset}
          className="flex flex-1 items-center justify-center gap-1.5 rounded-xl border border-[var(--color-border)] px-3 py-2 text-xs font-semibold text-[var(--color-text)] transition hover:border-[var(--color-success)] hover:text-[var(--color-success)]"
        >
          <RotateCcw className="h-3.5 w-3.5" aria-hidden="true" /> Reset
        </button>
      </div>
    </div>
  );
}
