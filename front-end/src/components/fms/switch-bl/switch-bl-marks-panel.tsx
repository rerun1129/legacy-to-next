"use client";

import { useFormContext, Controller } from "react-hook-form";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";
import type { SwitchBlFormValues } from "./switch-bl-modal";

export function SwitchBlMarksPanel() {
  const { control } = useFormContext<SwitchBlFormValues>();

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0 }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Marks & Numbers</span>
      </div>
      <div
        className="panel__body"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}
      >
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
