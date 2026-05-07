"use client";

import { useState, useMemo }                     from "react";
import { useFormContext, useFieldArray }          from "react-hook-form";
import { Plus, Minus }                           from "lucide-react";
import { GridList, type GridColumn }             from "@/components/shared/grid-list";
import { DropBox }                               from "@/components/shared/inputs";
import { useEnumOptions }                        from "@/application/enums/use-enum";
import type { NonBlFormValues }                  from "../../non-bl-schema";
import { EMPTY_DIM_ROW }                         from "../../non-bl-schema";

interface DimRow { id: number; length: string; width: string; height: string; qty: string; cbm: string; volWt: string; }

export function NonBLDimensionPanel() {
  const { control, register } = useFormContext<NonBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "dimensions" });
  const { options: volumeDivisorOptions } = useEnumOptions("VolumeDivisor");

  const cols = useMemo<GridColumn<DimRow>[]>(() => [
    { key: "_no",    label: "#",           width: 50, className: "row-num", render: (_, __, i) => i + 1 },
    { key: "length", label: "Length",     width: 80, className: "is-num", render: (_, __, i) => <input className="grid__cell-input is-num" {...register(`dimensions.${i}.length`)} /> },
    { key: "width",  label: "Width",      width: 80, className: "is-num", render: (_, __, i) => <input className="grid__cell-input is-num" {...register(`dimensions.${i}.width`)} /> },
    { key: "height", label: "Height",     width: 80, className: "is-num", render: (_, __, i) => <input className="grid__cell-input is-num" {...register(`dimensions.${i}.height`)} /> },
    { key: "qty",    label: "Qty",        width: 80, className: "is-num", render: (_, __, i) => <input className="grid__cell-input is-num" {...register(`dimensions.${i}.qty`)} /> },
    { key: "cbm",    label: "CBM",        width: 80, className: "is-num", render: (_, __, i) => <input className="grid__cell-input is-num" {...register(`dimensions.${i}.cbm`)} /> },
    { key: "volWt",  label: "Volume Wt.", width: 80, className: "is-num", render: (_, __, i) => <input className="grid__cell-input is-num" {...register(`dimensions.${i}.volWt`)} /> },
  ], [register]);
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
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Dimension</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <DropBox
            options={volumeDivisorOptions}
            {...register("dimensionDivisor")}
          />
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={cols}
        data={fields as unknown as DimRow[]}
        rowKey={(r) => r.id}
        onRowClick={(row) => setSelectedKey(String(row.id))}
        rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
