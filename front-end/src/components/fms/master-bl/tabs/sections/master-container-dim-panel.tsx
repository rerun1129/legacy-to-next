"use client";

import { Plus, Trash2 } from "lucide-react";
import { useFieldArray, type UseFormReturn } from "react-hook-form";
import { getModeLabels } from "@/lib/bl-mode-labels";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

const SEA_ROWS: { cno: string; type: string; seal: string; pkg: number; unit: string; gw: string; cbm: string }[] = [];

// AIR dims controlled grid
function AirDimsGrid({ form }: { form: UseFormReturn<MasterBlFormValues> }) {
  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name:    "dims",
  });

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 4, gap: 4 }}>
        <button type="button" className="btn btn--sm">Load Dimension</button>
        <button
          type="button"
          className="btn btn--sm"
          onClick={() => append({ lengthCm: undefined, widthCm: undefined, heightCm: undefined, quantity: undefined, cbm: undefined, volumeWeightKg: undefined })}
        >
          <Plus size={10} />
        </button>
      </div>
      <table className="grid--list">
        <thead>
          <tr>
            <th className="row-num">#</th>
            <th className="is-num">Length</th>
            <th className="is-num">Width</th>
            <th className="is-num">Height</th>
            <th className="is-num">Qty</th>
            <th className="is-num">CBM</th>
            <th className="is-num">Vol. Wt.</th>
            <th style={{ width: 24 }} />
          </tr>
        </thead>
        <tbody>
          {fields.map((field, i) => (
            <tr key={field.id}>
              <td className="row-num">{i + 1}</td>
              {(["lengthCm", "widthCm", "heightCm", "quantity", "cbm", "volumeWeightKg"] as const).map(k => (
                <td key={k} className="is-num">
                  <input
                    type="number"
                    step="any"
                    className="grid__cell-input"
                    {...form.register(`dims.${i}.${k}`, { valueAsNumber: true })}
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
    </>
  );
}

// AIR dims stub (form 없을 때)
const AIR_STUB_ROWS: { length: string; width: string; height: string; qty: string; cbm: string; volWt: string }[] = [];

function AirDimsStub() {
  return (
    <table className="grid--list">
      <thead>
        <tr>
          <th className="row-num">#</th>
          <th className="is-num">Length</th><th className="is-num">Width</th>
          <th className="is-num">Height</th><th className="is-num">Qty</th>
          <th className="is-num">CBM</th><th className="is-num">Vol. Wt.</th>
        </tr>
      </thead>
      <tbody>
        {AIR_STUB_ROWS.map((r, i) => (
          <tr key={`${r.length}-${r.width}-${r.height}`}>
            <td className="row-num">{i + 1}</td>
            {(["length","width","height","qty","cbm","volWt"] as const).map(k => (
              <td key={k} className="is-num"><NumericCell defaultValue={r[k]} /></td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export function MasterContainerDimPanel({ variant, form }: Props) {
  if (!variant) return null;
  const isSea = variant.mode === "SEA";
  const ml    = getModeLabels(variant.mode);

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{ml.containerPanel}</span>
        {isSea && <span className="panel__rowcount">{SEA_ROWS.length}</span>}
      </div>
      <div style={{ overflow: "auto", flex: 1 }}>
        {isSea ? (
          <table className="grid--list">
            <thead>
              <tr>
                <th className="row-num">#</th>
                <th>Container No.</th><th>Type</th><th>Seal No.</th>
                <th className="is-num">Pkg</th><th>Unit</th>
                <th className="is-num">G/W</th><th className="is-num">CBM</th><th>SOC</th>
              </tr>
            </thead>
            <tbody>
              {SEA_ROWS.map((r, i) => (
                <tr key={r.cno} style={{ color: "var(--ink-3)" }}>
                  <td className="row-num">{i + 1}</td>
                  <td className="cell-mono">{r.cno}</td><td>{r.type}</td><td className="cell-mono">{r.seal}</td>
                  <td className="is-num cell-mono">{r.pkg}</td><td>{r.unit}</td>
                  <td className="is-num cell-mono">{r.gw}</td><td className="is-num cell-mono">{r.cbm}</td><td>N</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          form ? <AirDimsGrid form={form} /> : <AirDimsStub />
        )}
      </div>
    </div>
  );
}
