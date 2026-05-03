"use client";

import { useFieldArray, useFormContext } from "react-hook-form";
import { Plus, X } from "lucide-react";
import type { HouseBlFormValues } from "../house-bl-schema";

// SEA EXP 전용 라이선스 다중행 그리드

const EMPTY_ROW = {
  licenseNo: "", pkgQty: "", pkgUnit: "", grossWeightKg: "",
  combinedPackingMark: "", combinedPackingQty: "", combinedPackingUnit: "",
  partialShipment: false, partialShipmentSeq: "", hsnNo: "",
} as const;

const INPUT_ST: React.CSSProperties = { width: "100%", height: 24, padding: "0 4px", fontSize: 10 };
const NUM_ST:   React.CSSProperties = { ...INPUT_ST, textAlign: "right" };

export function LicensePanel() {
  const { control, register } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "licenses" });

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">License</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={() => append({ ...EMPTY_ROW })}>
            <Plus size={12} />
          </button>
        </div>
      </div>

      <div style={{ flex: 1, overflow: "auto" }}>
        <table className="grid-table" style={{ minWidth: 860 }}>
          <thead>
            <tr>
              <th style={{ width: 32 }}>#</th>
              <th style={{ width: 120 }}>License No</th>
              <th style={{ width: 60 }}>Pkg Qty</th>
              <th style={{ width: 60 }}>Unit</th>
              <th style={{ width: 90 }}>G/W (kg)</th>
              <th style={{ width: 110 }}>Comb. Mark</th>
              <th style={{ width: 60 }}>Comb. Qty</th>
              <th style={{ width: 60 }}>Comb. Unit</th>
              <th style={{ width: 60 }}>HSN No</th>
              <th style={{ width: 32 }} />
            </tr>
          </thead>
          <tbody>
            {fields.length === 0 && (
              <tr>
                <td colSpan={10} style={{ textAlign: "center", padding: 8, fontSize: 11, color: "var(--ink-3)" }}>
                  No license rows. Click + to add.
                </td>
              </tr>
            )}
            {fields.map((field, idx) => (
              <tr key={field.id}>
                <td className="row-num" style={{ textAlign: "center", fontSize: 10 }}>{idx + 1}</td>
                <td><input {...register(`licenses.${idx}.licenseNo`)} style={INPUT_ST} /></td>
                <td><input {...register(`licenses.${idx}.pkgQty`)} style={NUM_ST} /></td>
                <td><input {...register(`licenses.${idx}.pkgUnit`)} style={INPUT_ST} /></td>
                <td><input {...register(`licenses.${idx}.grossWeightKg`)} style={NUM_ST} /></td>
                <td><input {...register(`licenses.${idx}.combinedPackingMark`)} style={INPUT_ST} /></td>
                <td><input {...register(`licenses.${idx}.combinedPackingQty`)} style={NUM_ST} /></td>
                <td><input {...register(`licenses.${idx}.combinedPackingUnit`)} style={INPUT_ST} /></td>
                <td><input {...register(`licenses.${idx}.hsnNo`)} style={INPUT_ST} /></td>
                <td style={{ textAlign: "center" }}>
                  <button
                    type="button"
                    className="btn btn--icon btn--xs"
                    onClick={() => remove(idx)}
                    aria-label="Remove row"
                  >
                    <X size={10} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
