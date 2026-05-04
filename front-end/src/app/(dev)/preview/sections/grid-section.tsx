"use client";

import { useForm, FormProvider } from "react-hook-form";
import { NonBLDimensionPanel } from "@/components/fms/non-bl/tabs/sections/nonbl-dimension-panel";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";
import { createEmptyNonBlFormValues } from "@/components/fms/non-bl/non-bl-defaults";

export function GridSection() {
  const methods = useForm<NonBlFormValues>({ defaultValues: createEmptyNonBlFormValues() });

  return (
    <div style={{ padding: "24px 24px 0", fontFamily: "inherit", fontSize: 12 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Grid Preview</h1>
      <button
        style={{ padding: "4px 10px", fontSize: 11, border: "1px solid #ccc", borderRadius: 4, marginBottom: 16, cursor: "pointer", background: "#fff" }}
        onClick={() => alert(JSON.stringify(methods.getValues(), null, 2))}
      >
        getValues
      </button>
      <div style={{ borderTop: "1px solid #ddd", paddingTop: 12 }}>
        <div style={{ fontWeight: 600, marginBottom: 8 }}>Standard Grid (Non B/L Dimension)</div>
        <FormProvider {...methods}>
          <div style={{ height: 480 }}>
            <NonBLDimensionPanel />
          </div>
        </FormProvider>
      </div>
    </div>
  );
}
