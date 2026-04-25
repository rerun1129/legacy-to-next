"use client";

import { useState } from "react";
import { Save, Trash2, Truck } from "lucide-react";
import { FreightTab } from "@/components/fms/house-bl/tabs/freight-tab";

export function TruckBLEntry() {
  const [tab, setTab] = useState("main");

  return (
    <>
      {/* Page header — NO Print button per PRD §S-06 */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Truck size={14} /></div>
          Truck B/L Entry
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
          <button className="btn btn--sm btn--danger"><Trash2 size={12} />Delete</button>
          <button className="btn btn--sm btn--primary">
            <Save size={12} />Save<span className="btn__kbd">⌘S</span>
          </button>
        </div>
      </div>

      {/* Toolbar: Document Key fields */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(5, 1fr)" }}>
        {[
          { l: "Truck B/L No",    v: "",           req: true  },
          { l: "Settle",          v: "PREPAID",    req: true  },
          { l: "Incoterms",       v: "DAP",        req: false },
          { l: "Freight Term",    v: "Prepaid",    req: false },
          { l: "Status",          v: "DRAFT",      req: false },
        ].map((f) => (
          <div key={f.l} className={`field${f.req ? " is-required" : ""}`}>
            <div className={`field__label${f.req ? " is-required" : ""}`}>{f.l}</div>
            <div className="field__input">
              <input
                defaultValue={f.v}
                placeholder={f.l === "Truck B/L No" ? "Auto on save" : f.v || f.l}
              />
            </div>
          </div>
        ))}
      </div>

      {/* Tabbar — 2 tabs only */}
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
        <div style={{ flex: 1, overflow: "auto", padding: "12px 16px", display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gridTemplateRows: "auto auto auto", gap: 10, alignContent: "start" }}>

          {/* PARTY: 4 slots */}
          <div className="panel" style={{ gridColumn: "1 / 2", gridRow: "1 / 3" }}>
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Party</span></div>
            <div className="panel__body" style={{ overflow: "auto" }}>
              {[
                { role: "SHIPPER",     btn: null            },
                { role: "CONSIGNEE",   btn: "To Order"      },
                { role: "NOTIFY",      btn: "Same as Cne."  },
                { role: "DOC PARTNER", btn: null            },
              ].map((p) => (
                <div key={p.role} className="party-block">
                  <div className="party-block__head">
                    <span style={{ fontSize: 11, fontWeight: 600, color: "var(--ink)", minWidth: 90 }}>{p.role}</span>
                    <div style={{ display: "grid", gridTemplateColumns: "100px 1fr", gap: 6, flex: "1 1 auto", alignItems: "center" }}>
                      <input placeholder="Code" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 11, color: "var(--ink)", outline: "none", fontFamily: "var(--font-mono)" }} />
                      <input placeholder="Company Name" style={{ width: "100%", borderBottom: "1px solid var(--border)", background: "transparent", padding: "4px 2px", fontSize: 11, color: "var(--ink)", outline: "none" }} />
                    </div>
                    <div className="party-block__head-actions">
                      {p.btn && <button className="party-block__head-btn">{p.btn}</button>}
                      <button className="party-block__head-btn">Clear</button>
                    </div>
                  </div>
                  <textarea className="textarea" style={{ minHeight: 48, fontSize: 11 }} placeholder="Address" />
                </div>
              ))}
            </div>
          </div>

          {/* SCHEDULE — simplified (no Liner, no POR/Delivery/Final) */}
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
            <div className="panel__body">
              <div className="sched-list">
                <div className="li"><span className="li__label" style={{ color: "var(--ink-4)" }}>Vessel</span><div className="li__input"><input readOnly value="TRUCK" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--bg-sunken)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink-3)", fontFamily: "var(--font-mono)", outline: "none" }} /></div></div>
                <div className="sched-pair">
                  {[{ l: "ETD *", v: "2026-04-24" }, { l: "ETA *", v: "2026-04-25" }].map((f) => (
                    <div key={f.l} className="li">
                      <span className="li__label is-required">{f.l}</span>
                      <div className="li__input"><input type="date" defaultValue={f.v} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} /></div>
                    </div>
                  ))}
                </div>
                {[{ l: "POL *", c: "KRBSAN", n: "Busan" }, { l: "POD *", c: "KRSEL", n: "Seoul" }].map((f) => (
                  <div key={f.l} className="lcn" style={{ marginBottom: 4 }}>
                    <span className="lcn__label">{f.l}</span>
                    <div className="lcn__code" style={{ position: "relative" }}><input defaultValue={f.c} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)", outline: "none" }} /></div>
                    <input className="lcn__name" defaultValue={f.n} placeholder="Location" />
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* CARGO — Trucking-specific (has Charge W/T) */}
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Cargo</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {[
                  { l: "Package",    v: "1300" },
                  { l: "Unit",       v: "CTN" },
                  { l: "G/W",        v: "30,600 KGS" },
                  { l: "CBM",        v: "87.5" },
                  { l: "Charge W/T", v: "30,600", tip: "Trucking 전용" },
                ].map((f) => (
                  <div key={f.l} className="li">
                    <span className="li__label">{f.l}{(f as { tip?: string }).tip ? " *" : ""}</span>
                    <div className="li__input">
                      <input defaultValue={f.v} style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* DOCUMENT: Pick-up, Trucker — Truck-specific */}
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Document</span></div>
            <div className="panel__body">
              <div className="sched-list">
                <div className="li"><span className="li__label">Pick-up Date</span><div className="li__input"><input type="date" defaultValue="2026-04-23" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} /></div></div>
                <div className="lcn" style={{ marginBottom: 4 }}>
                  <span className="lcn__label">Trucker</span>
                  <div className="lcn__code" style={{ position: "relative" }}><input placeholder="Code" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)", outline: "none" }} /></div>
                  <input className="lcn__name" placeholder="Trucker Name" />
                </div>
                <div className="li"><span className="li__label">Trucker PIC</span><div className="li__input"><input placeholder="담당자 성명" style={{ width: "100%", height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} /></div></div>
              </div>
            </div>
          </div>

          {/* PERFORMANCE — no Sales Class */}
          <div className="panel">
            <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Performance</span></div>
            <div className="panel__body">
              <div className="sched-list">
                {[
                  { l: "Actual Customer", req: true  },
                  { l: "Customer PIC",    req: false },
                  { l: "Settle Partner",  req: false },
                  { l: "Sales Man",       req: true  },
                  { l: "Operator",        req: true  },
                  { l: "Team",            req: true  },
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
        <span style={{ marginLeft: "auto" }}>Truck B/L — 내륙운송 전용</span>
      </div>
    </>
  );
}
