"use client";

import { Controller } from "react-hook-form";
import type { Control, UseFormReturn } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { LabelOption } from "@/components/shared/inputs/_types";
import type { PmsPerformanceFilter } from "./pms-performance-filter-model";

interface Props {
  control: Control<PmsPerformanceFilter>;
  register: UseFormReturn<PmsPerformanceFilter>["register"];
  t: (key: string) => string;
  groupedOptions: LabelOption[];
  issuedOptions: LabelOption[];
  documentTypeOptions: LabelOption[];
  documentStatusOptions: LabelOption[];
}

/** BMS 필드 그룹: 서류 종류(단일) / 서류 상태 / 서류번호 / 그룹번호 / 그룹화여부 / 발급여부 */
export function PmsPerformanceFilterBms({
  control,
  register,
  t,
  groupedOptions,
  issuedOptions,
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

      {/* 서류번호 */}
      <div className="lcn">
        <span className="lcn__label">{t("documentNoLike")}</span>
        <input
          {...register("documentNoLike")}
          className="lcn__name"
          placeholder={t("documentNoLike")}
          style={{ gridColumn: "2 / span 2" }}
        />
      </div>

      {/* 그룹 번호 */}
      <div className="lcn">
        <span className="lcn__label">{t("groupFinancialNo")}</span>
        <input
          {...register("groupFinancialNo")}
          className="lcn__name"
          placeholder={t("groupFinancialNo")}
          style={{ gridColumn: "2 / span 2" }}
        />
      </div>

      {/* 그룹화 여부 */}
      <div className="lcn">
        <span className="lcn__label">{t("grouped")}</span>
        <Controller
          control={control}
          name="grouped"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={groupedOptions}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
              style={{ gridColumn: "2 / span 2" }}
            />
          )}
        />
      </div>

      {/* 발급 여부 */}
      <div className="lcn">
        <span className="lcn__label">{t("issued")}</span>
        <Controller
          control={control}
          name="issued"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={issuedOptions}
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
