"use client";

import { Plus, Trash2 } from "lucide-react";
import { useFieldArray, type UseFormReturn } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

const AIR_CHARGE_COLS = [
  { key: "freightCode",    label: "Code",      width: 50 },
  { key: "currencyCode",   label: "Currency",  width: 50 },
  { key: "per",            label: "Per",       width: 40 },
  { key: "freightTerm",    label: "Term",      width: 50 },
  { key: "grossWeightKg",  label: "G/W",       width: 60, numeric: true },
  { key: "rateClass",      label: "Class",     width: 44 },
  { key: "chargeWeightKg", label: "Chg W/T",   width: 60, numeric: true },
  { key: "rate",           label: "Rate",      width: 60, numeric: true },
] as const;

type ColKey = typeof AIR_CHARGE_COLS[number]["key"];

export function MasterAirChargesPanel({ variant, form }: Props) {
  // AIR 모드에서만 표示
  if (!variant || variant.mode !== "AIR") return null;

  if (!form) {
    return (
      <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Air Charges</span>
        </div>
        <div style={{ overflow: "auto", flex: 1 }}>
          <table className="grid--list">
            <thead>
              <tr>
                <th className="row-num">#</th>
                {AIR_CHARGE_COLS.map(c => <th key={c.key} className={c.numeric ? "is-num" : ""}>{c.label}</th>)}
              </tr>
            </thead>
            <tbody />
          </table>
        </div>
      </div>
    );
  }

  return <AirChargesGrid form={form} />;
}

function AirChargesGrid({ form }: { form: UseFormReturn<MasterBlFormValues> }) {
  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name:    "airCharges",
  });

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Air Charges</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button
            type="button"
            className="btn btn--sm"
            onClick={() => append({
              freightCode: "", currencyCode: "", per: "", freightTerm: "",
              grossWeightKg: undefined, rateClass: "", chargeWeightKg: undefined, rate: undefined,
            })}
          >
            <Plus size={10} />Add
          </button>
        </div>
      </div>
      <div style={{ overflow: "auto", flex: 1 }}>
        <table className="grid--list">
          <thead>
            <tr>
              <th className="row-num">#</th>
              {AIR_CHARGE_COLS.map(c => <th key={c.key} className={c.numeric ? "is-num" : ""}>{c.label}</th>)}
              <th style={{ width: 24 }} />
            </tr>
          </thead>
          <tbody>
            {fields.map((field, i) => (
              <tr key={field.id}>
                <td className="row-num">{i + 1}</td>
                {AIR_CHARGE_COLS.map(col => (
                  <td key={col.key} className={col.numeric ? "is-num" : ""}>
                    <input
                      type={col.numeric ? "number" : "text"}
                      step={col.numeric ? "any" : undefined}
                      className="grid__cell-input"
                      {...form.register(
                        `airCharges.${i}.${col.key as ColKey}` as const,
                        col.numeric ? { valueAsNumber: true } : undefined,
                      )}
                    />
                  </td>
                ))}
                <td>
                  <button type="button" onClick={() => remove(i)} style={{ background: "none", border: "none", cursor: "pointer", padding: 2 }}>
                    <Trash2 size={10} />
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
