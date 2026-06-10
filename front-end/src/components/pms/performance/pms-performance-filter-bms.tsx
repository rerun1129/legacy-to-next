"use client";

import { Controller } from "react-hook-form";
import type { Control } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { LabelOption } from "@/components/shared/inputs/_types";
import type { PmsPerformanceFilter } from "./pms-performance-filter-model";

interface Props {
  control: Control<PmsPerformanceFilter>;
  t: (key: string) => string;
  documentTypeOptions: LabelOption[];
  documentStatusOptions: LabelOption[];
}

/** BMS 필드 그룹: 서류 종류 / 서류 상태 (정형 2개) */
export function PmsPerformanceFilterBms({
  control,
  t,
  documentTypeOptions,
  documentStatusOptions,
}: Props) {
  return (
    <>
      {/* 서류 종류 — 단일 선택 ComboBox (전체→[], 선택→[value]) */}
      <div className="lcn">
        <span className="lcn__label">{t("documentType")}</span>
        <Controller
          control={control}
          name="documentTypes"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={documentTypeOptions}
              value={field.value[0] ?? ""}
              onChange={(e) => {
                const v = e.target.value;
                field.onChange(v ? [v] : []);
              }}
              onBlur={field.onBlur}
              name={field.name}
              style={{ gridColumn: "2 / span 2" }}
            />
          )}
        />
      </div>

      {/* 서류 상태 */}
      <div className="lcn">
        <span className="lcn__label">{t("documentStatus")}</span>
        <Controller
          control={control}
          name="documentStatus"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={documentStatusOptions}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
              style={{ gridColumn: "2 / span 2" }}
            />
          )}
        />
      </div>
    </>
  );
}
