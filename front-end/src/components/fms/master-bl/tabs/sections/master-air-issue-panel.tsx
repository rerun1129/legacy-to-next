"use client";

import { useFormContext, Controller, type FieldPath } from "react-hook-form";
import { useTranslations } from "next-intl";
import { TextBox, DateBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";

interface Props { variant?: AnyVariantConfig }

// House air-issue-panel 패턴 그대로 mirror — EXP only
const AIR_ISSUE_LABEL_TO_FIELD: Record<string, FieldPath<MasterBlFormValues>> = {
  "Issue Date":  "airDetail.issueDate",
  "Issue Place": "airDetail.issuePlace",
  "Signature":   "airDetail.signature",
};

// i18n 키 매핑 — issueFields 값(BE 고정 영어)을 필드 카탈로그 키로 변환
const ISSUE_FIELD_I18N_KEY: Record<string, string> = {
  "Issue Date":  "issueDate",
  "Issue Place": "issuePlace",
  "Signature":   "signature",
};

export function MasterAirIssuePanel({ variant }: Props) {
  const { register, control } = useFormContext<MasterBlFormValues>();
  const tp = useTranslations("fms.masterBl.entry.panels");
  const tf = useTranslations("fms.masterBl.entry.fields");
  if (!variant) return null;

  const panelScope = `master-air-issue-panel.${variant.key}`;

  const issueItems: FieldItemDef[] = variant.issueFields.map(f => {
    const fieldName = AIR_ISSUE_LABEL_TO_FIELD[f];
    const i18nKey   = ISSUE_FIELD_I18N_KEY[f];
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
      label: tp("issueInformation"),
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
