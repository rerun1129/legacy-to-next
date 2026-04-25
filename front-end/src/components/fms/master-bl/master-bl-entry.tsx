"use client";

import { useState } from "react";
import Link from "next/link";
import { Save, Copy, Trash2, Layers, Send, RefreshCw } from "lucide-react";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { MasterMainTab } from "./tabs/main-tab";
import { MasterEdiTab }  from "./tabs/edi-tab";
import { OtherTab }      from "@/components/fms/house-bl/tabs/other-tab";
import { FreightTab }    from "@/components/fms/house-bl/tabs/freight-tab";

interface Props { variant: MasterVariantConfig }

const TOOLBAR_SEA = ["Master Ref", "MBL No", "Line Bkg. No", "Load Type", "Service Term", "B/L Type", "Shipment Type", "Status"] as const;
const TOOLBAR_AIR = ["Master Ref", "MAWB No", "Shipment Type", "Status", "", "", "", ""] as const;

export function MasterBLEntry({ variant }: Props) {
  const [tab, setTab] = useState("main");
  const isSea = variant.mode === "SEA";
  const toolbarFields = isSea ? TOOLBAR_SEA : TOOLBAR_AIR.filter(Boolean);

  const tabs = [
    { key: "main",    label: "Main"    },
    { key: "edi",     label: "EDI"     },
    { key: "other",   label: "Other"   },
    { key: "freight", label: "Freight" },
  ];

  const isExp = variant.direction === "EXP";
  const bottomActionsLeft  = variant.bottomActions.filter(a => ["Profit/Loss", "House B/L Load"].includes(a));
  const bottomActionsRight = variant.bottomActions.filter(a => !["Profit/Loss", "House B/L Load"].includes(a));

  return (
    <>
      {/* Page header — NOTE: No Print button per PRD §S-04 */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Layers size={14} /></div>
          Master B/L Entry
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
          <button className="btn btn--sm btn--danger"><Trash2 size={12} />Delete</button>
          <button className="btn btn--sm"><Copy size={12} />Copy</button>
          <button className="btn btn--sm">
            <RefreshCw size={12} />{isSea ? "Change B/L No" : "Change AWB No"}
          </button>
          <button className="btn btn--sm btn--info"><Send size={12} />EDI</button>
          <button className="btn btn--sm btn--primary">
            <Save size={12} />Save<span className="btn__kbd">⌘S</span>
          </button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="toolbar" style={{ gridTemplateColumns: `repeat(${isSea ? 8 : 4}, 1fr)` }}>
        {toolbarFields.map((f) => (
          <div key={f} className={`field${["MBL No","MAWB No","Master Ref"].includes(f) ? " is-required" : ""}`}>
            <div className={`field__label${["MBL No","MAWB No","Master Ref"].includes(f) ? " is-required" : ""}`}>{f}</div>
            <div className="field__input">
              <input defaultValue={
                f === "MBL No" ? "COSCO2404195" :
                f === "MAWB No" ? "180-12345678" :
                f === "Master Ref" ? "MR-2026-04195" :
                f === "Load Type" ? "FCL" :
                f === "Service Term" ? "CY/CY" :
                f === "B/L Type" ? "OBL" :
                f === "Line Bkg. No" ? "BKG-COSCO-0412" :
                f === "Shipment Type" ? "FCL" :
                ""
              } placeholder={f || ""} />
            </div>
          </div>
        ))}
      </div>

      {/* Tabbar */}
      <div className="tabbar">
        {tabs.map((t) => (
          <button key={t.key} className={`tabbar__tab${tab === t.key ? " is-active" : ""}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
        <div className="tabbar__spacer" />
        <div className="tabbar__meta">
          <span>Last saved: 10 min ago</span>
          <span style={{ color: "var(--divider)" }}>|</span>
          <span>담당: 김영선</span>
        </div>
      </div>

      {/* Tab content */}
      {tab === "main"    && <MasterMainTab variant={variant} />}
      {tab === "edi"     && <MasterEdiTab variant={variant} />}
      {tab === "other"   && <OtherTab />}
      {tab === "freight" && <FreightTab />}

      {/* Bottom action strip — Master B/L specific (§S-04) */}
      <div className="footbar" style={{ justifyContent: "flex-start", gap: 6 }}>
        {bottomActionsLeft.map((action) => (
          <button key={action} className="btn btn--sm btn--warn">{action}</button>
        ))}
        <div style={{ flex: 1 }} />
        {bottomActionsRight.map((action) => {
          const isDocOutput  = ["MAWB", "Shipping Request", "Shipping Advice"].includes(action);
          const isEdiSend    = ["M/F Send", "AFR Send"].includes(action);
          const cls = isDocOutput
            ? "btn btn--sm btn--success"
            : isEdiSend
            ? "btn btn--sm btn--info"
            : "btn btn--sm";
          return (
            <button key={action} className={cls}>{action}</button>
          );
        })}
      </div>
    </>
  );
}
