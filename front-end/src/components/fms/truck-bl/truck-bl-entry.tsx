"use client";

import { useState } from "react";
import { Save, Trash2, Truck } from "lucide-react";
import { FreightTab } from "@/components/fms/house-bl/tabs/freight-tab";
import { MainTruck }  from "./tabs/main-truck";

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
            <Save size={12} />Save
          </button>
        </div>
      </div>

      {/* Toolbar: Document Key fields */}
      <div className="toolbar" style={{ gridTemplateColumns: "repeat(5, 1fr)" }}>
        {[
          { l: "Truck B/L No",    v: "",  req: true  },
          { l: "Settle",          v: "",  req: true  },
          { l: "Incoterms",       v: "",  req: false },
          { l: "Freight Term",    v: "",  req: false },
          { l: "Status",          v: "",  req: false },
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

      {tab === "main" && <MainTruck />}

      {tab === "freight" && <FreightTab />}
    </>
  );
}
