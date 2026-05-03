"use client";

import { useState } from "react";
import { ChevronUp, RotateCcw, Search } from "lucide-react";
import type { MasterBlFilter } from "@/domain/master-bl";

interface Props {
  onSearch: (filter: Partial<MasterBlFilter>) => void;
  onReset: () => void;
}

export function MasterBlListFilter({ onSearch, onReset }: Props) {
  const [collapsed, setCollapsed] = useState(false);
  const [mblNo, setMblNo] = useState('');
  const [shipperCode, setShipperCode] = useState('');
  const [consigneeCode, setConsigneeCode] = useState('');
  const [polCode, setPolCode] = useState('');
  const [podCode, setPodCode] = useState('');
  const [etdFrom, setEtdFrom] = useState('');
  const [etdTo, setEtdTo] = useState('');

  function handleSearch() {
    onSearch({
      mblNo: mblNo || undefined,
      shipperCode: shipperCode || undefined,
      consigneeCode: consigneeCode || undefined,
      polCode: polCode || undefined,
      podCode: podCode || undefined,
      etdFrom: etdFrom || undefined,
      etdTo: etdTo || undefined,
    });
  }

  function handleReset() {
    setMblNo('');
    setShipperCode('');
    setConsigneeCode('');
    setPolCode('');
    setPodCode('');
    setEtdFrom('');
    setEtdTo('');
    onReset();
  }

  return (
    <div className={`search-card${collapsed ? " is-collapsed" : ""}`}>
      {!collapsed && (
        <div className="search-card__body">
          <div className="filter-grid">
            {/* ETD Range */}
            <div className="lcn">
              <span className="lcn__label">ETD</span>
              <div className="lcn__daterange">
                <input type="date" value={etdFrom} onChange={(e) => setEtdFrom(e.target.value)} />
                <span className="lcn__tilde">~</span>
                <input type="date" value={etdTo} onChange={(e) => setEtdTo(e.target.value)} />
              </div>
            </div>

            {/* MBL No */}
            <div className="lcn">
              <span className="lcn__label">MBL No</span>
              <input
                className="lcn__name"
                placeholder="MBL Number"
                style={{ gridColumn: "2 / span 2" }}
                value={mblNo}
                onChange={(e) => setMblNo(e.target.value)}
              />
            </div>

            {/* Shipper */}
            <div className="lcn">
              <span className="lcn__label">Shipper</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input
                  placeholder="Code"
                  style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 12, fontFamily: "var(--font-mono)" }}
                  value={shipperCode}
                  onChange={(e) => setShipperCode(e.target.value)}
                />
              </div>
              <input className="lcn__name" placeholder="Shipper Name" readOnly tabIndex={-1} />
            </div>

            {/* Consignee */}
            <div className="lcn">
              <span className="lcn__label">Consignee</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input
                  placeholder="Code"
                  style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 12, fontFamily: "var(--font-mono)" }}
                  value={consigneeCode}
                  onChange={(e) => setConsigneeCode(e.target.value)}
                />
              </div>
              <input className="lcn__name" placeholder="Consignee Name" readOnly tabIndex={-1} />
            </div>

            {/* POL */}
            <div className="lcn">
              <span className="lcn__label">POL</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input
                  placeholder="UNLOC"
                  style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 12, fontFamily: "var(--font-mono)" }}
                  value={polCode}
                  onChange={(e) => setPolCode(e.target.value)}
                />
              </div>
              <input className="lcn__name" placeholder="Port Name" readOnly tabIndex={-1} />
            </div>

            {/* POD */}
            <div className="lcn">
              <span className="lcn__label">POD</span>
              <div className="lcn__code" style={{ position: "relative" }}>
                <input
                  placeholder="UNLOC"
                  style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 12, fontFamily: "var(--font-mono)" }}
                  value={podCode}
                  onChange={(e) => setPodCode(e.target.value)}
                />
              </div>
              <input className="lcn__name" placeholder="Port Name" readOnly tabIndex={-1} />
            </div>
          </div>

          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 8, paddingBottom: 4 }}>
            <button className="btn btn--sm btn--ghost" onClick={handleReset}>
              <RotateCcw size={12} />
              Reset
            </button>
            <button className="btn btn--sm btn--primary" onClick={handleSearch}>
              <Search size={12} />
              Search
            </button>
          </div>
        </div>
      )}

      <button
        className="search-card__collapse"
        onClick={() => setCollapsed((v) => !v)}
        title={collapsed ? "Collapse filter" : "Expand filter"}
      >
        <ChevronUp size={12} style={{ transform: collapsed ? "rotate(180deg)" : undefined, transition: "transform 200ms" }} />
      </button>
    </div>
  );
}
