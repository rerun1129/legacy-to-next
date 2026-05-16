"use client";

import { useFormContext, Controller } from "react-hook-form";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import type { MasterBlFormValues } from "../../master-bl-schema";

export function MasterAirRemarkPanel() {
  const { control } = useFormContext<MasterBlFormValues>();

  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Remark</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <Controller
          control={control}
          name="remark"
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
