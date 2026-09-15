import { useEffect, useState, useCallback } from "react";

async function getJson(path) {
  const res = await fetch(path);
  if (!res.ok) throw new Error(`${path} -> ${res.status}`);
  return res.json();
}

export const api = {
  dashboard: () => getJson("/api/dashboard"),
  summary: () => getJson("/api/analytics/summary"),
  events: () => getJson("/api/status/events"),
  systemInfo: () => getJson("/api/system/info"),
  readings: (siteId, limit = 30) => getJson(`/api/sites/${siteId}/readings?limit=${limit}`),
  injectFailure: () => fetch("/api/admin/inject-failure", { method: "POST" }).then((r) => r.json()),
  reset: () => fetch("/api/admin/reset", { method: "POST" }).then((r) => r.json()),
};

// Polls a fetcher every `intervalMs` and exposes {data, error, loading, refresh}.
// One hook, reused by every panel — keeps polling behavior consistent
// instead of each component reinventing its own interval/cleanup logic.
export function usePolling(fetcher, intervalMs = 10000, deps = []) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(() => {
    fetcher()
      .then((d) => {
        setData(d);
        setError(null);
      })
      .catch((e) => setError(e))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    refresh();
    const id = setInterval(refresh, intervalMs);
    return () => clearInterval(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refresh, intervalMs]);

  return { data, error, loading, refresh };
}
