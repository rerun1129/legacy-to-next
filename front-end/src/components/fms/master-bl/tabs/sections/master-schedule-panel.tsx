"use client";

import { Search, Plus, Trash2 } from "lucide-react";
import { useFieldArray, type UseFormReturn } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { SeaScheduleStub, AirScheduleStub } from "./master-schedule-stub";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

// ── SEA schedule (form 연결) ────────────────────────────────
function SeaScheduleSection({ form }: { form: UseFormReturn<MasterBlFormValues> }) {
  return (
    <>
      {/* Liner & Vessel */}
      <div className="subhead"><div className="subhead__bar" />Liner &amp; Vessel</div>
      <div className="li">
        <span className="li__label is-required">Liner</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }}
            {...form.register("seaDetail.linerCode")} />
          <input style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }}
            placeholder="Liner Name" />
        </div>
      </div>
      <div className="li">
        <span className="li__label is-required">Vessel</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }}
            {...form.register("seaDetail.vesselCode")} />
          <input style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }}
            {...form.register("seaDetail.vesselName")} />
        </div>
      </div>
      <div className="li">
        <span className="li__label is-required">Voyage</span>
        <div className="li__input">
          <input style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }}
            {...form.register("seaDetail.voyageNo")} />
        </div>
      </div>

      {/* Ports */}
      <div className="subhead" style={{ marginTop: 8 }}><div className="subhead__bar" />Ports</div>
      {(["polCode", "podCode"] as const).map(field => (
        <div key={field} className="lcn" style={{ marginBottom: 4 }}>
          <span className="lcn__label is-required">{field === "polCode" ? "POL" : "POD"}</span>
          <div className="lcn__code" style={{ position: "relative" }}>
            <input style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }}
              {...form.register(field)} />
            <Search size={10} className="lcn__icon" />
          </div>
          <input className="lcn__name" placeholder="Port" style={{ fontSize: 10 }} />
        </div>
      ))}
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label">POR</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }}
            {...form.register("seaDetail.porCode")} />
          <Search size={10} className="lcn__icon" />
        </div>
        <input className="lcn__name" placeholder="Port" style={{ fontSize: 10 }} />
      </div>
      <div className="lcn" style={{ marginBottom: 4 }}>
        <span className="lcn__label">Dest</span>
        <div className="lcn__code" style={{ position: "relative" }}>
          <input style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }}
            {...form.register("seaDetail.finalDestCode")} />
          <Search size={10} className="lcn__icon" />
        </div>
        <input className="lcn__name" placeholder="Port" style={{ fontSize: 10 }} />
      </div>

      {/* Issue */}
      <div className="subhead" style={{ marginTop: 8 }}><div className="subhead__bar" />Issue</div>
      <div className="li">
        <span className="li__label">Issue Date</span>
        <div className="li__input">
          {/* PanelDateInput은 uncontrolled이므로 일반 input으로 8자리 날짜 입력 */}
          <input style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }}
            placeholder="yyyyMMdd"
            {...form.register("seaDetail.issueDate")} />
        </div>
      </div>
      <div className="li">
        <span className="li__label">Freight Term</span>
        <div className="li__input">
          <select style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }}
            {...form.register("freightTerm")}>
            <option value="PREPAID">Prepaid</option>
            <option value="COLLECT">Collect</option>
          </select>
        </div>
      </div>
      <div className="li">
        <span className="li__label">Vessel Nat.</span>
        <div className="li__input">
          <input style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }}
            {...form.register("seaDetail.vesselNationality")} />
        </div>
      </div>
    </>
  );
}

// ── AIR scheduleLegs useFieldArray ─────────────────────────
function AirLegsSection({ form }: { form: UseFormReturn<MasterBlFormValues> }) {
  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name:    "scheduleLegs",
  });

  return (
    <>
      <div className="subhead" style={{ display: "flex", alignItems: "center", gap: 8 }}>
        <div className="subhead__bar" />Schedule Legs
        <button
          type="button"
          className="btn btn--sm"
          style={{ marginLeft: "auto" }}
          onClick={() => append({ toCode: "", onBoardDt: "20000101", arrivalDt: "20000101" })}
        >
          <Plus size={10} />Add
        </button>
      </div>
      <div style={{ overflow: "auto" }}>
        <table className="grid--list">
          <thead>
            <tr>
              <th className="row-num">#</th>
              <th>To</th><th>By</th><th>Flight</th>
              <th>On Board</th><th>Time</th>
              <th>Arrival</th><th>Time</th>
              <th style={{ width: 24 }} />
            </tr>
          </thead>
          <tbody>
            {fields.map((field, i) => (
              <tr key={field.id}>
                <td className="row-num">{i + 1}</td>
                <td><input className="grid__cell-input" style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
                  {...form.register(`scheduleLegs.${i}.toCode`)} /></td>
                <td><input className="grid__cell-input"
                  {...form.register(`scheduleLegs.${i}.byCarrier`)} /></td>
                <td><input className="grid__cell-input" style={{ fontFamily: "var(--font-mono)" }}
                  {...form.register(`scheduleLegs.${i}.flightNo`)} /></td>
                <td><input className="grid__cell-input"
                  {...form.register(`scheduleLegs.${i}.onBoardDt`)} /></td>
                <td><input className="grid__cell-input"
                  {...form.register(`scheduleLegs.${i}.onBoardTm`)} /></td>
                <td><input className="grid__cell-input"
                  {...form.register(`scheduleLegs.${i}.arrivalDt`)} /></td>
                <td><input className="grid__cell-input"
                  {...form.register(`scheduleLegs.${i}.arrivalTm`)} /></td>
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
    </>
  );
}

export function MasterSchedulePanel({ variant, form }: Props) {
  if (!variant) return null;
  const panelScope = `master-schedule-panel.${variant.key}`;
  const isExp      = variant.direction === "EXP";
  const isSea      = variant.mode === "SEA";

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Schedule</span>
        <div className="panel__actions"><button type="button" className="btn btn--sm">Reset</button></div>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        {form ? (
          isSea
            ? <SeaScheduleSection form={form} />
            : <AirLegsSection form={form} />
        ) : (
          isSea
            ? <SeaScheduleStub panelScope={panelScope} isExp={isExp} />
            : <AirScheduleStub panelScope={panelScope} isExp={isExp} />
        )}
      </div>
    </div>
  );
}
