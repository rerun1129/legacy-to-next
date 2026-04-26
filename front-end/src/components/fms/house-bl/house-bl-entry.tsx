"use client";

import { useState } from "react";
import { Save, Printer, Copy, Trash2, FileText, Send, Download } from "lucide-react";
import type { BLVariantConfig } from "@/lib/bl-variants";
import { getPageTitle } from "@/lib/bl-variants";
import { MainTabSea }  from "./tabs/main-sea";
import { MainTabAir }  from "./tabs/main-air";
import { EdiTab }      from "./tabs/edi-tab";
import { OtherTab }    from "./tabs/other-tab";
import { FreightTab }  from "./tabs/freight-tab";

interface Props { variant: BLVariantConfig }

const TOOLBAR_FIELDS_SEA = [
  "Shipment Type", "Settle", "HBL No", "MBL No", "Load Type", "Service Term", "B/L Type", "Master Ref",
] as const;
const TOOLBAR_FIELDS_AIR = [
  "Shipment Type", "Settle", "HAWB No", "MAWB No", "Rate Class", "Service Term", "B/L Type", "Master Ref",
] as const;

const DEFAULTS_SEA: Record<string, string> = {
  "Shipment Type": "FCL", "Settle": "PREPAID", "HBL No": "HBLKR24041956",
  "MBL No": "COSCO2404195", "Load Type": "CY/CY", "Service Term": "FCL",
  "B/L Type": "OBL", "Master Ref": "",
};
const DEFAULTS_AIR: Record<string, string> = {
  "Shipment Type": "GCR", "Settle": "PREPAID", "HAWB No": "HAWBKR24041001",
  "MAWB No": "180-12345678", "Rate Class": "GCR", "Service Term": "D2D",
  "B/L Type": "", "Master Ref": "",
};

function getToolbarFields(variant: BLVariantConfig) {
  return variant.mode === "SEA" ? TOOLBAR_FIELDS_SEA : TOOLBAR_FIELDS_AIR;
}

function getToolbarDefaults(variant: BLVariantConfig) {
  return variant.mode === "SEA" ? DEFAULTS_SEA : DEFAULTS_AIR;
}

function renderMainTab(variant: BLVariantConfig) {
  return variant.mode === "SEA"
    ? <MainTabSea variant={variant} />
    : <MainTabAir variant={variant} />;
}

export function HouseBLEntry({ variant }: Props) {
  const [tab, setTab] = useState("main");

  const toolbarFields = getToolbarFields(variant);
  const defaults = getToolbarDefaults(variant);

  const tabs = [
    { key: "main",    label: "Main"    },
    { key: "edi",     label: "EDI"     },
    { key: "other",   label: "Other"   },
    { key: "freight", label: "Freight" },
  ];

  return (
    <>
      {/* Page header */}
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><FileText size={14} /></div>
          {getPageTitle(variant, 'House', 'Entry')}
        </div>
        <div className="page-head__meta">
          <span className="badge badge--draft">DRAFT</span>
        </div>
        <div className="page-head__actions">
          <button className="btn btn--sm btn--danger"><Trash2 size={12} />Delete</button>
          <button className="btn btn--sm"><Copy size={12} />Copy</button>
          <button className="btn btn--sm"><Download size={12} />Export</button>
          {variant.printDocs.length > 0 && (
            <button className="btn btn--sm btn--success"><Printer size={12} />Print</button>
          )}
          <button className="btn btn--sm btn--info"><Send size={12} />EDI</button>
          <button className="btn btn--sm btn--primary">
            <Save size={12} />Save
          </button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="toolbar">
        {toolbarFields.map((f) => (
          <div key={f} className={`field${["HBL No","HAWB No","Shipment Type","Settle"].includes(f) ? " is-required" : ""}`}>
            <div className={`field__label${["HBL No","HAWB No","Shipment Type","Settle"].includes(f) ? " is-required" : ""}`}>{f}</div>
            <div className="field__input">
              <input defaultValue={defaults[f] ?? ""} placeholder={f} />
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
        </div>
      </div>

      {/* Tab content */}
      {tab === "main"    && renderMainTab(variant)}
      {tab === "edi"     && <EdiTab variant={variant} />}
      {tab === "other"   && <OtherTab />}
      {tab === "freight" && <FreightTab />}
    </>
  );
}
