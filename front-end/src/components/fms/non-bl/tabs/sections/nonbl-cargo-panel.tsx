"use client";

import { Search } from "lucide-react";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";

const CARGO_ITEMS: FieldItemDef[] = [
  {
    key: "main-item",
    render: () => (
      <div className="li">
        <span className="li__label">Main Item</span>
        <div className="li__input">
          <input placeholder="Main Item" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "hs-code",
    render: () => (
      <div className="li">
        <span className="li__label">HS Code</span>
        <div className="li__input">
          <input placeholder="HS Code" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "package",
    render: () => (
      <div className="li">
        <span className="li__label">Package</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input type="number" placeholder="0" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
          <select style={{ width: 44, height: 22, padding: "0 2px", fontSize: 10, border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)", flexShrink: 0 }}>
            <option>KG</option>
            <option>LBS</option>
          </select>
        </div>
      </div>
    ),
  },
  {
    key: "unit",
    render: () => (
      <div className="li">
        <span className="li__label">Unit</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input placeholder="Code" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={12} style={{ flexShrink: 0, color: "var(--ink-muted)", cursor: "pointer" }} />
        </div>
      </div>
    ),
  },
  {
    key: "gross-wt",
    render: () => (
      <div className="li">
        <span className="li__label">Gross W/T</span>
        <div className="li__input">
          <input type="number" placeholder="0" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "volume-wt",
    render: () => (
      <div className="li">
        <span className="li__label">Volume W/T</span>
        <div className="li__input">
          <input type="number" placeholder="0" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "cbm",
    render: () => (
      <div className="li">
        <span className="li__label">CBM</span>
        <div className="li__input">
          <input type="number" placeholder="0" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "rton",
    render: () => (
      <div className="li">
        <span className="li__label">R/Ton</span>
        <div className="li__input">
          <input type="number" defaultValue={1} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
];

export function NonBLCargoPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Cargo</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="nonbl-cargo-panel" items={CARGO_ITEMS} />
      </div>
    </div>
  );
}
