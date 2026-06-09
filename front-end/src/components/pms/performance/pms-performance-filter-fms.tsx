"use client";

import { Controller } from "react-hook-form";
import type { Control } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import type { LabelOption } from "@/components/shared/inputs/_types";
import type { PmsPerformanceFilter } from "./pms-performance-filter-model";

interface Props {
  control: Control<PmsPerformanceFilter>;
  t: (key: string) => string;
  jobDivOptionsWithAll: LabelOption[];
  jobDivLoading: boolean;
  jobDivPlaceholder: string | undefined;
  boundOptionsWithAll: LabelOption[];
  boundLoading: boolean;
  boundPlaceholder: string | undefined;
  dateKindOptions: LabelOption[];
}

/** FMS 필드 그룹: 업무구분 / 수출입 / 일자범위 (정형 5개) */
export function PmsPerformanceFilterFms({
  control,
  t,
  jobDivOptionsWithAll,
  jobDivLoading,
  jobDivPlaceholder,
  boundOptionsWithAll,
  boundLoading,
  boundPlaceholder,
  dateKindOptions,
}: Props) {
  return (
    <>
      {/* 업무구분 */}
      <div className="lcn">
        <span className="lcn__label">{t("jobDiv")}</span>
        <Controller
          control={control}
          name="jobDiv"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={jobDivOptionsWithAll}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
              disabled={jobDivLoading}
              placeholder={jobDivPlaceholder}
              style={{ gridColumn: "2 / span 2" }}
            />
          )}
        />
      </div>

      {/* 수출입 */}
      <div className="lcn">
        <span className="lcn__label">{t("bound")}</span>
        <Controller
          control={control}
          name="bound"
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={boundOptionsWithAll}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
              disabled={boundLoading}
              placeholder={boundPlaceholder}
              style={{ gridColumn: "2 / span 2" }}
            />
          )}
        />
      </div>

      {/* 일자 종류 + 범위 (ETD / ETA / 실적일자 / 서류일자 통합) */}
      <Controller
        control={control}
        name="dateKind"
        render={({ field: dateKindField }) => (
          <Controller
            control={control}
            name="dateFrom"
            render={({ field: fromField }) => (
              <Controller
                control={control}
                name="dateTo"
                render={({ field: toField }) => (
                  <DateRangeBox
                    labelOptions={dateKindOptions}
                    labelValue={dateKindField.value}
                    onLabelChange={dateKindField.onChange}
                    fromProps={{
                      name: fromField.name,
                      value: fromField.value ?? "",
                      onChange: fromField.onChange,
                      onBlur: fromField.onBlur,
                      placeholder: "From",
                    }}
                    toProps={{
                      name: toField.name,
                      value: toField.value ?? "",
                      onChange: toField.onChange,
                      onBlur: toField.onBlur,
                      placeholder: "To",
                    }}
                  />
                )}
              />
            )}
          />
        )}
      />
    </>
  );
}
