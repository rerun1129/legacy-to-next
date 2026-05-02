import { PIPELINE_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function ShipmentPipeline() {
  return (
    <div className="dash-panel" style={{ minHeight: 180 }}>
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          Shipment Pipeline
        </div>
        <div className="dash-panel__meta">SEA + AIR</div>
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
        <span><i style={{ background: "var(--accent)" }} />FCL</span>
        <span><i style={{ background: "color-mix(in oklch, var(--accent) 50%, white 50%)" }} />LCL</span>
        <span><i style={{ background: "var(--warn)" }} />Bulk/Air</span>
      </div>
    </div>
  );
}
