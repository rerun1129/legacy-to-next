"use client";

import { useMemo, useRef, useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { TextBox, NumberBox } from "@/components/shared/inputs";

type ItemRow = NonNullable<HouseBlFormValues["itemHs"]>[number];

const EMPTY_ITEM_HS_ROW: Omit<ItemRow, "id"> = {
  hs: "", desc: "", qty: "", unit: "", value: "", cur: "",
};

export function ItemHsPanel() {
  const { control, register } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "itemHs", keyName: "fieldId" });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);
  const columns = useMemo<GridColumn<ItemRow>[]>(() => [
    { key: "_no",   label: "#",           width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
    { key: "hs",    label: "HS Code",     width: 100, render: (_, __, i) => <TextBox   variant="cell" {...register(`itemHs.${i}.hs`)}    style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "desc",  label: "Description", width: 200, render: (_, __, i) => <TextBox   variant="cell" {...register(`itemHs.${i}.desc`)} /> },
    { key: "qty",   label: "Qty",         width: 70,  className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`itemHs.${i}.qty`}   valueAsNumber={false} /> },
    { key: "unit",  label: "Unit",        width: 60,  render: (_, __, i) => <TextBox   variant="cell" {...register(`itemHs.${i}.unit`)} /> },
    { key: "value", label: "Value",       width: 100, className: "is-num", render: (_, __, i) => <NumberBox variant="cell" name={`itemHs.${i}.value`} valueAsNumber={false} /> },
    { key: "cur",   label: "Currency",    width: 80,  render: (_, __, i) => <TextBox   variant="cell" {...register(`itemHs.${i}.cur`)}   style={{ fontFamily: "var(--font-mono)" }} /> },
  ], [register]);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => String((f as unknown as { fieldId: string }).fieldId) === selectedKey)
    : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    append({ ...EMPTY_ITEM_HS_ROW, id: 0 });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const focused = focusedRowKeyRef.current;
    let targetIdx = -1;
    if (focused !== null) {
      targetIdx = fields.findIndex(f => String((f as unknown as { fieldId: string }).fieldId) === focused);
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
          <button type="button" className="btn btn--sm btn--icon btn--success" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm btn--icon btn--danger" onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={columns}
        data={fields as unknown as ItemRow[]}
        rowKey={(row) => String((row as unknown as { fieldId: string }).fieldId)}
        onRowClick={(row) => setSelectedKey(String((row as unknown as { fieldId: string }).fieldId))}
        rowClassName={(row) => String((row as unknown as { fieldId: string }).fieldId) === selectedKey ? "is-selected" : undefined}
        onClearRow={() => setSelectedKey(null)}
        style={{ flex: 1, minHeight: 0 }}
      />
    </div>
  );
}
