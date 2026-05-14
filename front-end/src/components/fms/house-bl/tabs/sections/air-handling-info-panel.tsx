"use client";

import { useFormContext, Controller } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

export function AirHandlingInfoPanel() {
  const { control } = useFormContext<HouseBlFormValues>();
  const { options, placeholder } = useEnumOptions("HandlingInfoCode");

  return (
    <div className="panel panel--col-flex">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Handling Information</span>
      </div>
      <div className="panel__body panel__body--scroll-flex2" style={{ display: "flex", flexDirection: "column", gap: 4 }}>
        <Controller
          name="handlingInfoCode"
          control={control}
          render={({ field }) => (
            <ComboBox variant="panel" options={options} placeholder={placeholder} value={field.value} onChange={field.onChange} />
          )}
        />
        <Controller
          name="handlingInfoText"
          control={control}
          render={({ field }) => (
            <LineNumberTextarea
              name={field.name}
              value={field.value as string | undefined}
              onChange={field.onChange}
              onBlur={field.onBlur}
              style={{ flex: 1 }}
            />
          )}
        />
      </div>
    </div>
  );
}
