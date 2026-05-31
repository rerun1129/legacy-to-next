"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Search } from "lucide-react";
import { TRACE_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function CargoTrace() {
  const t = useTranslations("fms.dashboard");
  const [query, setQuery] = useState("HBLKR24041801");

  return (
    <div className="dash-panel" style={{ minHeight: 260 }}>
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          {t("panels.cargoTrace")}
        </div>
      </div>
      <div className="trace-search">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={t("trace.placeholder")}
        />
        <button type="button" className="btn btn--sm btn--primary">
          <Search size={12} />
          {t("trace.button")}
        </button>
      </div>
      <div className="trace-steps">
        {TRACE_DATA.map((step) => (
          <div key={step.label} className={`trace-step ${step.state}`}>
            <div className="trace-step__dot" />
            <div>
              <div className="trace-step__label">{step.label}</div>
              <div className="trace-step__meta">{step.meta}</div>
            </div>
            <div className="trace-step__time">{step.time}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
