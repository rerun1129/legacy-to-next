"use client";

import { useFormContext, Controller } from "react-hook-form";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

export function NonBLRemarkPanel() {
  const { control } = useFormContext<NonBlFormValues>();

  return (
    <div className="panel panel--col-flex">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Remark</span>
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
              placeholder="Remark"
              style={{ flex: 1 }}
            />
          )}
        />
      </div>
    </div>
  );
}
