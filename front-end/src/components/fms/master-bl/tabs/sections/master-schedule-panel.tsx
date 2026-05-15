"use client";

import { useFormContext, useFieldArray, type UseFormReturn, type UseFormRegister, type Control } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import { GridList } from "@/components/shared/grid-list";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { buildAirScheduleLegCols, type LegRow } from "@/components/fms/_shared/air-schedule-legs-cols";
import { Button } from "@/components/shared/button";
import { TextBox } from "@/components/shared/inputs/text-box";
import { buildSeaFields } from "./master-schedule-sea-fields";

interface Props { variant?: AnyVariantConfig; form?: UseFormReturn<MasterBlFormValues> }

// ── AIR schedule 헬퍼 (AIR 코드 격리 원칙 — 자체 수정 없이 재사용) ─────────
function SchedField({ label, name }: { label: string; name: string }) {
  const { register } = useFormContext();
  return (
    <div className="li">
      <span className="li__label">{label}</span>
      <div className="li__input">
        <TextBox variant="panel" {...register(name)} />
      </div>
    </div>
  );
}

function CodeNameField({ label, codeField, nameField, req }: { label: string; codeField: string; nameField: string; req?: boolean }) {
  const { register } = useFormContext();
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input" style={{ gap: 4 }}>
        <TextBox variant="panel" style={{ width: 70 }} {...register(codeField)} />
        <TextBox variant="panel" style={{ flex: 1 }} {...register(nameField)} />
      </div>
    </div>
  );
}

// ── AIR schedule (격리 원칙 — 내부 코드 자체 수정 없이 유지) ─────────────────
function buildAirFields(
  panelScope: string,
  isExp: boolean,
  register: UseFormRegister<MasterBlFormValues>,
  control: Control<MasterBlFormValues>,
): FieldWidgetDef[] {
  const carrierItems: FieldItemDef[] = [
    { key: "carrier",   render: () => <CodeNameField label={isExp ? "Airline" : "Carrier"} codeField="seaDetail.linerCode" nameField="seaDetail.linerName" req /> },
    { key: "departure", render: () => <CodeNameField label="Departure" codeField="polCode" nameField="seaDetail.polName" req /> },
  ];
  const issueItems: FieldItemDef[] = [
    { key: "issue-date",  render: () => <SchedField label="Issue Date"  name="seaDetail.issueDate" /> },
    { key: "signature",   render: () => <SchedField label="Signature"   name="seaDetail.signature" /> },
    { key: "issue-place", render: () => <SchedField label="Issue Place" name="seaDetail.issuePlace" /> },
  ];

  return [
    {
      key: "carrier", label: "Carrier",
      render: () => <FieldItemGrid itemScope={`${panelScope}.carrier`} items={carrierItems} />,
    },
    {
      key: "legs", label: "Schedule Legs",
      render: () => <AirLegsWidget register={register} control={control} />,
    },
    ...(isExp ? [{ key: "issue", label: "Issue", render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];
}

function AirLegsWidget({
  register,
  control,
}: {
  register: UseFormRegister<MasterBlFormValues>;
  control: Control<MasterBlFormValues>;
}) {
  const { fields, append, remove } = useFieldArray({ control, name: "scheduleLegs" });

  function handleAdd() {
    append({ toCode: "", byCarrier: "", flightNo: "", onBoardDt: "", onBoardTm: "", arrivalDt: "", arrivalTm: "" });
  }
  function handleRemove() {
    if (fields.length > 0) remove(fields.length - 1);
  }

  return (
    <>
      <div className="subhead"><div className="subhead__bar" />Schedule Legs</div>
      <div>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 4 }}>
          <span className="panel__rowcount">{fields.length}</span>
          <div className="panel__actions" style={{ display: "flex", gap: 4 }}>
            <Button variant="success" size="sm" iconOnly onClick={handleAdd}><Plus size={12} /></Button>
            <Button variant="danger" size="sm" iconOnly onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></Button>
          </div>
        </div>
        <div style={{ overflow: "auto" }}>
          <GridList
            columns={buildAirScheduleLegCols(register, control, "scheduleLegs")}
            data={fields as unknown as LegRow[]}
            rowKey={(row) => row.id ?? ""}
          />
        </div>
      </div>
    </>
  );
}

export function MasterSchedulePanel({ variant }: Props) {
  const { register, control } = useFormContext<MasterBlFormValues>();

  if (!variant) return null;
  const panelScope = `master-schedule-panel.${variant.key}`;
  const isExp      = variant.direction === "EXP";
  const fields     = variant.mode === "SEA"
    ? buildSeaFields(panelScope, isExp)
    : buildAirFields(panelScope, isExp, register, control);

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Schedule</span>
        <div className="panel__actions"><button type="button" className="btn btn--sm">Reset</button></div>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
