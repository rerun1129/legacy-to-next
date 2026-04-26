"use client";

import { useState } from "react";
import { Save, Trash2, Package, Printer } from "lucide-react";
import { FreightTab } from "@/components/fms/house-bl/tabs/freight-tab";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface ContainerInfoRow {
  cno: string; type: string; pkg: string; gw: string; cbm: string;
}

const containerInfoRows: ContainerInfoRow[] = [
  { cno: "CSNU1234567", type: "20GP", pkg: "500 CTN", gw: "12,400", cbm: "22.5" },
  { cno: "TCKU9876543", type: "40HC", pkg: "800 CTN", gw: "18,200", cbm: "65.0" },
  { cno: "MSKU3456789", type: "40GP", pkg: "650 CTN", gw: "15,800", cbm: "60.2" },
  { cno: "HLXU2345678", type: "20GP", pkg: "420 CTN", gw: "10,500", cbm: "21.0" },
  { cno: "GESU5678901", type: "40HC", pkg: "750 CTN", gw: "19,400", cbm: "67.5" },
  { cno: "TCNU8901234", type: "20GP", pkg: "350 CTN", gw: "8,750",  cbm: "19.8" },
];

const CONTAINER_INFO_COLS: GridColumn<ContainerInfoRow>[] = [
  { key: "_no",  label: "#",             render: (_, __, i) => i + 1 },
  { key: "cno",  label: "Container No." },
  { key: "type", label: "Type" },
  { key: "pkg",  label: "Pkg",  className: "is-num" },
  { key: "gw",   label: "G/W",  className: "is-num" },
  { key: "cbm",  label: "CBM",  className: "is-num" },
];

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
            <Save size={12} />Save
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
      </div>

      {/* Main tab */}
      {tab === "main" && (
        <div style={{ flex: 1, overflow: "auto", padding: "12px 16px", display: "flex", gap: 10, minHeight: 0 }}>

          {/* 좌: Party (전체 높이) */}
          <div style={{ flex: "34 1 0", minWidth: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
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
                    <span style={{ fontSize: 11, fontWeight: 600, color: "var(--ink)", minWidth: 120, flexShrink: 0 }}>
                      {p.role}{p.req && <span style={{ color: "var(--required)", marginLeft: 3 }}>*</span>}
                    </span>
                    <div style={{ display: "grid", gridTemplateColumns: "90px 1fr", gap: 6, flex: "1 1 auto", alignItems: "center" }}>
                      <input placeholder="Code" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 10, color: "var(--ink)", outline: "none", fontFamily: "var(--font-mono)" }} />
                      <input placeholder="Company Name" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 10, color: "var(--ink)", outline: "none" }} />
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

          </div>{/* /Party 좌측 열 */}

          {/* 우: 행1(Schedule+WorkDiv) + 행2(RefNo+Performance) */}
          <div style={{ flex: "66 1 0", minWidth: 0, display: "flex", flexDirection: "column", gap: 10 }}>

            {/* 행1: Schedule | Work Division */}
            <div style={{ display: "flex", gap: 10 }}>
              <div style={{ flex: 1, minWidth: 0, overflow: "hidden" }}>
              <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
            <div className="panel__body">
              <div className="sched-list">
                <div className="sched-pair">
                  {[{ l: "ETD", v: "2026-04-24" }, { l: "ETA", v: "2026-05-08" }].map((f) => (
                    <div key={f.l} className="li">
                      <span className="li__label is-required">{f.l}</span>
                      <div className="li__input"><input type="date" defaultValue={f.v} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div>
                    </div>
                  ))}
                </div>
                {[{ l: "POL", c: "KRBSAN", n: "Busan" }, { l: "POD", c: "CNSHA", n: "Shanghai" }].map((f) => (
                  <div key={f.l} className="lcn" style={{ marginBottom: 4 }}>
                    <span className="lcn__label is-required">{f.l}</span>
                    <div className="lcn__code" style={{ position: "relative" }}><input defaultValue={f.c} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} /></div>
                    <input className="lcn__name" defaultValue={f.n} />
                  </div>
                ))}
              </div>
            </div>
          </div>

              </div>{/* /Schedule 열 */}
              <div style={{ flex: 1, minWidth: 0, overflow: "hidden" }}>
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
                <div style={{ overflow: "auto" }}>
                  <GridList
                    columns={CONTAINER_INFO_COLS}
                    data={containerInfoRows}
                  />
                </div>
              )}
              {workDiv === "Air" && (
                <div>
                  <div className="subhead"><div className="subhead__bar" />Cargo</div>
                  <div className="sched-list">
                    {["Package/Unit", "Gross W/T", "Charge W/T", "Rate Class", "CBM"].map((f) => (
                      <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>
                    ))}
                  </div>
                </div>
              )}
              {workDiv === "Warehouse" && (
                <div className="sched-list">
                  {["창고 코드", "입고일", "출고 예정일", "보관 위치", "면적(m²)"].map((f) => (
                    <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>
                  ))}
                </div>
              )}
              {workDiv === "Trucking" && (
                <div className="sched-list">
                  {["Pick-up Date", "Trucker", "Trucker PIC", "차량번호"].map((f) => (
                    <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>
                  ))}
                </div>
              )}
            </div>
          </div>

              </div>{/* /WorkDiv 열 */}
            </div>{/* /행1 */}

            {/* 행2: Reference Numbers | Performance */}
            <div style={{ display: "flex", gap: 10 }}>
              <div style={{ flex: 1, minWidth: 0, overflow: "hidden" }}>
              <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Reference Numbers</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {["원본 B/L No.", "PO No.", "Invoice No.", "Customer Ref"].map((f) => (
                  <div key={f} className="li"><span className="li__label">{f}</span><div className="li__input"><input placeholder={f} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div></div>
                ))}
              </div>
            </div>
          </div>

              </div>{/* /RefNo 열 */}
              <div style={{ flex: 1, minWidth: 0, overflow: "hidden" }}>
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
                    <div className="li__input"><input placeholder={f.l} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} /></div>
                  </div>
                ))}
              </div>
            </div>
          </div>
              </div>{/* /Performance 열 */}
            </div>{/* /행2 */}
          </div>{/* /우측 열 */}
        </div>
      )}

      {tab === "freight" && <FreightTab />}
    </>
  );
}
