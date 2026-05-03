"use client";

import { useMemo, useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell, TextCell } from "@/components/shared/grid-cell-inputs";

type ContainerRow = NonNullable<HouseBlFormValues["containers"]>[number];

const EMPTY_CONTAINER_ROW: ContainerRow = {
  containerNo: "",
  containerType: "",
  sealNo1: "",
  pkgQty: "",
  pkgUnit: "",
  grossWeightKg: "",
  cbm: "",
  vgmKg: "",
};

export function ContainerGridPanel() {
  const { control, register } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "containers" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const columns = useMemo<GridColumn<ContainerRow>[]>(() => [
    { key: "_no",          label: "#",            width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
    { key: "containerNo",  label: "Container No", width: 160, render: (_, __, i) => <TextCell {...register(`containers.${i}.containerNo`)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
    { key: "containerType", label: "Type",        width: 70,  render: (_, __, i) => <TextCell {...register(`containers.${i}.containerType`)} /> },
    { key: "sealNo1",      label: "Seal No",      width: 110, render: (_, __, i) => <TextCell {...register(`containers.${i}.sealNo1`)} style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "pkgQty",       label: "Pkg",          width: 70,  className: "is-num", render: (_, __, i) => <NumericCell {...register(`containers.${i}.pkgQty`)} /> },
    { key: "pkgUnit",      label: "Unit",         width: 60,  render: (_, __, i) => <TextCell {...register(`containers.${i}.pkgUnit`)} /> },
    { key: "grossWeightKg", label: "G/W",         width: 90,  className: "is-num", render: (_, __, i) => <NumericCell {...register(`containers.${i}.grossWeightKg`)} /> },
    { key: "cbm",          label: "CBM",          width: 80,  className: "is-num", render: (_, __, i) => <NumericCell {...register(`containers.${i}.cbm`)} /> },
    { key: "vgmKg",        label: "VGM",          width: 90,  className: "is-num", render: (_, __, i) => <NumericCell {...register(`containers.${i}.vgmKg`)} /> },
  ], [register]);

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
        columns={columns}
        data={fields as unknown as ContainerRow[]}
        rowKey={(_, i) => fields[i].id}
        onRowClick={(_, i) => setSelectedKey(fields[i].id)}
        rowClassName={(_, i) => fields[i]?.id === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
