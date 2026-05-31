"use client";

import { useTranslations } from "next-intl";
import { FX_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function ExchangeRates() {
  const t = useTranslations("fms.dashboard");

  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          {t("panels.exchangeRates")}
        </div>
        <div className="dash-panel__meta">{t("meta.krwBaseDaily")}</div>
      </div>
      <div className="fx-list">
        {FX_DATA.map((fx) => (
          <div key={fx.pair} className="fx-row">
            <div className="fx-row__pair">{fx.pair.split("/")[0]}</div>
            <div>
              <div style={{ fontSize: 11.5, fontWeight: 600 }}>{fx.pair}</div>
              <div className="fx-row__name">{fx.name}</div>
            </div>
            <div className="fx-row__rate">{fx.rate}</div>
            <div className={`fx-row__chg ${fx.dir}`}>
              {fx.dir === "up" ? "▲" : "▼"} {fx.chg}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
