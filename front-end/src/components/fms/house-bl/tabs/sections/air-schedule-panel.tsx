import type React from "react";
import { useFormContext, useFieldArray, Controller, type FieldPath } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import { GridList } from "@/components/shared/grid-list";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { buildAirScheduleLegCols, type LegRow } from "@/components/fms/_shared/air-schedule-legs-cols";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

interface Props { variant?: AnyVariantConfig }

const LI_ST: React.CSSProperties = { width: "100%", height: 22, padding: "0 8px", fontSize: 10 };

// 라벨 → RHF 필드명 매핑 (Air Issue Information 섹션)
const AIR_ISSUE_LABEL_TO_FIELD: Record<string, FieldPath<HouseBlFormValues>> = {
  "Issue Date":  "seaDetail.issueDate",
  "Issue Place": "seaDetail.issuePlace",
  "Signature":   "seaDetail.signature",
};

export function AirSchedulePanel({ variant }: Props) {
  const { register, control } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "scheduleLegs" });

  if (!variant) return null;
  const isExp      = variant.direction === "EXP";
  const panelScope = `air-schedule-panel.${variant.key}`;

  const airlineItems: FieldItemDef[] = [
    {
      key: "airline",
      render: () => (
        <div className="li">
          <span className="li__label is-required">{isExp ? "Airline" : "Carrier"}</span>
          <div className="li__input" style={{ gap: 4 }}>
            <input style={{ width: 60, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }}
              {...register("airlineCode")} />
            <input style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }}
              {...register("airlineName")} />
          </div>
        </div>
      ),
    },
    {
      key: "departure",
      render: () => (
        <div className="li">
          <span className="li__label is-required">Departure</span>
          <div className="li__input" style={{ gap: 4 }}>
            <input style={{ width: 60, height: 22, padding: "0 6px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register("pol")} />
            <input style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} {...register("polName")} />
          </div>
        </div>
      ),
    },
  ];

  const issueItems: FieldItemDef[] = variant.issueFields.map(f => {
    const fieldName = AIR_ISSUE_LABEL_TO_FIELD[f];
    return {
      key:    f.toLowerCase().replace(/[^a-z0-9]/g, "-"),
      render: () => (
        <div className="li">
          <span className="li__label">{f}</span>
          <div className="li__input">
            {f.includes("Date")
              ? fieldName
                ? <Controller
                    control={control}
                    name={fieldName}
                    render={({ field }) => (
                      <PanelDateInput
                        value={field.value as string}
                        onChange={field.onChange}
                        onBlur={field.onBlur}
                        ref={field.ref}
                      />
                    )}
                  />
                : <PanelDateInput />
              : <input style={LI_ST} {...(fieldName ? register(fieldName) : {})} />}
          </div>
        </div>
      ),
    };
  });

  const widgetFields: FieldWidgetDef[] = [
    {
      key:   "airline",
      label: isExp ? "Airline" : "Carrier",
      render: () => (
        <FieldItemGrid
          itemScope={`${panelScope}.airline`}
          items={airlineItems}
          cols={1}
          shouldShowRowControls={false}
        />
      ),
    },
    {
      key:   "legs",
      label: "Schedule Legs",
      render: () => {
        function handleAdd() {
          append({ toCode: "", byCarrier: "", flightNo: "", onBoardDt: "", onBoardTm: "", arrivalDt: "", arrivalTm: "" });
        }
        function handleRemove() {
          if (fields.length > 0) remove(fields.length - 1);
        }
        return (
          <div>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 4 }}>
              <span className="panel__rowcount">{fields.length}</span>
              <div className="panel__actions" style={{ display: "flex", gap: 4 }}>
                <button type="button" className="btn btn--sm btn--icon btn--success" onClick={handleAdd}><Plus size={12} /></button>
                <button type="button" className="btn btn--sm btn--icon btn--danger" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
              </div>
            </div>
            <div style={{ overflow: "auto" }}>
              <GridList
                columns={buildAirScheduleLegCols(register, "scheduleLegs")}
                data={fields as unknown as LegRow[]}
                rowKey={(row) => row.id ?? ""}
              />
            </div>
          </div>
        );
      },
    },
    ...(variant.issueFields.length > 0
      ? [{
          key:   "issue",
          label: "Issue Information",
          render: () => (
            <>
              <div className="subhead"><div className="subhead__bar" />Issue Information</div>
              <FieldItemGrid itemScope={`${panelScope}.issue`} items={issueItems} cols={1} shouldShowRowControls={false} />
            </>
          ),
        }]
      : []),
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={widgetFields} />
      </div>
    </div>
  );
}
