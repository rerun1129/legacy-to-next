"use client";

import { useState } from "react";
import { Search, RotateCcw, ChevronDown } from "lucide-react";

export function ListFilter() {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className={`search-card${collapsed ? " is-collapsed" : ""}`}>
      {!collapsed && (
        <div className="search-card__body">
          <div className="filter-grid">
            {/* Exp/Imp */}
            <div className="lcn">
              <span className="lcn__label">Exp/Imp</span>
              <select className="lcn__select" style={{ gridColumn: "2 / span 2" }}>
                <option value="">ALL</option>
                <option value="EXP">EXP (수출)</option>
                <option value="IMP">IMP (수입)</option>
              </select>
            </div>

            {/* ETD Range */}
            <div className="lcn">
              <span className="lcn__label">ETD</span>
              <div className="lcn__daterange">
                <input type="date" defaultValue="2026-04-01" />
                <span className="lcn__tilde">~</span>
                <input type="date" defaultValue="2026-04-30" />
              </div>
            </div>

            {/* HBL No */}
            <div className="lcn">
              <span className="lcn__label">HBL No</span>
              <input className="lcn__name" placeholder="HBL Number" style={{ gridColumn: "2 / span 2" }} />
            </div>

            {/* MBL No */}
            <div className="lcn">
              <span className="lcn__label">MBL No</span>
              <input className="lcn__name" placeholder="MBL Number" style={{ gridColumn: "2 / span 2" }} />
            </div>

            {/* Shipper */}
            <div className="lcn">
              <span className="lcn__label">Shipper</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input placeholder="Code" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)" }} />
              </div>
              <input className="lcn__name" placeholder="Shipper Name" />
            </div>

            {/* Consignee */}
            <div className="lcn">
              <span className="lcn__label">Consignee</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input placeholder="Code" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)" }} />
              </div>
              <input className="lcn__name" placeholder="Consignee Name" />
            </div>

            {/* Load Type */}
            <div className="lcn">
              <span className="lcn__label">Load Type</span>
              <select className="lcn__select" style={{ gridColumn: "2 / span 2" }}>
                <option value="">ALL</option>
                <option>FCL</option>
                <option>LCL</option>
                <option>BULK</option>
              </select>
            </div>

            {/* Vessel */}
            <div className="lcn">
              <span className="lcn__label">Vessel</span>
              <input className="lcn__name" placeholder="Vessel Name" style={{ gridColumn: "2 / span 2" }} />
            </div>

            {/* POL */}
            <div className="lcn">
              <span className="lcn__label">POL</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input placeholder="UNLOC" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)" }} />
              </div>
              <input className="lcn__name" placeholder="Port Name" />
            </div>

            {/* POD */}
            <div className="lcn">
              <span className="lcn__label">POD</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input placeholder="UNLOC" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)" }} />
              </div>
              <input className="lcn__name" placeholder="Port Name" />
            </div>

            {/* Operator */}
            <div className="lcn">
              <span className="lcn__label">Operator</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input placeholder="Code" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)" }} />
              </div>
              <input className="lcn__name" placeholder="Name" />
            </div>

            {/* Doc Status */}
            <div className="lcn">
              <span className="lcn__label">Doc Status</span>
              <select className="lcn__select" style={{ gridColumn: "2 / span 2" }}>
                <option value="">ALL</option>
                <option>Draft</option>
                <option>In Progress</option>
                <option>Confirmed</option>
              </select>
            </div>
          </div>

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
      )}

      <button
        className="search-card__collapse"
        onClick={() => setCollapsed((v) => !v)}
        title={collapsed ? "Expand filter" : "Collapse filter"}
      >
        <ChevronDown size={12} style={{ transform: collapsed ? "rotate(180deg)" : undefined, transition: "transform 200ms" }} />
      </button>
    </div>
  );
}
