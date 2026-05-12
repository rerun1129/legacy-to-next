"use client";

import { useMemo, useRef, useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell, TextCell } from "@/components/shared/grid-cell-inputs";

type ItemRow = NonNullable<HouseBlFormValues["itemHs"]>[number];

const EMPTY_ITEM_HS_ROW: Omit<ItemRow, "id"> = {
  hs: "", desc: "", qty: "", unit: "", value: "", cur: "",
};

export function ItemHsPanel() {
  const { control, register } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "itemHs", keyName: "fieldId" });
  const [selectedKey, setSelectedKey] = useState<number | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);
  const columns = useMemo<GridColumn<ItemRow>[]>(() => [
    { key: "_no",   label: "#",           width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
    { key: "hs",    label: "HS Code",     width: 100, render: (_, __, i) => <TextCell {...register(`itemHs.${i}.hs`)} style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "desc",  label: "Description", width: 200, render: (_, __, i) => <TextCell {...register(`itemHs.${i}.desc`)} /> },
    { key: "qty",   label: "Qty",         width: 70,  className: "is-num", render: (_, __, i) => <NumericCell {...register(`itemHs.${i}.qty`)} /> },
    { key: "unit",  label: "Unit",        width: 60,  render: (_, __, i) => <TextCell {...register(`itemHs.${i}.unit`)} /> },
    { key: "value", label: "Value",       width: 100, className: "is-num", render: (_, __, i) => <NumericCell {...register(`itemHs.${i}.value`)} /> },
    { key: "cur",   label: "Currency",    width: 80,  render: (_, __, i) => <TextCell {...register(`itemHs.${i}.cur`)} style={{ fontFamily: "var(--font-mono)" }} /> },
  ], [register]);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    const nextId = fields.length > 0 ? Math.max(...fields.map(f => f.id ?? 0)) + 1 : 1;
    append({ ...EMPTY_ITEM_HS_ROW, id: nextId });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const focused = focusedRowKeyRef.current;
    let targetIdx = -1;
    if (focused !== null) {
      targetIdx = fields.findIndex(f => (f as unknown as { fieldId: string }).fieldId === focused);
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
        <span className="panel__title">Item / HS Code</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm" onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={columns}
        data={fields as unknown as ItemRow[]}
        rowKey={(_, i) => fields[i].fieldId}
        onRowClick={(row) => setSelectedKey(row.id)}
        rowClassName={(row) => row.id === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
