"use client";

import { Plus, Trash2 } from "lucide-react";
import { useFieldArray, type UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { TextBox }   from "@/components/shared/inputs/text-box";
import { NumberBox } from "@/components/shared/inputs/number-box";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

// i18n 키로 참조 — 렌더 시 tf() 통해 번역
const AIR_CHARGE_COLS = [
  { key: "freightCode",    labelKey: "freight",    width: 50 },
  { key: "currencyCode",   labelKey: "currency",   width: 50 },
  { key: "per",            labelKey: "per",        width: 40 },
  { key: "freightTerm",    labelKey: "freightTerm",width: 50 },
  { key: "grossWeightKg",  labelKey: "gw",         width: 60, numeric: true },
  { key: "rateClass",      labelKey: "rateClass",  width: 44 },
  { key: "chargeWeightKg", labelKey: "chargeWT",   width: 60, numeric: true },
  { key: "rate",           labelKey: "rate",       width: 60, numeric: true },
] as const;

type ColKey = typeof AIR_CHARGE_COLS[number]["key"];

export function MasterAirChargesPanel({ variant, form }: Props) {
  const tp = useTranslations("fms.masterBl.entry.panels");
  const tf = useTranslations("fms.masterBl.entry.fields");

  // AIR 모드에서만 표시
  if (!variant || variant.mode !== "AIR") return null;

  if (!form) {
    return (
      <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tp("chargeInformation")}</span>
        </div>
        <div style={{ overflow: "auto", flex: 1 }}>
          <table className="grid--list">
            <thead>
              <tr>
                <th className="row-num">#</th>
                {AIR_CHARGE_COLS.map(c => <th key={c.key} className={"numeric" in c && c.numeric ? "is-num" : ""}>{tf(c.labelKey)}</th>)}
              </tr>
            </thead>
            <tbody />
          </table>
        </div>
      </div>
    );
  }

  return <AirChargesGrid form={form} tf={tf} tp={tp} />;
}

function AirChargesGrid({
  form,
  tf,
  tp,
}: {
  form: UseFormReturn<MasterBlFormValues>;
  tf: ReturnType<typeof useTranslations>;
  tp: ReturnType<typeof useTranslations>;
}) {
  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name:    "airCharges",
  });

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("chargeInformation")}</span>
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
              {AIR_CHARGE_COLS.map(c => <th key={c.key} className={"numeric" in c && c.numeric ? "is-num" : ""}>{tf(c.labelKey)}</th>)}
              <th style={{ width: 24 }} />
            </tr>
          </thead>
          <tbody>
            {fields.map((field, i) => (
              <tr key={field.id}>
                <td className="row-num">{i + 1}</td>
                {AIR_CHARGE_COLS.map(col => (
                  <td key={col.key} className={"numeric" in col && col.numeric ? "is-num" : ""}>
                    {"numeric" in col && col.numeric ? (
                      <NumberBox
                        variant="cell"
                        decimalPlaces={3}
                        {...form.register(
                          `airCharges.${i}.${col.key as ColKey}` as const,
                          { valueAsNumber: true },
                        )}
                      />
                    ) : (
                      <TextBox
                        variant="cell"
                        {...form.register(`airCharges.${i}.${col.key as ColKey}` as const)}
                      />
                    )}
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
