"use client";

import { useFormContext, useFieldArray, type UseFormReturn, type UseFormRegister, type Control } from "react-hook-form";
import { useTranslations } from "next-intl";
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
import { PolField, PodField, EtdField, EtaField } from "./master-schedule-sea-atoms";

interface Props { variant?: AnyVariantConfig; form?: UseFormReturn<MasterBlFormValues> }

// ── AIR schedule 헬퍼 (AIR 코드 격리 원칙 — 자체 수정 없이 재사용) ─────────
function SchedField({ label, name, req }: { label: string; name: string; req?: boolean }) {
  const { register } = useFormContext();
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        <TextBox variant="panel" {...register(name)} />
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
  tf: ReturnType<typeof useTranslations>,
): FieldWidgetDef[] {
  // §누락1 fix: airlineCode는 airDetail.airlineCode (seaDetail.linerCode 오기 수정)
  // §누락2 fix: polCode/podCode/etd/eta 위젯 추가 — BE @NotBlank 공통 필수 필드
  const carrierItems: FieldItemDef[] = [
    { key: "carrier",   render: () => <SchedField label={isExp ? tf("airline") : tf("carrier")} name="airDetail.airlineCode" req /> },
    { key: "departure", render: () => <SchedField label={tf("departure")} name="polCode" req /> },
  ];
  // §누락1 fix: issueDate/signature/issuePlace도 airDetail path로 수정 (seaDetail은 SEA 전용)
  const issueItems: FieldItemDef[] = [
    { key: "issue-date",  render: () => <SchedField label={tf("issueDate")}  name="airDetail.issueDate" /> },
    { key: "signature",   render: () => <SchedField label={tf("signature")}   name="airDetail.signature" /> },
    { key: "issue-place", render: () => <SchedField label={tf("issuePlace")} name="airDetail.issuePlace" /> },
  ];
  // §누락2 fix: BE @NotBlank 공통 필수 polCode/podCode/etd/eta — SEA atoms 재사용
  const portItems: FieldItemDef[] = [
    { key: "pol", render: () => <PolField /> },
    { key: "pod", render: () => <PodField /> },
  ];
  const dateItems: FieldItemDef[] = [
    { key: "etd", render: () => <EtdField /> },
    { key: "eta", render: () => <EtaField /> },
  ];

  return [
    {
      key: "carrier", label: tf("carrier"),
      render: () => <FieldItemGrid itemScope={`${panelScope}.carrier`} items={carrierItems} />,
    },
    {
      key: "ports", label: `${tf("pol")} / ${tf("pod")}`,
      render: () => <FieldItemGrid itemScope={`${panelScope}.ports`} items={portItems} cols={2} shouldShowRowControls={false} />,
    },
    {
      key: "dates", label: `${tf("etd")} / ${tf("eta")}`,
      render: () => <FieldItemGrid itemScope={`${panelScope}.dates`} items={dateItems} cols={2} shouldShowRowControls={false} />,
    },
    {
      key: "legs", label: tf("scheduleLegs"),
      render: () => <AirLegsWidget register={register} control={control} tf={tf} />,
    },
    ...(isExp ? [{ key: "issue", label: tf("issue"), render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];
}

function AirLegsWidget({
  register,
  control,
  tf,
}: {
  register: UseFormRegister<MasterBlFormValues>;
  control: Control<MasterBlFormValues>;
  tf: ReturnType<typeof useTranslations>;
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
      <div className="subhead"><div className="subhead__bar" />{tf("scheduleLegs")}</div>
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
  const tp = useTranslations("fms.masterBl.entry.panels");
  const tf = useTranslations("fms.masterBl.entry.fields");

  if (!variant) return null;
  const panelScope = `master-schedule-panel.${variant.key}`;
  const isExp      = variant.direction === "EXP";
  const fields     = variant.mode === "SEA"
    ? buildSeaFields(panelScope, isExp, tf)
    : buildAirFields(panelScope, isExp, register, control, tf);

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("schedule")}</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
