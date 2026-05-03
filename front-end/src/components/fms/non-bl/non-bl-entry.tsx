"use client";

import { useState }       from "react";
import { Save, Trash2, Package, Printer } from "lucide-react";
import { FreightTab }     from "@/components/fms/house-bl/tabs/freight-tab";
import { MainNonBL }      from "./tabs/main-non-bl";

type WorkDiv = "Sea" | "Air" | "Warehouse" | "Trucking";

export function NonBLEntry() {
  const [tab,     setTab]     = useState("main");
  const [workDiv, setWorkDiv] = useState<WorkDiv>("Sea");

  return (
    <>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          Non B/L Entry
        </div>
        <div className="page-head__meta"><span className="badge badge--draft">DRAFT</span></div>
        <div className="page-head__actions">
          <button className="btn btn--sm btn--danger"><Trash2 size={12} />Delete</button>
          <button className="btn btn--sm btn--success"><Printer size={12} />Print</button>
          <button className="btn btn--sm btn--primary"><Save size={12} />Save</button>
        </div>
      </div>

      <div className="toolbar" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
        {[
          { l: "Non B/L No",    v: "",      req: true  },
          { l: "Work Division", v: workDiv, req: true  },
          { l: "Status",        v: "",      req: false },
          { l: "Ref. No.",      v: "",      req: false },
        ].map(f => (
          <div key={f.l} className={`field${f.req ? " is-required" : ""}`}>
            <div className={`field__label${f.req ? " is-required" : ""}`}>{f.l}</div>
            <div className="field__input">
              {f.l === "Work Division" ? (
                <select value={workDiv} onChange={e => setWorkDiv(e.target.value as WorkDiv)} style={{ all: "unset", flex: 1, minWidth: 0, fontSize: "var(--fs-base)", color: "var(--ink)", cursor: "pointer" }}>
                  <option>Sea</option><option>Air</option><option>Warehouse</option><option>Trucking</option>
                </select>
              ) : (
                <input defaultValue={f.v} placeholder={f.l === "Non B/L No" ? "Auto on save" : f.v || f.l} />
              )}
            </div>
          </div>
        ))}
      </div>

      <div className="tabbar">
        {[{ key: "main", label: "Main" }, { key: "freight", label: "Freight" }].map(t => (
          <button key={t.key} className={`tabbar__tab${tab === t.key ? " is-active" : ""}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
        <div className="tabbar__spacer" />
      </div>

      {tab === "main"    && <MainNonBL />}
      {tab === "freight" && <FreightTab />}
    </>
  );
}
