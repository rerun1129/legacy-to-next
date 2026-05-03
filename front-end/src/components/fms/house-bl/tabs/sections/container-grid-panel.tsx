"use client";

import { useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

interface ContainerRow {
  cno: string; type: string; seal: string; pkg: string; pkgT: string;
  gw: string; cbm: string; vgm: string;
}

const CONTAINER_COLS: GridColumn<ContainerRow>[] = [
  { key: "_no",  label: "#",            width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
  { key: "cno",  label: "Container No", width: 160, render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
  { key: "type", label: "Type",         width: 70,  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "seal", label: "Seal No",      width: 110, render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "pkg",  label: "Pkg",          width: 70,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "pkgT", label: "Unit",         width: 60,  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "gw",   label: "G/W",          width: 90,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "cbm",  label: "CBM",          width: 80,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "vgm",  label: "VGM",          width: 90,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
];

const EMPTY_CONTAINER_ROW = {
  cno: "", type: "", seal: "", pkg: "", pkgT: "", gw: "", cbm: "", vgm: "",
};

export function ContainerGridPanel() {
  const { control } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "containers" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function handleAdd() {
    append({ ...EMPTY_CONTAINER_ROW });
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
        <span className="panel__title">Container</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={CONTAINER_COLS}
        data={fields as unknown as ContainerRow[]}
        rowKey={(_, i) => fields[i].id}
        onRowClick={(_, i) => setSelectedKey(fields[i].id)}
        rowClassName={(_, i) => fields[i]?.id === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
