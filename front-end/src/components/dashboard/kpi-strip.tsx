import { KPI_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

const variantClass: Record<string, string> = {
  default: "",
  ok:      "kpi--ok",
  warn:    "kpi--warn",
  danger:  "kpi--danger",
  neutral: "kpi--neutral",
};

export function KpiStrip() {
  return (
    <div className="kpi-strip">
      {KPI_DATA.map((kpi) => (
        <div key={kpi.label} className={`kpi ${variantClass[kpi.variant]}`}>
          <div className="kpi__label">{kpi.label}</div>
          <div className="kpi__value">{kpi.value}</div>
          <div className={`kpi__delta ${kpi.trend}`}>
            {kpi.trend === "up" ? "▲" : "▼"} {kpi.delta}
          </div>
        </div>
      ))}
    </div>
  );
}
