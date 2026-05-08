"use client";

import { useState } from "react";
import { useForm, FormProvider } from "react-hook-form";
import { GridPreviewPanel, type DimPreviewFormValues, createDimPreviewDefaults } from "./grid-preview-panel";

export function GridSection() {
  const methods = useForm<DimPreviewFormValues>({ defaultValues: createDimPreviewDefaults() });
  const managedMethods = useForm<DimPreviewFormValues>({ defaultValues: createDimPreviewDefaults() });
  const [isLoading, setIsLoading] = useState(false);

  return (
    <div style={{ padding: "24px 24px 0", fontFamily: "inherit", fontSize: 12 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Grid Preview</h1>

      <div style={{
        fontSize: 11, color: "#444", padding: "8px 10px",
        background: "#f0f9ff", borderRadius: 4, marginBottom: 16,
        border: "1px solid #bae6fd",
      }}>
        <strong>동작 안내:</strong>
        <ul style={{ margin: "4px 0 0", paddingLeft: 16, lineHeight: 1.6 }}>
          <li>컬럼 헤더 우측 경계 4px 드래그 → 너비 조정 (PlainGridList: 세션 한정 / ManagedGridList: localStorage 영속)</li>
          <li>셀 클릭 → 선택 overlay. 외부 클릭 → 해제 (onClearRow prop 필수).</li>
          <li>셀 편집 단일 outline (이전 inset ring + 외곽 ring 중복 제거됨).</li>
          <li>행 클릭 → is-selected 강조. 다른 그리드 클릭 → 해제.</li>
        </ul>
      </div>

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

      <div style={{ borderTop: "1px solid var(--border, #e5e7eb)", paddingTop: 16, marginTop: 24 }}>
        <div style={{ fontWeight: 600, fontSize: 12, marginBottom: 8, color: "var(--ink-4, #6b7280)" }}>
          ManagedGridList (gridId=&quot;catalog-managed-demo&quot; — 컬럼 너비 localStorage 영속)
        </div>
        <FormProvider {...managedMethods}>
          <div style={{ height: 400 }}>
            <GridPreviewPanel isLoading={false} gridId="catalog-managed-demo" />
          </div>
        </FormProvider>
      </div>
    </div>
  );
}
