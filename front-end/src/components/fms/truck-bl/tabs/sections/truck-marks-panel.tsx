"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";

export function TruckMarksPanel() {
  // Rules of Hooks: unconditionally at top
  const tp = useTranslations("fms.truckBl.entry.panels");

  const { control } = useFormContext<TruckBlFormValues>();

  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("marksNumbers")}</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <Controller
          control={control}
          name="marks"
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
