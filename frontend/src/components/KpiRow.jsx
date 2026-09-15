import { Zap, Gauge, Server, AlertTriangle } from "lucide-react";
import CountUp from "./CountUp.jsx";

const Tile = ({ icon: Icon, label, value, decimals, suffix, accent }) => (
  <div className="flex items-center gap-4 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 shadow-sm shadow-black/20">
    <span
      className="flex h-11 w-11 flex-none items-center justify-center rounded-xl"
      style={{ background: `${accent}1a`, color: accent }}
    >
      <Icon className="h-5 w-5" aria-hidden="true" />
    </span>
    <div>
      <p className="text-[11px] font-medium uppercase tracking-wide text-[var(--color-muted)]">{label}</p>
      <p className="text-2xl font-bold text-[var(--color-text)]">
        <CountUp value={value ?? 0} decimals={decimals} suffix={suffix} />
      </p>
    </div>
  </div>
);

export default function KpiRow({ summary }) {
  return (
    <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <Tile
        icon={Zap}
        label="Current output"
        value={summary?.totalCurrentOutputKw}
        decimals={1}
        suffix=" kW"
        accent="var(--color-primary)"
      />
      <Tile
        icon={Gauge}
        label="Fleet capacity"
        value={summary?.totalCapacityKw}
        decimals={0}
        suffix=" kW"
        accent="var(--color-secondary)"
      />
      <Tile
        icon={Server}
        label="Active sites"
        value={summary?.activeSiteCount}
        decimals={0}
        suffix={` / ${summary?.siteCount ?? 0}`}
        accent="var(--color-success)"
      />
      <Tile
        icon={AlertTriangle}
        label="Anomalies (24h)"
        value={summary?.anomaliesLast24h}
        decimals={0}
        suffix=""
        accent="var(--color-warning)"
      />
    </div>
  );
}
