"use client";

import { useFormContext, Controller, type FieldPath } from "react-hook-form";
import { useTranslations } from "next-intl";
import { TextBox, DateBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

interface Props { variant?: AnyVariantConfig }

// §6.49 ⑯ — airDetail nested 경로 정합 (seaDetail 경로 오기입 교정)
const AIR_ISSUE_LABEL_TO_FIELD: Record<string, FieldPath<HouseBlFormValues>> = {
  "Issue Date":  "airDetail.issueDate",
  "Issue Place": "airDetail.issuePlace",
  "Signature":   "airDetail.signature",
};

// issue field label (English) → fields.* i18n key
const ISSUE_LABEL_TO_KEY: Record<string, string> = {
  "Issue Date":  "issueDate",
  "Issue Place": "issuePlace",
  "Signature":   "signature",
};

export function AirIssuePanel({ variant }: Props) {
  const tf = useTranslations("fms.houseBl.entry.fields");
  const tp = useTranslations("fms.houseBl.entry.panels");
  const { register, control } = useFormContext<HouseBlFormValues>();
  if (!variant) return null;

  const panelScope = `air-issue-panel.${variant.key}`;

  const issueItems: FieldItemDef[] = variant.issueFields.map(f => {
    const fieldName = AIR_ISSUE_LABEL_TO_FIELD[f];
    const i18nKey   = ISSUE_LABEL_TO_KEY[f];
    const label     = i18nKey ? tf(i18nKey) : f;
    return {
      key:    f.toLowerCase().replace(/[^a-z0-9]/g, "-"),
      render: () => (
        <div className="li">
          <span className="li__label">{label}</span>
          <div className="li__input">
            {f.includes("Date")
              ? fieldName
                ? <Controller
                    control={control}
                    name={fieldName}
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
                : <DateBox variant="panel" />
              : <TextBox variant="panel" {...(fieldName ? register(fieldName) : {})} />}
          </div>
        </div>
      ),
    };
  });

  const widgetFields: FieldWidgetDef[] = [
    {
      key:   "issue",
      label: tf("issueInformation"),
      render: () => (
        <FieldItemGrid
          itemScope={`${panelScope}.issue`}
          items={issueItems}
          cols={2}
        />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{tp("issueInformation")}</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={widgetFields} />
      </div>
    </div>
  );
}
