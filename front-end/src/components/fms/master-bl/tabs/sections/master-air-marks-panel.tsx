"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import type { MasterBlFormValues } from "../../master-bl-schema";

export function MasterAirMarksPanel() {
  const { control } = useFormContext<MasterBlFormValues>();
  const tp = useTranslations("fms.masterBl.entry.panels");

  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("marksNumbers")}</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <Controller
          control={control}
          name="desc.marks"
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
