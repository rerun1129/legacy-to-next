"use client";

import { useState } from "react";
import { Save, Trash2, Package, Printer } from "lucide-react";
import { FreightTab } from "@/components/fms/house-bl/tabs/freight-tab";

type WorkDiv = "Sea" | "Air" | "Warehouse" | "Trucking";

export function NonBLEntry() {
  const [tab,     setTab]     = useState("main");
  const [workDiv, setWorkDiv] = useState<WorkDiv>("Sea");

  return (
    <>
      {/* Page header — Print button EXISTS for Non B/L (부록 §C) */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          Non B/L Entry
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
          <button className="btn btn--sm btn--danger"><Trash2 size={12} />Delete</button>
          <button className="btn btn--sm btn--success"><Printer size={12} />Print</button>
          <button className="btn btn--sm btn--primary">
            <Save size={12} />Save<span className="btn__kbd">⌘S</span>
          </button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(6, 1fr)" }}>
        {[
          { l: "Non B/L No",   v: "",     req: true  },
          { l: "Work Division",v: workDiv,req: true  },
          { l: "Status",       v: "접수", req: false },
          { l: "Ref. No.",     v: "",     req: false },
          { l: "Operator",     v: "KYS",  req: true  },
          { l: "Team",         v: "OPS",  req: true  },
        ].map((f) => (
          <div key={f.l} className={`field${f.req ? " is-required" : ""}`}>
            <div className={`field__label${f.req ? " is-required" : ""}`}>{f.l}</div>
            <div className="field__input">
              {f.l === "Work Division" ? (
                <select value={workDiv} onChange={(e) => setWorkDiv(e.target.value as WorkDiv)} style={{ all: "unset", flex: 1, minWidth: 0, fontSize: "var(--fs-base)", color: "var(--ink)", cursor: "pointer" }}>
                  <option>Sea</option><option>Air</option><option>Warehouse</option><option>Trucking</option>
                </select>
              ) : (
                <input defaultValue={f.v} placeholder={f.l === "Non B/L No" ? "Auto on save" : f.v || f.l} />
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Tabbar */}
      <div className="tabbar">
        {[{ key: "main", label: "Main" }, { key: "freight", label: "Freight" }].map((t) => (
          <button key={t.key} className={`tabbar__tab${tab === t.key ? " is-active" : ""}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
        <div className="tabbar__spacer" />
        <div className="tabbar__meta"><span>Work Division: <strong>{workDiv}</strong></span></div>
      </div>

      {/* Main tab */}
      {tab === "main" && (
        <div style={{ flex: 1, overflow: "auto", padding: "12px 16px", display: "grid", gridTemplateColumns: "1.1fr 0.9fr 0.9fr", gap: 10, alignContent: "start" }}>

          {/* PARTY: 5 slots, address block 생략 */}
          <div className="panel" style={{ gridRow: "1 / 3" }}>
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party <span style={{ color: "var(--ink-3)", fontWeight: 400, fontSize: 10 }}>(주소 생략 — 거래처 코드+Name만)</span></span></div>
            <div className="panel__body">
              {([
                { role: "ACTUAL CUSTOMER", req: true,  btn: null           },
                { role: "SHIPPER",         req: false, btn: null           },
                { role: "CONSIGNEE",       req: false, btn: "To Order"     },
                { role: "NOTIFY",          req: false, btn: "Same as Cne." },
                { role: "SETTLE PARTNER",  req: false, btn: null           },
              ] as const).map((p) => (
                <div key={p.role} className="party-block" style={{ paddingBottom: 8 }}>
                  <div className="party-block__head">
                    <span style={{ fontSize: 11.5, fontWeight: 600, color: "var(--ink)", minWidth: 120, flexShrink: 0 }}>
                      {p.role}{p.req && <span style={{ color: "var(--required)", marginLeft: 3 }}>*</span>}
                    </span>
                    <div style={{ display: "grid", gridTemplateColumns: "90px 1fr", gap: 6, flex: "1 1 auto", alignItems: "center" }}>
                      <input placeholder="Code" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 11, color: "var(--ink)", outline: "none", fontFamily: "var(--font-mono)" }} />
                      <input placeholder="Company Name" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 11, color: "var(--ink)", outline: "none" }} />
                    </div>
                    {p.btn && (
                      <div className="party-block__head-actions">
                        <button className="party-block__head-btn">{p.btn}</button>
                      </div>
                    )}
                  </div>
                  {/* No address block for Non B/L */}
                </div>
              ))}
            </div>
          </div>

          {/* SCHEDULE */}
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
            <div className="panel__body">
              <div className="sched-list">
                <div className="sched-pair">
                  {[{ l: "ETD *", v: "2026-04-24" }, { l: "ETA *", v: "2026-05-08" }].map((f) => (
                    <div key={f.l} className="li">
                      <span className="li__label is-required">{f.l}</span>
                      <div className="li__input"><input type="date" defaultValue={f.v} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, outline: "none" }} /></div>
                    </div>
                  ))}
                </div>
                {[{ l: "POL *", c: "KRBSAN", n: "Busan" }, { l: "POD *", c: "CNSHA", n: "Shanghai" }].map((f) => (
                  <div key={f.l} className="lcn" style={{ marginBottom: 4 }}>
                    <span className="lcn__label">{f.l}</span>
                    <div className="lcn__code" style={{ position: "relative" }}><input defaultValue={f.c} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, fontFamily: "var(--font-mono)", outline: "none" }} /></div>
                    <input className="lcn__name" defaultValue={f.n} />
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Work Division dynamic block */}
          <div className="panel">
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">
                {workDiv === "Sea"       ? "Container Info (E-10)"    :
                 workDiv === "Air"       ? "Dimension / Cargo (E-11)" :
                 workDiv === "Warehouse" ? "Warehouse Info (E-24)"    :
                                          "Truck Info (E-20)"}
              </span>
            </div>
            <div className="panel__body">
              {workDiv === "Sea" && (
                <div style={{ overflowX: "auto" }}>
                  <table className="grid"><thead><tr><th>#</th><th>Container No.</th><th>Type</th><th className="is-num">Pkg</th><th className="is-num">G/W</th><th className="is-num">CBM</th></tr></thead>
                  <tbody><tr><td colSpan={6} className="grid__empty">+ Add Container</td></tr></tbody></table>
                </div>
              )}
              {workDiv === "Air" && (
                <div>
                  <div className="subhead"><div className="subhead__bar" />Cargo</div>
                  <div className="sched-list">
                    {["Package/Unit", "Gross W/T", "Charge W/T", "Rate Class", "CBM"].map((f) => (
                      <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, outline: "none" }} /></div></div>
                    ))}
                  </div>
                </div>
              )}
              {workDiv === "Warehouse" && (
                <div className="sched-list">
                  {["창고 코드", "입고일", "출고 예정일", "보관 위치", "면적(m²)"].map((f) => (
                    <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, outline: "none" }} /></div></div>
                  ))}
                </div>
              )}
              {workDiv === "Trucking" && (
                <div className="sched-list">
                  {["Pick-up Date", "Trucker", "Trucker PIC", "차량번호"].map((f) => (
                    <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, outline: "none" }} /></div></div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Reference Numbers */}
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Reference Numbers</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {["원본 B/L No.", "PO No.", "Invoice No.", "Customer Ref"].map((f) => (
                  <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, outline: "none" }} /></div></div>
                ))}
              </div>
            </div>
          </div>

          {/* Performance — Sales Man 필수 아님 */}
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Performance</span><span style={{ fontSize: 10, color: "var(--ink-4)", marginLeft: 6 }}>Sales Man 선택</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {[
                  { l: "Actual Customer", req: true  },
                  { l: "Operator",        req: true  },
                  { l: "Team",            req: true  },
                  { l: "Sales Man",       req: false },
                ].map((f) => (
                  <div key={f.l} className="li">
                    <span className={`li__label${f.req ? " is-required" : ""}`}>{f.l}</span>
                    <div className="li__input"><input placeholder={f.l} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} /></div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {tab === "freight" && <FreightTab />}

      <div className="footbar">
        <span className="footbar__shortcut"><kbd className="kbd">⌘S</kbd> Save</span>
        <span style={{ marginLeft: "auto" }}>Non B/L — Work Division: {workDiv}</span>
      </div>
    </>
  );
}
