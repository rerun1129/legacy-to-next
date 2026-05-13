"use client";

import { useMemo, useRef, useState } from "react";
import { useFormContext, useFieldArray, Controller } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { TextBox, NumberBox, ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { Button } from "@/components/shared/button";

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
  const { options: rawContainerTypeOptions } = useEnumOptions("ContainerType");
  const containerTypeOptions = useMemo(
    () => rawContainerTypeOptions.map(o => ({ value: o.value, label: o.label })),
    [rawContainerTypeOptions]
  );
  const { fields, append, remove } = useFieldArray({ control, name: "containers" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);
  const columns = useMemo<GridColumn<ContainerRow>[]>(() => [
    { key: "_no",          label: "#",            width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
    { key: "containerNo",  label: "Container No", width: 160, render: (_, __, i) => <TextBox   variant="cell" {...register(`containers.${i}.containerNo`)}  style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
    { key: "containerType", label: "Type",        width: 100, render: (_, __, i) => (
      <Controller
        name={`containers.${i}.containerType`}
        control={control}
        render={({ field }) => (
          <ComboBox variant="cell" options={containerTypeOptions} value={field.value ?? ""} onChange={field.onChange} />
        )}
      />
    ) },
    { key: "sealNo1",      label: "Seal No",      width: 110, render: (_, __, i) => <TextBox   variant="cell" {...register(`containers.${i}.sealNo1`)}       style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "pkgQty",       label: "Pkg",          width: 70,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`containers.${i}.pkgQty`}        decimalPlaces={0} valueAsNumber={false} /> },
    { key: "pkgUnit",      label: "Unit",         width: 60,  render: (_, __, i) => <TextBox   variant="cell" {...register(`containers.${i}.pkgUnit`)} /> },
    { key: "grossWeightKg", label: "G/W",         width: 90,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`containers.${i}.grossWeightKg`} decimalPlaces={3} valueAsNumber={false} /> },
    { key: "cbm",          label: "CBM",          width: 80,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`containers.${i}.cbm`}           decimalPlaces={3} valueAsNumber={false} /> },
    { key: "vgmKg",        label: "VGM",          width: 90,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`containers.${i}.vgmKg`}         decimalPlaces={3} valueAsNumber={false} /> },
  ], [register, control, containerTypeOptions]);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    append({ ...EMPTY_CONTAINER_ROW });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const focused = focusedRowKeyRef.current;
    let targetIdx = -1;
    if (focused !== null) {
      targetIdx = fields.findIndex(f => (f as unknown as { id: string }).id === focused);
    }
    if (targetIdx === -1 && selectedKey !== null && selectedIdx !== -1) {
      targetIdx = selectedIdx;
    }
    if (targetIdx === -1) targetIdx = fields.length - 1;
    remove(targetIdx);
    setSelectedKey(null);
    focusedRowKeyRef.current = null;
  }

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Container</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <Button variant="success" size="sm" iconOnly onClick={handleAdd}><Plus size={12} /></Button>
          <Button variant="danger" size="sm" iconOnly onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></Button>
        </div>
      </div>
      <GridList
        columns={columns}
        data={fields as unknown as ContainerRow[]}
        rowKey={(row) => (row as unknown as { id: string }).id}
        onRowClick={(row) => setSelectedKey((row as unknown as { id: string }).id)}
        rowClassName={(row) => (row as unknown as { id: string }).id === selectedKey ? "is-selected" : undefined}
        onClearRow={() => setSelectedKey(null)}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
