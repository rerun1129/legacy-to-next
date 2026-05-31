"use client";

import { useFieldArray, useFormContext } from "react-hook-form";
import { useTranslations } from "next-intl";
import { Plus, X } from "lucide-react";
import type { HouseBlFormValues } from "../house-bl-schema";

// TRUCK 전용 트럭오더 다중행 그리드

const EMPTY_ROW = {
  truckOrderNo: "", pkgQty: "", pkgUnit: "", grossWeightKg: "", cbm: "",
  truckNo: "", truckType: "", driver: "", mobileNo: "",
  containerNo: "", containerType: "", sealNo1: "", sealNo2: "", sealNo3: "",
} as const;

const INPUT_ST: React.CSSProperties = { width: "100%", height: 24, padding: "0 4px", fontSize: 10 };
const NUM_ST:   React.CSSProperties = { ...INPUT_ST, textAlign: "right" };

export function TruckOrderPanel() {
  const tp = useTranslations("fms.houseBl.entry.panels");
  const tf = useTranslations("fms.houseBl.entry.fields");
  const { control, register } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "truckOrders" });

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("truckOrder")}</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={() => append({ ...EMPTY_ROW })}>
            <Plus size={12} />
          </button>
        </div>
      </div>

      <div style={{ flex: 1, overflow: "auto" }}>
        <table className="grid-table" style={{ minWidth: 1000 }}>
          <thead>
            <tr>
              <th style={{ width: 32 }}>#</th>
              <th style={{ width: 110 }}>{tf("truckOrderNo")}</th>
              <th style={{ width: 55 }}>{tf("pkgQty")}</th>
              <th style={{ width: 55 }}>{tf("unit")}</th>
              <th style={{ width: 80 }}>{tf("gwKg")}</th>
              <th style={{ width: 70 }}>{tf("cbm")}</th>
              <th style={{ width: 80 }}>{tf("truckNo")}</th>
              <th style={{ width: 70 }}>{tf("containerType")}</th>
              <th style={{ width: 90 }}>{tf("driver")}</th>
              <th style={{ width: 100 }}>{tf("mobileNo")}</th>
              <th style={{ width: 110 }}>{tf("containerNo")}</th>
              <th style={{ width: 55 }}>{tf("seal1")}</th>
              <th style={{ width: 32 }} />
            </tr>
          </thead>
          <tbody>
            {fields.length === 0 && (
              <tr>
                <td colSpan={13} style={{ textAlign: "center", padding: 8, fontSize: 11, color: "var(--ink-3)" }}>
                  {tf("noTruckOrderRows")}
                </td>
              </tr>
            )}
            {fields.map((field, idx) => (
              <tr key={field.id}>
                <td className="row-num" style={{ textAlign: "center", fontSize: 10 }}>{idx + 1}</td>
                <td><input {...register(`truckOrders.${idx}.truckOrderNo`)} style={INPUT_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.pkgQty`)} style={NUM_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.pkgUnit`)} style={INPUT_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.grossWeightKg`)} style={NUM_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.cbm`)} style={NUM_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.truckNo`)} style={INPUT_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.truckType`)} style={INPUT_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.driver`)} style={INPUT_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.mobileNo`)} style={INPUT_ST} /></td>
                <td><input {...register(`truckOrders.${idx}.containerNo`)} style={{ ...INPUT_ST, fontFamily: "var(--font-mono)" }} /></td>
                <td><input {...register(`truckOrders.${idx}.sealNo1`)} style={INPUT_ST} /></td>
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
