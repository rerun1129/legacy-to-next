"use client";

import { useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

interface ItemRow {
  id: number;
  hs: string; desc: string; qty: string; unit: string; value: string; cur: string;
}

const ITEM_COLS: GridColumn<ItemRow>[] = [
  { key: "_no",   label: "#",           width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
  { key: "hs",    label: "HS Code",     width: 100, render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "desc",  label: "Description", width: 200, render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",   label: "Qty",         width: 70,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "unit",  label: "Unit",        width: 60,  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "value", label: "Value",       width: 100, className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "cur",   label: "Currency",    width: 80,  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
];

const EMPTY_ITEM_HS_ROW = {
  hs: "", desc: "", qty: "", unit: "", value: "", cur: "",
};

export function ItemHsPanel() {
  const { control } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "itemHs" });
  const [selectedKey, setSelectedKey] = useState<number | null>(null);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function handleAdd() {
    const nextId = fields.length > 0 ? Math.max(...fields.map(f => f.id)) + 1 : 1;
    append({ ...EMPTY_ITEM_HS_ROW, id: nextId });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const targetIdx = selectedKey !== null && selectedIdx !== -1 ? selectedIdx : fields.length - 1;
    remove(targetIdx);
    setSelectedKey(null);
  }

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Item / HS Code</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={ITEM_COLS}
        data={fields as unknown as ItemRow[]}
        rowKey={(r) => r.id}
        onRowClick={(row) => setSelectedKey(row.id)}
        rowClassName={(row) => row.id === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
