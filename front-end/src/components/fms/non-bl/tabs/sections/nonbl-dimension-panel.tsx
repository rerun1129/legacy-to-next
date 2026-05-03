"use client";

import { useState }                              from "react";
import { useFormContext, useFieldArray }          from "react-hook-form";
import { Plus, Minus }                           from "lucide-react";
import { GridList, type GridColumn }             from "@/components/shared/grid-list";
import type { NonBlFormValues }                  from "../../non-bl-schema";
import { EMPTY_DIM_ROW }                         from "../../non-bl-schema";

interface DimRow { id: number; length: string; width: string; height: string; qty: string; cbm: string; volWt: string; }

const COLS: GridColumn<DimRow>[] = [
  { key: "_no",   label: "#",           width: 50, className: "row-num", render: (_, __, i) => i + 1 },
  { key: "length", label: "Length",     width: 80, className: "is-num" },
  { key: "width",  label: "Width",      width: 80, className: "is-num" },
  { key: "height", label: "Height",     width: 80, className: "is-num" },
  { key: "qty",    label: "Qty",        width: 80, className: "is-num" },
  { key: "cbm",    label: "CBM",        width: 80, className: "is-num" },
  { key: "volWt",  label: "Volume Wt.", width: 80, className: "is-num" },
];

export function NonBLDimensionPanel() {
  const { control } = useFormContext<NonBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "dimensions" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);

  const selectedIdx = fields.findIndex(f => f.id === Number(selectedKey));

  function handleAdd() {
    const nextId = Math.max(0, ...fields.map(f => f.id)) + 1;
    append({ ...EMPTY_DIM_ROW, id: nextId });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const targetIdx = selectedKey !== null && selectedIdx !== -1 ? selectedIdx : fields.length - 1;
    remove(targetIdx);
    setSelectedKey(null);
  }

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Dimension</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <select style={{ fontSize: "var(--fs-sm)", border: "1px solid var(--border)", borderRadius: 4, padding: "2px 6px", background: "var(--surface)" }}>
            <option>CM / 6000</option>
            <option>CM / 5000</option>
            <option>IN / 366</option>
          </select>
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <GridList
          columns={COLS}
          data={fields as unknown as DimRow[]}
          rowKey={(r) => r.id}
          onRowClick={(row) => setSelectedKey(String(row.id))}
          rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}
        />
      </div>
    </div>
  );
}
