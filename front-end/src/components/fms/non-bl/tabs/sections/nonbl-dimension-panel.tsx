"use client";

import { Plus, Minus } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface DimRow { id: number; length: string; width: string; height: string; qty: string; cbm: string; volWt: string; }

const COLS: GridColumn<DimRow>[] = [
  { key: "_no",   label: "#",          className: "row-num", render: (_, __, i) => i + 1 },
  { key: "length", label: "Length",    className: "is-num" },
  { key: "width",  label: "Width",     className: "is-num" },
  { key: "height", label: "Height",    className: "is-num" },
  { key: "qty",    label: "Qty",       className: "is-num" },
  { key: "cbm",    label: "CBM",       className: "is-num" },
  { key: "volWt",  label: "Volume Wt.", className: "is-num" },
];

export function NonBLDimensionPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Dimension</span>
        <div className="panel__actions">
          <select style={{ fontSize: "var(--fs-sm)", border: "1px solid var(--border)", borderRadius: 4, padding: "2px 6px", background: "var(--surface)" }}>
            <option>CM / 6000</option>
            <option>CM / 5000</option>
            <option>IN / 366</option>
          </select>
          <button className="btn btn--sm btn--ghost" onClick={() => { /* TODO */ }}><Plus size={12} /></button>
          <button className="btn btn--sm btn--ghost" onClick={() => { /* TODO */ }}><Minus size={12} /></button>
        </div>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <GridList columns={COLS} data={[]} rowKey={(r) => r.id} />
      </div>
    </div>
  );
}
