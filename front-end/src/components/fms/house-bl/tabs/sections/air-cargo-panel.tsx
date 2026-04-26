import { PackageField } from "@/components/shared/panel-fields";

const LI_ST: React.CSSProperties = { width: "100%", height: 24, padding: "0 6px", fontSize: 10 };
const UNIT_SEL: React.CSSProperties = {
  height: 24, padding: "0 2px", fontSize: 10, flexShrink: 0, width: 44, outline: "none",
  border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)",
};

function GWField() {
  return (
    <div className="li">
      <span className="li__label">Gross W/T</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <input type="number" step="any" defaultValue="30600" style={{ ...LI_ST, flex: 1, width: undefined }} />
        <select style={UNIT_SEL}><option>KGS</option><option>LBS</option></select>
      </div>
    </div>
  );
}

export function AirCargoPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <div className="sched-list">
          <PackageField height={24} />
          <GWField />
          <div className="li"><span className="li__label">Volume W/T</span>
            <div className="li__input"><input type="number" step="any" defaultValue="14583" style={LI_ST} /></div>
          </div>
          <div className="li"><span className="li__label">Charge W/T</span>
            <div className="li__input"><input type="number" step="any" defaultValue="30600" style={LI_ST} /></div>
          </div>
          <div className="li"><span className="li__label">Rate Class</span>
            <div className="li__input"><input defaultValue="GCR" style={LI_ST} /></div>
          </div>
          <div className="li"><span className="li__label">CBM</span>
            <div className="li__input"><input type="number" step="any" defaultValue="87.5" style={LI_ST} /></div>
          </div>
        </div>
      </div>
    </div>
  );
}
