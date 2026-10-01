import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { MapPin, AlertTriangle } from "lucide-react";
import { api, usePolling } from "../api/client.js";

// The "custom filter" drill-down: a bigger, single-site view with more
// history and that site's own anomaly log — everything here comes from
// endpoints that already existed for the fleet view (readings with a
// higher limit, anomalies scoped to one site), just presented for one
// site instead of five.
export default function SiteDetailView({ site }) {
  const { data: readings } = usePolling(() => api.readings(site.id, 50), 10000, [site.id]);
  const { data: anomalies } = usePolling(() => api.siteAnomalies(site.id), 10000, [site.id]);

  const chartData = (readings ?? [])
    .slice()
    .reverse()
    .map((r) => ({
      time: new Date(r.observedAt).toLocaleString(undefined, { month: "short", day: "numeric", hour: "2-digit" }),
      kw: Number(r.estimatedPowerKw.toFixed(1)),
    }));

  const latest = readings?.[0];

  return (
    <div>
      <div className="mb-3 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <MapPin className="h-4 w-4 text-[var(--color-primary)]" aria-hidden="true" />
          <h2 className="text-sm font-semibold text-[var(--color-text)]">{site.name}</h2>
          {latest?.stale && (
            <span className="rounded-full border border-[var(--color-warning)]/40 bg-[var(--color-warning)]/10 px-2 py-0.5 text-[10px] font-medium text-[var(--color-warning)]">
              stale
            </span>
          )}
        </div>
      </div>

      <div className="mb-3 grid grid-cols-4 gap-3 text-center">
        <div>
          <p className="text-lg font-bold text-[var(--color-text)] tabular-nums">
            {latest ? latest.estimatedPowerKw.toFixed(1) : "—"}
          </p>
          <p className="text-[10px] uppercase text-[var(--color-muted)]">Output (kW)</p>
        </div>
        <div>
          <p className="text-lg font-bold text-[var(--color-text)] tabular-nums">{site.capacityKw}</p>
          <p className="text-[10px] uppercase text-[var(--color-muted)]">Capacity (kW)</p>
        </div>
        <div>
          <p className="text-lg font-bold text-[var(--color-text)] tabular-nums">
            {latest ? latest.irradianceWm2.toFixed(0) : "—"}
          </p>
          <p className="text-[10px] uppercase text-[var(--color-muted)]">Irradiance (W/m²)</p>
        </div>
        <div>
          <p className="text-lg font-bold text-[var(--color-text)] tabular-nums">
            {latest ? `${latest.temperatureC.toFixed(1)}°` : "—"}
          </p>
          <p className="text-[10px] uppercase text-[var(--color-muted)]">Temperature</p>
        </div>
      </div>

      <div className="h-52">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={chartData}>
            <CartesianGrid stroke="var(--color-border)" strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="time" tick={{ fill: "var(--color-muted)", fontSize: 9 }} axisLine={false} tickLine={false} minTickGap={30} />
            <YAxis tick={{ fill: "var(--color-muted)", fontSize: 10 }} axisLine={false} tickLine={false} unit=" kW" />
            <Tooltip
              contentStyle={{ background: "var(--color-surface-2)", border: "1px solid var(--color-border)", borderRadius: 8 }}
              labelStyle={{ color: "var(--color-muted)" }}
            />
            <defs>
              <linearGradient id="detail-grad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="var(--color-primary)" stopOpacity={0.5} />
                <stop offset="100%" stopColor="var(--color-primary)" stopOpacity={0} />
              </linearGradient>
            </defs>
            <Area type="monotone" dataKey="kw" name={site.name} stroke="var(--color-primary)" strokeWidth={2} fill="url(#detail-grad)" isAnimationActive={false} />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div className="mt-3">
        <div className="mb-1.5 flex items-center gap-1.5 text-xs font-medium text-[var(--color-muted)]">
          <AlertTriangle className="h-3.5 w-3.5" aria-hidden="true" /> Anomalies for this site
        </div>
        {!anomalies || anomalies.length === 0 ? (
          <p className="text-xs text-[var(--color-muted)]">None flagged — within its rolling baseline.</p>
        ) : (
          <ul className="space-y-1.5">
            {anomalies.map((a, i) => (
              <li key={i} className="text-xs text-[var(--color-muted)]">
                <span className="font-mono text-[10px]">z={a.zScore.toFixed(2)}</span> — {a.reason}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
