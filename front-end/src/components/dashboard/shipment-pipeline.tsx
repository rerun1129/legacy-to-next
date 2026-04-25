import { pipelineData } from "@/lib/mock-data";

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
        {pipelineData.map((stage) => (
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
