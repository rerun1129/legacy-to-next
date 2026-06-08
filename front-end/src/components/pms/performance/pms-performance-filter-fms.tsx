"use client";

import { Controller } from "react-hook-form";
import type { Control, UseFormReturn } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion, LabelOption } from "@/components/shared/inputs/_types";
import type { PmsPerformanceFilter } from "./pms-performance-filter-model";

interface AutocompleteState {
  onSearch: (q: string) => void;
  suggestions: CodeBoxSuggestion[];
  suggestionsLoading: boolean;
}

interface Props {
  control: Control<PmsPerformanceFilter>;
  register: UseFormReturn<PmsPerformanceFilter>["register"];
  setValue: UseFormReturn<PmsPerformanceFilter>["setValue"];
  watch: UseFormReturn<PmsPerformanceFilter>["watch"];
  t: (key: string) => string;
  jobDivOptionsWithAll: LabelOption[];
  jobDivLoading: boolean;
  jobDivPlaceholder: string | undefined;
  boundOptionsWithAll: LabelOption[];
  boundLoading: boolean;
  boundPlaceholder: string | undefined;
  dateKindOptions: LabelOption[];
  portKindOptions: LabelOption[];
  actualCustomer: AutocompleteState;
  settlePartner: AutocompleteState;
  carrier: AutocompleteState;
  port: AutocompleteState;
  salesMan: AutocompleteState;
  team: AutocompleteState;
  operator: AutocompleteState;
}

/** FMS 필드 그룹: 업무구분 / 수출입 / 일자범위 / B/L No / 거래처 / 운송사 / 항만 / 영업 */
export function PmsPerformanceFilterFms({
  control,
  register,
  setValue,
  watch,
  t,
  jobDivOptionsWithAll,
  jobDivLoading,
  jobDivPlaceholder,
  boundOptionsWithAll,
  boundLoading,
  boundPlaceholder,
  dateKindOptions,
  portKindOptions,
  actualCustomer,
  settlePartner,
  carrier,
  port,
  salesMan,
  team,
  operator,
}: Props) {
  // 종류 셀렉터 현재값 — watch가 구독 기반으로 재렌더 안전
  const portKind = watch("portKind");

  function handleSelectActualCustomer(item: CodeBoxSuggestion) {
    setValue("actualCustomerCode", item.code, { shouldDirty: true });
    setValue("actualCustomerName", item.name, { shouldDirty: true });
  }
  function handleSelectSettlePartner(item: CodeBoxSuggestion) {
    setValue("settlePartnerCode", item.code, { shouldDirty: true });
    setValue("settlePartnerName", item.name, { shouldDirty: true });
  }
  function handleSelectCarrier(item: CodeBoxSuggestion) {
    setValue("carrierCode", item.code, { shouldDirty: true });
    setValue("carrierName", item.name, { shouldDirty: true });
  }
  function handleSelectPort(item: CodeBoxSuggestion) {
    setValue("portCode", item.code, { shouldDirty: true });
    setValue("portName", item.name, { shouldDirty: true });
  }
  function handleSelectSalesMan(item: CodeBoxSuggestion) {
    setValue("salesManCode", item.code, { shouldDirty: true });
    setValue("salesManName", item.name, { shouldDirty: true });
  }
  function handleSelectTeam(item: CodeBoxSuggestion) {
    setValue("teamCode", item.code, { shouldDirty: true });
    setValue("teamName", item.name, { shouldDirty: true });
  }
  function handleSelectOperator(item: CodeBoxSuggestion) {
    setValue("operator", item.code, { shouldDirty: true });
    setValue("operatorName", item.name, { shouldDirty: true });
  }

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

      {/* House B/L No */}
      <div className="lcn">
        <span className="lcn__label">{t("hblNo")}</span>
        <input
          {...register("hblNo")}
          className="lcn__name"
          placeholder={t("hblNo")}
          style={{ gridColumn: "2 / span 2" }}
        />
      </div>

      {/* Master B/L No */}
      <div className="lcn">
        <span className="lcn__label">{t("mblNo")}</span>
        <input
          {...register("mblNo")}
          className="lcn__name"
          placeholder={t("mblNo")}
          style={{ gridColumn: "2 / span 2" }}
        />
      </div>

      {/* Actual Customer */}
      <CodeBox
        kind="lcn"
        label={t("actualCustomer")}
        codeProps={{ ...register("actualCustomerCode"), placeholder: t("codePlaceholder") }}
        nameProps={{ ...register("actualCustomerName"), readOnly: true }}
        suggestions={actualCustomer.suggestions}
        onSearch={actualCustomer.onSearch}
        onSelect={handleSelectActualCustomer}
        suggestionsLoading={actualCustomer.suggestionsLoading}
      />

      {/* Settle Partner */}
      <CodeBox
        kind="lcn"
        label={t("settlePartner")}
        codeProps={{ ...register("settlePartnerCode"), placeholder: t("codePlaceholder") }}
        nameProps={{ ...register("settlePartnerName"), readOnly: true }}
        suggestions={settlePartner.suggestions}
        onSearch={settlePartner.onSearch}
        onSelect={handleSelectSettlePartner}
        suggestionsLoading={settlePartner.suggestionsLoading}
      />

      {/* Carrier */}
      <CodeBox
        kind="lcn"
        label={t("carrier")}
        codeProps={{ ...register("carrierCode"), placeholder: t("codePlaceholder") }}
        nameProps={{ ...register("carrierName"), readOnly: true }}
        suggestions={carrier.suggestions}
        onSearch={carrier.onSearch}
        onSelect={handleSelectCarrier}
        suggestionsLoading={carrier.suggestionsLoading}
      />

      {/* 항만 종류 + 코드 */}
      <CodeBox
        kind="lcn"
        label={t("port")}
        labelOptions={portKindOptions}
        labelValue={portKind}
        onLabelChange={(v) => setValue("portKind", v as PmsPerformanceFilter["portKind"])}
        codeProps={{ ...register("portCode"), placeholder: t("codePlaceholder") }}
        nameProps={{ ...register("portName"), readOnly: true }}
        suggestions={port.suggestions}
        onSearch={port.onSearch}
        onSelect={handleSelectPort}
        suggestionsLoading={port.suggestionsLoading}
      />

      {/* 영업사원 */}
      <CodeBox
        kind="lcn"
        label={t("salesMan")}
        codeProps={{ ...register("salesManCode"), placeholder: t("codePlaceholder") }}
        nameProps={{ ...register("salesManName"), readOnly: true }}
        suggestions={salesMan.suggestions}
        onSearch={salesMan.onSearch}
        onSelect={handleSelectSalesMan}
        suggestionsLoading={salesMan.suggestionsLoading}
      />

      {/* 팀 */}
      <CodeBox
        kind="lcn"
        label={t("team")}
        codeProps={{ ...register("teamCode"), placeholder: t("teamPlaceholder") }}
        nameProps={{ ...register("teamName"), readOnly: true }}
        suggestions={team.suggestions}
        onSearch={team.onSearch}
        onSelect={handleSelectTeam}
        suggestionsLoading={team.suggestionsLoading}
      />

      {/* 담당자 */}
      <CodeBox
        kind="lcn"
        label={t("operator")}
        codeProps={{ ...register("operator"), placeholder: t("codePlaceholder") }}
        nameProps={{ ...register("operatorName"), readOnly: true }}
        suggestions={operator.suggestions}
        onSearch={operator.onSearch}
        onSelect={handleSelectOperator}
        suggestionsLoading={operator.suggestionsLoading}
      />

    </>
  );
}
