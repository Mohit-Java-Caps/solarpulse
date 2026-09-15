import { api, usePolling } from "./api/client.js";
import TopBar from "./components/TopBar.jsx";
import KpiRow from "./components/KpiRow.jsx";
import CircuitBreakerPanel from "./components/CircuitBreakerPanel.jsx";
import EventTimeline from "./components/EventTimeline.jsx";
import SiteGrid from "./components/SiteGrid.jsx";
import TrendChart from "./components/TrendChart.jsx";
import AnomalyFeed from "./components/AnomalyFeed.jsx";
import SystemInfoFooter from "./components/SystemInfoFooter.jsx";

export default function App() {
  const { data: dashboard, refresh: refreshDashboard } = usePolling(api.dashboard, 10000);
  const { data: summary, refresh: refreshSummary } = usePolling(api.summary, 10000);
  const { data: events, refresh: refreshEvents } = usePolling(api.events, 10000);
  const { data: systemInfo } = usePolling(api.systemInfo, 30000);

  const refreshAll = () => {
    refreshDashboard();
    refreshSummary();
    refreshEvents();
  };

  const handleInjectFailure = () => api.injectFailure().then(refreshAll);
  const handleReset = () => api.reset().then(refreshAll);

  return (
    <div className="mx-auto flex min-h-screen max-w-6xl flex-col">
      <TopBar />

      <main className="flex-1 space-y-5 px-6 py-6">
        <KpiRow summary={summary} />

        <div className="grid grid-cols-1 gap-5 lg:grid-cols-3">
          <div className="lg:col-span-2">
            <TrendChart sites={dashboard?.sites} />
          </div>
          <div className="space-y-5">
            <CircuitBreakerPanel status={dashboard?.status} onInjectFailure={handleInjectFailure} onReset={handleReset} />
          </div>
        </div>

        <SiteGrid sites={dashboard?.sites} />

        <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
          <EventTimeline events={events} />
          <AnomalyFeed anomalies={dashboard?.recentAnomalies} />
        </div>
      </main>

      <SystemInfoFooter info={systemInfo} />
    </div>
  );
}
