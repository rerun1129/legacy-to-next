import {Package, RotateCcw, Search} from "lucide-react";
import { NonBlGrid } from "@/components/fms/non-bl/non-bl-grid";

export default function NonBLListPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          Non B/L List
        </div>
        <div className="page-head__actions">
            <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 12 }}>
                <button className="btn btn--sm btn--ghost">
                    <RotateCcw size={12} />
                    Reset
                </button>
                <button className="btn btn--sm btn--primary">
                    <Search size={12} />
                    Search
                </button>
            </div>
        </div>
      </div>

      <div style={{ margin: "10px 14px 0", background: "var(--surface)", border: "1px solid var(--border)", borderRadius: "var(--radius)", padding: "12px 16px" }}>
        <div className="filter-grid">
          {[
            { l: "기간",           type: "daterange" },
            { l: "Non B/L No",     type: "text" },
            { l: "Work Division",  type: "select" },
            { l: "Actual Customer",type: "text" },
          ].map((f) => (
            <div key={f.l} className="lcn">
              <span className="lcn__label">{f.l}</span>
              {f.type === "daterange"
                ? <div className="lcn__daterange" style={{ gridColumn: "2 / span 2" }}><input type="date" defaultValue="2026-04-01" /><span className="lcn__tilde">~</span><input type="date" defaultValue="2026-04-30" /></div>
                : f.type === "select"
                ? <select className="lcn__select" style={{ gridColumn: "2 / span 2" }}><option>ALL</option><option>Sea</option><option>Air</option><option>Warehouse</option><option>Trucking</option></select>
                : <input className="lcn__name" placeholder={f.l} style={{ gridColumn: "2 / span 2" }} />
              }
            </div>
          ))}
        </div>
      </div>

      <div style={{ flex: 1, overflow: "auto", margin: "10px 14px 0", display: "flex", flexDirection: "column" }}>
        <NonBlGrid />
      </div>
    </div>
  );
}
