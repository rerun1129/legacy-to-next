"use client";

import { useTranslations } from "next-intl";
import { PIPELINE_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function ShipmentPipeline() {
  const t = useTranslations("fms.dashboard");

  return (
    <div className="dash-panel" style={{ minHeight: 180 }}>
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          {t("panels.shipmentPipeline")}
        </div>
        <div className="dash-panel__meta">{t("meta.seaAir")}</div>
      </div>
      <div className="pipeline">
        {PIPELINE_DATA.map((stage) => (
          <div key={stage.label} className={`pipe-stage ${stage.variant}`}>
            <div className="pipe-stage__label">{stage.label}</div>
            <div className="pipe-stage__count">{stage.count}</div>
            <div className="pipe-stage__bar">
              <span style={{ width: `${stage.pct}%` }} />
            </div>
          </div>
        ))}
      </div>
      <div className="pipeline-legend">
        <span><i style={{ background: "var(--accent)" }} />{t("pipeline.legend.fcl")}</span>
        <span><i style={{ background: "color-mix(in oklch, var(--accent) 50%, white 50%)" }} />{t("pipeline.legend.lcl")}</span>
        <span><i style={{ background: "var(--warn)" }} />{t("pipeline.legend.bulkAir")}</span>
      </div>
    </div>
  );
}
