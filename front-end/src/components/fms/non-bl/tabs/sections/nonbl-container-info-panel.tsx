"use client";

import { useState, useMemo }                     from "react";
import { useFormContext, useFieldArray }          from "react-hook-form";
import { Plus, Minus }                           from "lucide-react";
import { GridList, type GridColumn }             from "@/components/shared/grid-list";
import { TextBox, NumberBox, ComboBox }           from "@/components/shared/inputs";
import { useEnumOptions }                        from "@/application/enums/use-enum";
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

export function NonBLContainerInfoPanel() {
  const { control, register } = useFormContext<NonBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "containers" });
  const { options: rawOptions } = useEnumOptions("ContainerType");
  const contTypeOptions = useMemo(() => rawOptions.map(o => ({ value: o.value, label: o.value })), [rawOptions]);

  const cols = useMemo<GridColumn<ContainerInfoRow>[]>(() => [
    { key: "_no",      width: 50, label: "#",           className: "row-num", render: (_, __, i) => i + 1 },
    { key: "cno",      width: 80, label: "Container No.", render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.cno`)} /> },
    { key: "contType", width: 80, label: "Cont.Type",   render: (_, __, i) => <ComboBox variant="cell" options={contTypeOptions} {...register(`containers.${i}.contType`)} /> },
    { key: "sealNo1",  width: 80, label: "Seal No. 1",  render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.sealNo1`)} /> },
    { key: "sealNo2",  width: 80, label: "Seal No. 2",  render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.sealNo2`)} /> },
    { key: "sealNo3",  width: 80, label: "Seal No. 3",  render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.sealNo3`)} /> },
    { key: "pkg",      width: 80, label: "Package",     render: (_, __, i) => <NumberBox name={`containers.${i}.pkg`} variant="cell" decimalPlaces={0} /> },
    { key: "pkgUnit",  width: 80, label: "Unit",        render: (_, __, i) => <TextBox variant="cell" {...register(`containers.${i}.pkgUnit`)} /> },
    { key: "grossWt",  width: 80, label: "Gross W/T",   render: (_, __, i) => <NumberBox name={`containers.${i}.grossWt`} variant="cell" decimalPlaces={3} /> },
    { key: "cbm",      width: 80, label: "CBM",         render: (_, __, i) => <NumberBox name={`containers.${i}.cbm`} variant="cell" decimalPlaces={3} /> },
  ], [register, contTypeOptions]);
  const [selectedKey, setSelectedKey] = useState<string | null>(null);

  // id: z.number(), selectedKey: string state → 비교 시 명시 변환 필요 (가이드 §6.9)
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
    <div className="panel panel--col">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Container Information</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm btn--ghost" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={cols}
        data={fields as unknown as ContainerInfoRow[]}
        rowKey={(r) => r.id}
        onRowClick={(row) => setSelectedKey(String(row.id))}
        rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}
        onClearRow={() => setSelectedKey("")}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
