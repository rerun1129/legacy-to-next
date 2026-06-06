"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import type { FreightLineIssueFilter } from "./use-freight-line-issue-filter-model";
import { useFreightLineIssueFilterModel } from "./use-freight-line-issue-filter-model";

interface Props {
  form: UseFormReturn<FreightLineIssueFilter>;
  scope: string;
}

export function FreightLineIssueListFilter({ form, scope }: Props) {
  const {
    t,
    register,
    setValue,
    issuedStatusOptions,
    jobDivOptionsWithAll,
    jobDivLoading,
    jobDivPlaceholder,
    boundOptionsWithAll,
    boundLoading,
    boundPlaceholder,
    customer,
  } = useFreightLineIssueFilterModel(form, scope);

  function handleSelectCustomer(item: CodeBoxSuggestion) {
    setValue("customerCode", item.code, { shouldDirty: true });
    setValue("customerName", item.name, { shouldDirty: true });
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          {/* 거래처 자동완성 */}
          <CodeBox
            kind="lcn"
            label={t("customer")}
            codeProps={{ ...register("customerCode"), placeholder: t("customerPlaceholder") }}
            nameProps={{ ...register("customerName"), readOnly: true }}
            suggestions={customer.suggestions}
            onSearch={customer.onSearch}
            onSelect={handleSelectCustomer}
            suggestionsLoading={customer.suggestionsLoading}
          />

          {/* 업무구분 */}
          <div className="lcn">
            <span className="lcn__label">{t("jobDiv")}</span>
            <Controller
              control={form.control}
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
              control={form.control}
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

          {/* 실적일자 범위 */}
          <Controller
            control={form.control}
            name="performanceDtFrom"
            render={({ field: fromField }) => (
              <Controller
                control={form.control}
                name="performanceDtTo"
                render={({ field: toField }) => (
                  <DateRangeBox
                    labelOptions={[{ value: "PERFORMANCE_DT", label: t("performanceDt") }]}
                    labelValue="PERFORMANCE_DT"
                    onLabelChange={() => undefined}
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

          {/* 발급상태 */}
          <div className="lcn">
            <span className="lcn__label">{t("issuedStatus")}</span>
            <Controller
              control={form.control}
              name="issuedStatus"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={issuedStatusOptions}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                  style={{ gridColumn: "2 / span 2" }}
                />
              )}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
