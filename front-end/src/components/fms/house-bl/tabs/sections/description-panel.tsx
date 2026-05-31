"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import { ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

export function DescriptionPanel() {
  const tp = useTranslations("fms.houseBl.entry.panels");
  const { control } = useFormContext<HouseBlFormValues>();
  const { options: clause1Options, placeholder: clause1Placeholder } = useEnumOptions("DescClause1");
  const { options: clause2Options, placeholder: clause2Placeholder } = useEnumOptions("DescClause2");

  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("description")}</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div style={{ display: "flex", gap: 4, marginBottom: 8, flexShrink: 0 }}>
          <Controller
            name="desc.descClause1"
            control={control}
            render={({ field }) => (
              <ComboBox
                variant="panel"
                options={clause1Options}
                placeholder={clause1Placeholder ?? "-- 부지약관 --"}
                value={field.value}
                onChange={field.onChange}
                style={{ flex: 1 }}
              />
            )}
          />
          <Controller
            name="desc.descClause2"
            control={control}
            render={({ field }) => (
              <ComboBox
                variant="panel"
                options={clause2Options}
                placeholder={clause2Placeholder ?? "-- 부지약관 --"}
                value={field.value}
                onChange={field.onChange}
                style={{ flex: 1 }}
              />
            )}
          />
        </div>
        <Controller
          control={control}
          name="desc.description"
          render={({ field }) => (
            <LineNumberTextarea
              name={field.name}
              value={field.value ?? ""}
              onChange={field.onChange}
              onBlur={field.onBlur}
              style={{ flex: 1, minHeight: 0 }}
            />
          )}
        />
      </div>
    </div>
  );
}
