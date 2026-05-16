"use client";

import { Controller, type UseFormReturn } from "react-hook-form";
import { TextBox } from "@/components/shared/inputs/text-box";
import { DateBox } from "@/components/shared/inputs/date-box";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

export function MasterAirIssuePanel({ variant, form }: Props) {
  // AIR 모드에서만 표시 (air-exp만 issueFields 존재)
  if (!variant || variant.mode !== "AIR") return null;
  if (variant.issueFields.length === 0) return null;
  if (!form) return <AirIssuePanelStub />;

  return <AirIssuePanelContent variant={variant} form={form} />;
}

function AirIssuePanelStub() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Issue Information</span>
      </div>
    </div>
  );
}

function AirIssuePanelContent({
  variant,
  form,
}: {
  variant: AnyVariantConfig;
  form: UseFormReturn<MasterBlFormValues>;
}) {
  const { register, control } = form;
  const panelScope = `master-air-issue-panel.${variant.key}`;

  // §bl-variants MASTER_VARIANTS air-exp.issueFields: ['Issue Date', 'Signature', 'Issue Place']
  // airDetail.* nested path — BE Phase 2 AirDetailRequest 정합
  // §BE-sync — issueDate/issuePlace/signature는 @NotBlank(AirMasterGroup) → FE is-required 표시
  const FIELD_MAP: Record<string, { path: "airDetail.issueDate" | "airDetail.issuePlace" | "airDetail.signature"; isDate: boolean; required: boolean }> = {
    "Issue Date":  { path: "airDetail.issueDate",  isDate: true,  required: true  },
    "Issue Place": { path: "airDetail.issuePlace", isDate: false, required: true  },
    "Signature":   { path: "airDetail.signature",  isDate: false, required: true  },
  };

  const issueItems: FieldItemDef[] = variant.issueFields
    .filter((f): f is keyof typeof FIELD_MAP => f in FIELD_MAP)
    .map((f) => {
      const { path, isDate, required } = FIELD_MAP[f];
      return {
        key: f.toLowerCase().replace(/[^a-z0-9]/g, "-"),
        render: () => (
          <div className="li">
            <span className={`li__label${required ? " is-required" : ""}`}>{f}</span>
            <div className="li__input">
              {isDate ? (
                <Controller
                  control={control}
                  name={path}
                  render={({ field }) => (
                    <DateBox
                      variant="panel"
                      name={field.name}
                      value={field.value as string}
                      onChange={field.onChange}
                      onBlur={field.onBlur}
                      ref={field.ref}
                    />
                  )}
                />
              ) : (
                <TextBox variant="panel" {...register(path)} />
              )}
            </div>
          </div>
        ),
      };
    });

  const widgetFields: FieldWidgetDef[] = [
    {
      key:   "issue",
      label: "Issue Information",
      render: () => (
        <FieldItemGrid
          itemScope={`${panelScope}.issue`}
          items={issueItems}
          cols={1}
          shouldShowRowControls={false}
        />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Issue Information</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={widgetFields} />
      </div>
    </div>
  );
}
