"use client";

import { useMemo, useRef, useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { useTranslations } from "next-intl";
import { Plus, Minus } from "lucide-react";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";
import { Button } from "@/components/shared/button";

type DimRow = NonNullable<MasterBlFormValues["dims"]>[number];

const EMPTY_DIM_ROW: DimRow = {
  lengthCm: undefined,
  widthCm: undefined,
  heightCm: undefined,
  quantity: undefined,
  cbm: undefined,
  volumeWeightKg: undefined,
};

export function MasterAirDimensionPanel() {
  const { control, register } = useFormContext<MasterBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "dims" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);
  const tp = useTranslations("fms.masterBl.entry.panels");
  const tf = useTranslations("fms.masterBl.entry.fields");

  const columns = useMemo<GridColumn<DimRow>[]>(() => [
    { key: "_no",            label: "#",                className: "row-num", width: 50,  render: (_, __, i) => i + 1 },
    { key: "lengthCm",       label: tf("length"),       className: "is-num",  width: 80,  render: (_, __, i) => <NumericCell {...register(`dims.${i}.lengthCm`)} /> },
    { key: "widthCm",        label: tf("width"),        className: "is-num",  width: 80,  render: (_, __, i) => <NumericCell {...register(`dims.${i}.widthCm`)} /> },
    { key: "heightCm",       label: tf("height"),       className: "is-num",  width: 80,  render: (_, __, i) => <NumericCell {...register(`dims.${i}.heightCm`)} /> },
    { key: "quantity",       label: tf("qty"),          className: "is-num",  width: 80,  render: (_, __, i) => <NumericCell {...register(`dims.${i}.quantity`)} /> },
    { key: "cbm",            label: tf("cbm"),          className: "is-num",  width: 80,  render: (_, __, i) => <NumericCell {...register(`dims.${i}.cbm`)} /> },
    { key: "volumeWeightKg", label: tf("volumeWtCol"),  className: "is-num",  width: 80,  render: (_, __, i) => <NumericCell {...register(`dims.${i}.volumeWeightKg`)} /> },
  ], [register, tf]);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    append({ ...EMPTY_DIM_ROW });
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
        <span className="panel__title">{tp("dimension")}</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <Button variant="success" size="sm" iconOnly onClick={handleAdd}><Plus size={12} /></Button>
          <Button variant="danger" size="sm" iconOnly onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></Button>
        </div>
      </div>
      <GridList
        columns={columns}
        data={fields as unknown as DimRow[]}
        rowKey={(row) => (row as unknown as { id: string }).id}
        onRowClick={(row) => setSelectedKey((row as unknown as { id: string }).id)}
        rowClassName={(row) => (row as unknown as { id: string }).id === selectedKey ? "is-selected" : undefined}
        onClearRow={() => setSelectedKey(null)}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
