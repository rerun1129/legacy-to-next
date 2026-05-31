"use client";

import { useTranslations } from "next-intl";
import { WEEKLY_VOLUME_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function WeeklyVolume() {
  const t = useTranslations("fms.dashboard");
  const maxTotal = Math.max(...WEEKLY_VOLUME_DATA.map((d) => d.total));

  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          {t("panels.weeklyVolume")}
        </div>
        <div className="dash-panel__meta">{t("meta.last7Days")}</div>
      </div>
      <div className="wk-chart">
        {WEEKLY_VOLUME_DATA.map((d) => {
          const height = (d.total / maxTotal) * 100;
          const fclH = (d.fcl / d.total) * height;
          const lclH = (d.lcl / d.total) * height;
          const bulkH = (d.bulk / d.total) * height;
          return (
            <div key={d.label} className="wk-bar">
              <div className="wk-bar__value">{d.total}</div>
              <div className="wk-bar__stack">
                <div className="wk-bar__seg bulk" style={{ height: `${(bulkH / height) * 100}%` }} />
                <div className="wk-bar__seg lcl"  style={{ height: `${(lclH  / height) * 100}%` }} />
                <div className="wk-bar__seg fcl"  style={{ height: `${(fclH  / height) * 100}%` }} />
              </div>
              <div className="wk-bar__label">{d.label}</div>
            </div>
          );
        })}
      </div>
      <div className="wk-legend">
        <span><i className="fcl" />{t("volume.legend.fcl")}</span>
        <span><i className="lcl" />{t("volume.legend.lcl")}</span>
        <span><i className="bulk" />{t("volume.legend.bulk")}</span>
      </div>
    </div>
  );
}
