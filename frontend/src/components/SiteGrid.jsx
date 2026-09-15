import { AreaChart, Area, ResponsiveContainer, YAxis } from "recharts";
import { MapPin } from "lucide-react";
import { api, usePolling } from "../api/client.js";

function SiteCard({ site }) {
  const { data: readings } = usePolling(() => api.readings(site.id, 20), 15000, [site.id]);
  const chartData = (readings ?? []).slice().reverse().map((r, i) => ({ i, kw: r.estimatedPowerKw }));
  const reading = site.latestReading;
  const stale = reading?.stale;

  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 transition hover:border-[var(--color-primary)]/40">
      <div className="mb-2 flex items-center justify-between">
        <div className="flex items-center gap-1.5 text-xs font-medium text-[var(--color-muted)]">
          <MapPin className="h-3.5 w-3.5" aria-hidden="true" /> {site.name}
        </div>
        {stale && (
          <span className="rounded-full border border-[var(--color-warning)]/40 bg-[var(--color-warning)]/10 px-2 py-0.5 text-[10px] font-medium text-[var(--color-warning)]">
            stale
          </span>
        )}
      </div>
      <p className="text-2xl font-bold text-[var(--color-text)] tabular-nums">
        {reading ? reading.estimatedPowerKw.toFixed(1) : "—"}
        <span className="ml-1 text-xs font-medium text-[var(--color-muted)]">kW</span>
      </p>
      <div className="mt-1 h-12">
        {chartData.length > 1 && (
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={chartData}>
              <YAxis hide domain={["dataMin", "dataMax"]} />
              <defs>
                <linearGradient id={`grad-${site.id}`} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="var(--color-primary)" stopOpacity={0.5} />
                  <stop offset="100%" stopColor="var(--color-primary)" stopOpacity={0} />
                </linearGradient>
              </defs>
              <Area
                type="monotone"
                dataKey="kw"
                stroke="var(--color-primary)"
                strokeWidth={1.5}
                fill={`url(#grad-${site.id})`}
                isAnimationActive={false}
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>
      <div className="mt-2 flex justify-between text-[11px] text-[var(--color-muted)]">
        <span>Irradiance: {reading ? `${reading.irradianceWm2.toFixed(0)} W/m²` : "—"}</span>
        <span>Capacity: {site.capacityKw} kW</span>
      </div>
    </div>
  );
}

export default function SiteGrid({ sites }) {
  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
      <h2 className="mb-3 text-sm font-semibold text-[var(--color-text)]">Monitored sites</h2>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {(sites ?? []).map((site) => (
          <SiteCard key={site.id} site={site} />
        ))}
      </div>
    </div>
  );
}
