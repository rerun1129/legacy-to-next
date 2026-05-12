"use client";

import { useRef, useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import { getModeLabels } from "@/lib/bl-mode-labels";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";

interface Props { variant?: AnyVariantConfig }

const EMPTY_HBL_ROW = {
  hblNo: "", shipper: "", consignee: "", pkg: "", gw: "", cbm: "", status: "",
};

export function MasterHouseBLGrid({ variant }: Props) {
  const { control } = useFormContext<MasterBlFormValues>();
  // keyName을 "rhfKey"로 분리해 field.id(=스키마 숫자 id)와 RHF 내부 UUID를 구분
  const { fields, append, remove } = useFieldArray({
    control,
    name: "houseBls",
    keyName: "rhfKey",
  });
  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);

  if (!variant) return null;
  const isSea = variant.mode === "SEA";
  const ml    = getModeLabels(variant.mode);

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const tr = activeEl?.closest("tr[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = tr?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    // field.id는 스키마의 숫자 ID; 신규 행에 현재 max+1 할당
    const nextId = fields.length > 0
      ? Math.max(...fields.map(f => f.id)) + 1
      : 1;
    append({ ...EMPTY_HBL_ROW, id: nextId });
    setSelectedIdx(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const focused = focusedRowKeyRef.current;
    let targetIdx = -1;
    if (focused !== null) {
      targetIdx = fields.findIndex(f => (f as unknown as { rhfKey: string }).rhfKey === focused);
    }
    if (targetIdx === -1 && selectedIdx !== null) {
      targetIdx = selectedIdx;
    }
    if (targetIdx === -1) targetIdx = fields.length - 1;
    remove(targetIdx);
    setSelectedIdx(null);
    focusedRowKeyRef.current = null;
  }

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{ml.hblList}</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={handleAdd}>
            <Plus size={12} />{ml.newHbl}
          </button>
          <button
            type="button"
            className="btn btn--sm"
            onMouseDown={captureFocusedRow}
            onClick={handleRemove}
            disabled={fields.length === 0}
          >
            <Minus size={12} />
          </button>
          <button type="button" className="btn btn--sm">House Consol</button>
        </div>
      </div>
      <div style={{ overflow: "auto", flex: 1 }}>
        <table className="grid--list">
          <thead>
            <tr>
              <th className="row-num">#</th>
              <th>{ml.hblNo}</th>
              <th>Shipper</th><th>Consignee</th>
              <th className="is-num">Package</th>
              <th className="is-num">G/W</th><th className="is-num">CBM</th>
              <th>Status</th>
              {isSea && <><th>ETD</th><th>POL</th></>}
            </tr>
          </thead>
          <tbody>
            {fields.length === 0 && (
              <tr>
                <td colSpan={isSea ? 10 : 8} style={{ textAlign: "center", padding: 8, fontSize: 11, color: "var(--ink-3)" }}>
                  No rows.
                </td>
              </tr>
            )}
            {fields.map((field, idx) => (
              <tr
                key={field.rhfKey}
                data-row-key={field.rhfKey}
                className={idx === selectedIdx ? "is-selected" : undefined}
                onClick={() => setSelectedIdx(idx)}
                style={{ cursor: "pointer" }}
              >
                <td className="row-num">{idx + 1}</td>
                <td className="cell-hbl">{field.hblNo}</td>
                <td>{field.shipper}</td>
                <td>{field.consignee}</td>
                <td className="is-num cell-mono">{field.pkg}</td>
                <td className="is-num cell-mono">{field.gw}</td>
                <td className="is-num cell-mono">{field.cbm}</td>
                <td>{field.status}</td>
                {isSea && <><td className="cell-mono"></td><td className="cell-mono"></td></>}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
