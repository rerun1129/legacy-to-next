"use client";

import { useState }                              from "react";
import { useFormContext, useFieldArray }          from "react-hook-form";
import { Plus, Minus }                           from "lucide-react";
import { GridList, type GridColumn }             from "@/components/shared/grid-list";
import type { NonBlFormValues }                  from "../../non-bl-schema";
import { EMPTY_CONTAINER_ROW }                   from "../../non-bl-schema";

interface ContainerInfoRow {
  id: number;
  cno: string;
  contType: string;
  sealNo1: string;
  sealNo2: string;
  sealNo3: string;
  pkg: number;
  pkgUnit: string;
  grossWt: number;
  cbm: number;
}

const COLS: GridColumn<ContainerInfoRow>[] = [
  { key: "_no",      width: 50, label: "#",                 className: "row-num", render: (_, __, i) => i + 1 },
  { key: "cno",      width: 80, label: "Container No." },
  { key: "contType", width: 80, label: "Cont.Type" },
  { key: "sealNo1",  width: 80, label: "Seal No. 1" },
  { key: "sealNo2",  width: 80, label: "Seal No. 2" },
  { key: "sealNo3",  width: 80, label: "Seal No. 3" },
  { key: "pkg",      width: 80, label: "Package",     className: "is-num" },
  { key: "pkgUnit",  width: 80, label: "Unit" },
  { key: "grossWt",  width: 80, label: "Gross W/T",   className: "is-num" },
  { key: "cbm",      width: 80, label: "CBM",         className: "is-num" },
];

export function NonBLContainerInfoPanel() {
  const { control } = useFormContext<NonBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "containers" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);

  const selectedIdx = fields.findIndex(f => f.id === Number(selectedKey));

  function handleAdd() {
    const nextId = Math.max(0, ...fields.map(f => f.id)) + 1;
    append({ ...EMPTY_CONTAINER_ROW, id: nextId });
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
        <span className="panel__title">Container Information</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <GridList
          columns={COLS}
          data={fields as unknown as ContainerInfoRow[]}
          rowKey={(r) => r.id}
          onRowClick={(row) => setSelectedKey(String(row.id))}
          rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}
        />
      </div>
    </div>
  );
}
