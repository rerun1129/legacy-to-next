"use client";

import { useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

interface DimRow {
  length: string; width: string; height: string;
  qty: string; cbm: string; volWt: string;
}

const COLS: GridColumn<DimRow>[] = [
  { key: "_no",    label: "#",          className: "row-num", render: (_, __, i) => i + 1 },
  { key: "length", label: "Length",     className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "width",  label: "Width",      className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "height", label: "Height",     className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "qty",    label: "Qty",        className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "cbm",    label: "CBM",        className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "volWt",  label: "Volume Wt.", className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
];

const EMPTY_DIM_ROW = {
  length: "", width: "", height: "", qty: "", cbm: "", volWt: "",
};

export function DimensionPanel() {
  const { control } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "dims" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function handleAdd() {
    append({ ...EMPTY_DIM_ROW });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (selectedKey === null || selectedIdx === -1) return;
    if (window.confirm("삭제하시겠습니까?")) {
      remove(selectedIdx);
      setSelectedKey(null);
    }
  }

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Dimension</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm" onClick={handleRemove} disabled={selectedKey === null}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={COLS}
        data={fields as unknown as DimRow[]}
        rowKey={(_, i) => fields[i].id}
        onRowClick={(_, i) => setSelectedKey(fields[i].id === selectedKey ? null : fields[i].id)}
        rowClassName={(_, i) => fields[i]?.id === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
