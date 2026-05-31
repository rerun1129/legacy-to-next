"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

export function NonBLRemarkPanel() {
  // Rules of Hooks: ALL hooks unconditionally before any early-return
  const tf = useTranslations("fms.nonBl.entry.fields");
  const tp = useTranslations("fms.nonBl.entry.panels");

  const { control } = useFormContext<NonBlFormValues>();

  return (
    <div className="panel panel--col-flex">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("remark")}</span>
      </div>
      <div className="panel__body panel__body--col">
        <Controller
          control={control}
          name="remark"
          render={({ field }) => (
            <LineNumberTextarea
              value={field.value ?? ""}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
              placeholder={tf("remark")}
              style={{ flex: 1 }}
            />
          )}
        />
      </div>
    </div>
  );
}
