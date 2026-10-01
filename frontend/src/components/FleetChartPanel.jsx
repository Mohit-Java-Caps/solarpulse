import { TrendingUp, ChevronDown } from "lucide-react";
import TrendChart from "./TrendChart.jsx";
import SiteDetailView from "./SiteDetailView.jsx";

// The "custom filter": a single dropdown that switches this panel
// between the fleet-wide comparison chart and one site's detail view.
// SiteGrid cards drive the same selectedSiteId state, so clicking a
// card and picking it from this dropdown do the same thing.
export default function FleetChartPanel({ sites, selectedSiteId, onSelectSite }) {
  const selectedSite = (sites ?? []).find((s) => s.id === selectedSiteId);

  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
      <div className="mb-3 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <TrendingUp className="h-4 w-4 text-[var(--color-primary)]" aria-hidden="true" />
          <h2 className="text-sm font-semibold text-[var(--color-text)]">
            {selectedSite ? "Site detail" : "Fleet output comparison"}
          </h2>
        </div>
        <div className="relative">
          <select
            value={selectedSiteId ?? ""}
            onChange={(e) => onSelectSite(e.target.value || null)}
            className="appearance-none rounded-lg border border-[var(--color-border)] bg-[var(--color-surface-2)] px-3 py-1.5 pr-7 text-xs font-medium text-[var(--color-text)] outline-none transition hover:border-[var(--color-primary)]"
          >
            <option value="">All sites (fleet view)</option>
            {(sites ?? []).map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          <ChevronDown className="pointer-events-none absolute right-2 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--color-muted)]" aria-hidden="true" />
        </div>
      </div>

      {selectedSite ? <SiteDetailView site={selectedSite} /> : <TrendChart sites={sites} />}
    </div>
  );
}
