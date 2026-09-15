import { Sun, Github, ExternalLink } from "lucide-react";

export default function TopBar() {
  return (
    <header className="flex items-center justify-between border-b border-[var(--color-border)] px-6 py-4">
      <div className="flex items-center gap-3">
        <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-[var(--color-primary)] to-orange-600 text-white shadow-lg shadow-orange-900/30">
          <Sun className="h-5 w-5" aria-hidden="true" />
        </span>
        <div>
          <h1 className="text-base font-bold tracking-tight text-[var(--color-text)]">SolarPulse</h1>
          <p className="text-[11px] font-medium uppercase tracking-wider text-[var(--color-muted)]">
            Live resilience &amp; telemetry platform
          </p>
        </div>
      </div>
      <div className="flex items-center gap-2">
        <a
          href="/swagger-ui.html"
          className="hidden items-center gap-1.5 rounded-lg border border-[var(--color-border)] px-3 py-1.5 text-xs font-medium text-[var(--color-muted)] transition hover:border-[var(--color-secondary)] hover:text-[var(--color-secondary)] sm:flex"
        >
          API Docs <ExternalLink className="h-3.5 w-3.5" aria-hidden="true" />
        </a>
        <a
          href="https://github.com/Mohit-Java-Caps/solarpulse"
          target="_blank"
          rel="noreferrer"
          className="flex items-center gap-1.5 rounded-lg border border-[var(--color-border)] px-3 py-1.5 text-xs font-medium text-[var(--color-muted)] transition hover:border-[var(--color-primary)] hover:text-[var(--color-primary)]"
        >
          <Github className="h-3.5 w-3.5" aria-hidden="true" /> Source
        </a>
      </div>
    </header>
  );
}
