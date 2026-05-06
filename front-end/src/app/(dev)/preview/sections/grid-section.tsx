"use client";

import { useState } from "react";
import { useForm, FormProvider } from "react-hook-form";
import { GridPreviewPanel, type DimPreviewFormValues, createDimPreviewDefaults } from "./grid-preview-panel";

export function GridSection() {
  const methods = useForm<DimPreviewFormValues>({ defaultValues: createDimPreviewDefaults() });
  const [isLoading, setIsLoading] = useState(false);

  return (
    <div style={{ padding: "24px 24px 0", fontFamily: "inherit", fontSize: 12 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Grid Preview</h1>
      <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
        <button
          style={{ padding: "4px 10px", fontSize: 11, border: "1px solid #ccc", borderRadius: 4, cursor: "pointer", background: "#fff" }}
          onClick={() => alert(JSON.stringify(methods.getValues(), null, 2))}
        >
          getValues
        </button>
        <button
          style={{
            padding: "4px 10px", fontSize: 11, borderRadius: 4, cursor: "pointer",
            border: "1px solid var(--border, #ccc)",
            background: isLoading ? "var(--accent, #3b82f6)" : "var(--surface, #fff)",
            color: isLoading ? "#fff" : "var(--ink-3, #555)",
          }}
          onClick={() => setIsLoading((v) => !v)}
        >
          isLoading
        </button>
      </div>
      <div style={{ borderTop: "1px solid #ddd", paddingTop: 12 }}>
        <div style={{ fontWeight: 600, marginBottom: 8 }}>Grid Preview (Cell Inputs Demo)</div>
        <FormProvider {...methods}>
          <div style={{ height: 480 }}>
            <GridPreviewPanel isLoading={isLoading} />
          </div>
        </FormProvider>
      </div>
    </div>
  );
}
