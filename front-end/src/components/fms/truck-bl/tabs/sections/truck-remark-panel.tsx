"use client";

import { useFormContext } from "react-hook-form";
import { TextArea } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";

export function TruckRemarkPanel() {
  const { register } = useFormContext<TruckBlFormValues>();
  return (
    <div className="panel" style={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Remark</span>
      </div>
      <div className="panel__body" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <TextArea
          variant="panel"
          {...register("remark")}
          style={{ flex: 1, minHeight: 0, resize: "none", width: "100%" }}
        />
      </div>
    </div>
  );
}
