"use client";

import { Controller } from "react-hook-form";
import type { Control } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import type { LabelOption } from "@/components/shared/inputs/_types";
import type { FinancialDocumentFilter } from "./use-financial-document-list-filter-model";

interface Props {
  control: Control<FinancialDocumentFilter>;
  t: (key: string) => string;
  dateKindOptions: LabelOption[];
  documentStatusOptions: LabelOption[];
  jobDivOptionsWithAll: LabelOption[];
  jobDivLoading: boolean;
  jobDivPlaceholder: string | undefined;
  boundOptionsWithAll: LabelOption[];
  boundLoading: boolean;
  boundPlaceholder: string | undefined;
}

/** 기본 필터 필드: 업무구분 / 수출입 / 일자종류+범위 / Status / 서류번호 */
export function FinancialDocumentFilterBasicFields({
  control,
  t,
  dateKindOptions,
  documentStatusOptions,
  jobDivOptionsWithAll,
  jobDivLoading,
  jobDivPlaceholder,
  boundOptionsWithAll,
  boundLoading,
  boundPlaceholder,
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

      {/* 일자종류 + 범위 */}
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

      {/* 서류 Status */}
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
        <Controller
          control={control}
          name="documentNoLike"
          render={({ field }) => (
            <input
              {...field}
              className="lcn__name"
              placeholder={t("documentNoLike")}
              style={{ gridColumn: "2 / span 2" }}
            />
          )}
        />
      </div>
    </>
  );
}
