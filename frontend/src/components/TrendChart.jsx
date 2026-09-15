import { useEffect, useState } from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { TrendingUp } from "lucide-react";
import { api } from "../api/client.js";

const COLORS = ["#f5a623", "#38bdf8", "#34d399", "#a78bfa", "#fb7185"];

// Fetches recent history for every site and merges it into one
// index-aligned dataset for a multi-line comparison chart. Index-based
// (not timestamp-based) on purpose: sites poll independently and their
// exact timestamps drift slightly, so "N readings ago" is the honest,
// simple way to compare shape/scale across sites without implying a
// false precision the data doesn't have.
export default function TrendChart({ sites }) {
  const [series, setSeries] = useState({});

  useEffect(() => {
    if (!sites || sites.length === 0) return;
    let cancelled = false;
    Promise.all(sites.map((s) => api.readings(s.id, 24).then((r) => [s.id, r]))).then((pairs) => {
      if (cancelled) return;
      const next = {};
      for (const [id, readings] of pairs) next[id] = readings;
      setSeries(next);
    });
    return () => {
      cancelled = true;
    };
  }, [sites]);

  const maxLen = Math.max(0, ...Object.values(series).map((r) => r.length));
  const chartData = Array.from({ length: maxLen }, (_, idx) => {
    const point = { label: `-${maxLen - idx}` };
    for (const site of sites ?? []) {
      const readings = series[site.id] ?? [];
      const reading = readings[readings.length - 1 - idx];
      if (reading) point[site.id] = Number(reading.estimatedPowerKw.toFixed(1));
    }
    return point;
  });

  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
      <div className="mb-3 flex items-center gap-2">
        <TrendingUp className="h-4 w-4 text-[var(--color-primary)]" aria-hidden="true" />
        <h2 className="text-sm font-semibold text-[var(--color-text)]">Fleet output comparison</h2>
      </div>
      <div className="h-64">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={chartData}>
            <CartesianGrid stroke="var(--color-border)" strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="label" tick={{ fill: "var(--color-muted)", fontSize: 10 }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fill: "var(--color-muted)", fontSize: 10 }} axisLine={false} tickLine={false} unit=" kW" />
            <Tooltip
              contentStyle={{ background: "var(--color-surface-2)", border: "1px solid var(--color-border)", borderRadius: 8 }}
              labelStyle={{ color: "var(--color-muted)" }}
            />
            <Legend wrapperStyle={{ fontSize: 11 }} />
            {(sites ?? []).map((site, i) => (
              <Line
                key={site.id}
                type="monotone"
                dataKey={site.id}
                name={site.name}
                stroke={COLORS[i % COLORS.length]}
                strokeWidth={2}
                dot={false}
                isAnimationActive={false}
                connectNulls
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
