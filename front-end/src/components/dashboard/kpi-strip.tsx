"use client";

import { useTranslations } from "next-intl";
import { KPI_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

// KPI_DATA 배열 순서와 1:1 대응하는 카탈로그 키
const STAT_KEYS = [
  "activeShipments",
  "etdThisWeek",
  "docsPending",
  "mtdInvoiced",
  "onTimePct",
] as const;

const variantClass: Record<string, string> = {
  default: "",
  ok:      "kpi--ok",
  warn:    "kpi--warn",
  danger:  "kpi--danger",
  neutral: "kpi--neutral",
};

export function KpiStrip() {
  const t = useTranslations("fms.dashboard.stats");

  return (
    <div className="kpi-strip">
      {KPI_DATA.map((kpi, i) => (
        <div key={kpi.label} className={`kpi ${variantClass[kpi.variant]}`}>
          <div className="kpi__label">{t(STAT_KEYS[i])}</div>
          <div className="kpi__value">{kpi.value}</div>
          <div className={`kpi__delta ${kpi.trend}`}>
            {kpi.trend === "up" ? "▲" : "▼"} {kpi.delta}
          </div>
        </div>
      ))}
    </div>
  );
}
