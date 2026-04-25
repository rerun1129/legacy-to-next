import { Download, Truck } from "lucide-react";
import { TruckBlGrid } from "@/components/fms/truck-bl/truck-bl-grid";

export default function TruckBLListPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Truck size={14} /></div>
          Truck B/L List
        </div>
        <div className="page-head__actions">
          <button className="btn btn--sm"><Download size={12} />Export</button>
        </div>
      </div>

      <div style={{ margin: "10px 14px 0", background: "var(--surface)", border: "1px solid var(--border)", borderRadius: "var(--radius)", padding: "12px 16px" }}>
        <div className="filter-grid">
          {[
            { l: "기간",         type: "daterange" },
            { l: "Truck B/L No", type: "text" },
            { l: "POL",          type: "text" },
            { l: "POD",          type: "text" },
          ].map((f) => (
            <div key={f.l} className="lcn">
              <span className="lcn__label">{f.l}</span>
              {f.type === "daterange"
                ? <div className="lcn__daterange" style={{ gridColumn: "2 / span 2" }}><input type="date" defaultValue="2026-04-01" /><span className="lcn__tilde">~</span><input type="date" defaultValue="2026-04-30" /></div>
                : <input className="lcn__name" placeholder={f.l} style={{ gridColumn: "2 / span 2" }} />
              }
            </div>
          ))}
        </div>
        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 10 }}>
          <button className="btn btn--sm">Reset</button>
          <button className="btn btn--sm btn--primary">Search</button>
        </div>
      </div>

      <div style={{ flex: 1, overflow: "auto", margin: "10px 14px 0", display: "flex", flexDirection: "column" }}>
        <TruckBlGrid />
      </div>

      <div className="footbar">
        <span style={{ color: "var(--ink-4)", fontSize: "var(--fs-xs)" }}>Truck B/L No 더블클릭 → Entry</span>
        <span style={{ marginLeft: "auto" }}>3 records</span>
      </div>
    </div>
  );
}
