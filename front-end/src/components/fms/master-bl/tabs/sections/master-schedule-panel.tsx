"use client";

import { useFormContext, useFieldArray, Controller, type UseFormReturn, type UseFormRegister, type Control } from "react-hook-form";
import { Plus, Minus, Search } from "lucide-react";
import { GridList } from "@/components/shared/grid-list";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { buildAirScheduleLegCols, type LegRow } from "@/components/fms/_shared/air-schedule-legs-cols";
import { Button } from "@/components/shared/button";

interface Props { variant?: AnyVariantConfig; form?: UseFormReturn<MasterBlFormValues> }

// ── 공통 헬퍼 ──────────────────────────────────────────────
function SchedField({ label, name, req, type = "text" }: { label: string; name: string; req?: boolean; type?: string }) {
  const { register, control } = useFormContext();
  return (
    <div className="li">
      <span className={`li__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="li__input">
        {type === "date" ? (
          <Controller
            control={control}
            name={name}
            render={({ field }) => (
              <PanelDateInput
                required={req}
                value={field.value as string}
                onChange={field.onChange}
                onBlur={field.onBlur}
                ref={field.ref}
              />
            )}
          />
        ) : (
          <input style={{ width: "100%", height: 22, padding: "0 6px", fontSize: 10 }} {...register(name)} />
        )}
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
        <input style={{ width: 70, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register(codeField)} />
        <input style={{ flex: 1, height: 22, padding: "0 6px", fontSize: 10 }} {...register(nameField)} />
      </div>
    </div>
  );
}

function LcnField({ label, req, codeField, nameField }: { label: string; req?: boolean; codeField: string; nameField: string }) {
  const { register } = useFormContext();
  return (
    <div className="lcn" style={{ marginBottom: 4 }}>
      <span className={`lcn__label${req ? " is-required" : ""}`}>{label}</span>
      <div className="lcn__code" style={{ position: "relative" }}>
        <input placeholder="UNLOC" style={{ width: "100%", height: 24, padding: "0 20px 0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register(codeField)} />
        <Search size={10} className="lcn__icon" />
      </div>
      <input className="lcn__name" placeholder="Port" style={{ fontSize: 10 }} {...register(nameField)} />
    </div>
  );
}

// ── Sea schedule ────────────────────────────────────────────
function buildSeaFields(panelScope: string, isExp: boolean): FieldWidgetDef[] {
  const linerItems: FieldItemDef[] = [
    { key: "liner",  render: () => <CodeNameField label="Liner"  codeField="seaDetail.linerCode"  nameField="seaDetail.linerName"  req /> },
    { key: "vessel", render: () => <SchedField    label="Vessel" name="seaDetail.vesselName" req /> },
    { key: "voyage", render: () => <SchedField    label="Voyage" name="seaDetail.voyageNo"   req /> },
    { key: "etd",    render: () => <SchedField    label="ETD"    name="etd"                  req type="date" /> },
    { key: "eta",    render: () => <SchedField    label="ETA"    name="eta"                  req type="date" /> },
  ];
  const portItems: FieldItemDef[] = [
    { key: "pol",      render: () => <LcnField label="POL"      req  codeField="polCode"      nameField="seaDetail.polName" /> },
    { key: "pod",      render: () => <LcnField label="POD"      req  codeField="podCode"      nameField="seaDetail.podName" /> },
    { key: "delivery", render: () => <LcnField label="Delivery"      codeField="seaDetail.deliveryCode" nameField="seaDetail.deliveryName" /> },
  ];
  const issueItems: FieldItemDef[] = [
    { key: "issue-date",   render: () => <SchedField label="Issue Date"   name="seaDetail.issueDate"         type="date" /> },
    { key: "freight-term", render: () => <SchedField label="Freight Term" name="seaDetail.freightTermDetail" /> },
  ];

  return [
    { key: "liner-vessel", label: "Liner & Vessel", render: () => <FieldItemGrid itemScope={`${panelScope}.liner`} items={linerItems} /> },
    { key: "ports",        label: "Ports",          render: () => <FieldItemGrid itemScope={`${panelScope}.ports`} items={portItems} shouldShowRowControls={false} /> },
    ...(isExp ? [{ key: "issue", label: "Issue", render: () => <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} /> }] : []),
  ];
}

// ── Air schedule ────────────────────────────────────────────
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
    { key: "issue-date",  render: () => <SchedField label="Issue Date"  name="seaDetail.issueDate"  type="date" /> },
    { key: "signature",   render: () => <SchedField label="Signature"   name="seaDetail.signature"  /> },
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
