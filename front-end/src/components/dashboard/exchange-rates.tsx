import { fxData } from "@/lib/mock-data";

export function ExchangeRates() {
  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          Exchange Rates
        </div>
        <div className="dash-panel__meta">KRW base · Daily fix</div>
      </div>
      <div className="fx-list">
        {fxData.map((fx) => (
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
