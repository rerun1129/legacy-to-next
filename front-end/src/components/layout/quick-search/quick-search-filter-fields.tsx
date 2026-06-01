"use client";

import { useMemo } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { useEnumOptions } from "@/application/enums/use-enum";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import type { BlQuickSearchFilters } from "@/domain/bl-quick-search";
import type { LabelOption } from "@/components/shared/inputs/_types";
import { DATE_KIND_OPTIONS, PARTY_KIND_OPTIONS } from "./quick-search-options";

interface Props {
  form: UseFormReturn<BlQuickSearchFilters>;
}

export function QuickSearchFilterFields({ form }: Props) {
  const { register, setValue } = form;
  const t = useTranslations("shell.quickSearch");

  const dateKindOptions = useMemo<LabelOption[]>(
    () => DATE_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const partyKindOptions = useMemo<LabelOption[]>(
    () => PARTY_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );

  const allOption = useMemo(() => ({ value: "", label: t("all") }), [t]);

  // 업무구분 (JobDiv)
  const { options: jobDivOptions, isLoading: jobDivLoading, placeholder: jobDivPlaceholder } =
    useEnumOptions("housebl.JobDiv");
  const jobDivOptionsWithAll = useMemo(
    () => [allOption, ...jobDivOptions],
    [allOption, jobDivOptions]
  );

  // 수출입 (Bound)
  const { options: boundOptions, isLoading: boundLoading, placeholder: boundPlaceholder } =
    useEnumOptions("Bound");
  const boundOptionsWithAll = useMemo(
    () => [allOption, ...boundOptions],
    [allOption, boundOptions]
  );

  // 자동완성 훅 — 소스별 1:1 인스턴스 분리
  const team     = useCodeAutocomplete(CODE_SOURCES.team);
  const operator = useCodeAutocomplete(CODE_SOURCES.user);
  const salesMan = useCodeAutocomplete(CODE_SOURCES.user);
  const pol      = useCodeAutocomplete(CODE_SOURCES.port);
  const pod      = useCodeAutocomplete(CODE_SOURCES.port);
  const party    = useCodeAutocomplete(CODE_SOURCES.customer);

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">

          {/* 1. 업무구분 */}
          <div className="lcn">
            <span className="lcn__label">{t("jobDiv")}</span>
            <Controller
              control={form.control}
              name="jobDiv"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={jobDivOptionsWithAll}
                  disabled={jobDivLoading}
                  placeholder={jobDivPlaceholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value ?? ""}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>

          {/* 2. 수출입 */}
          <div className="lcn">
            <span className="lcn__label">{t("bound")}</span>
            <Controller
              control={form.control}
              name="bound"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={boundOptionsWithAll}
                  disabled={boundLoading}
                  placeholder={boundPlaceholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value ?? ""}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>

          {/* 3. ETD/ETA 날짜 범위 */}
          <Controller
            control={form.control}
            name="dateKind"
            render={({ field: kindField }) => (
              <Controller
                control={form.control}
                name="dateFrom"
                render={({ field: fromField }) => (
                  <Controller
                    control={form.control}
                    name="dateTo"
                    render={({ field: toField }) => (
                      <DateRangeBox
                        labelOptions={dateKindOptions}
                        labelValue={kindField.value}
                        onLabelChange={kindField.onChange}
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

          {/* 4. Team */}
          <CodeBox
            kind="lcn"
            label={t("team")}
            codeProps={{ ...register("teamCode"), placeholder: "Code" }}
            nameProps={{ ...register("teamName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={team.onSearch}
            suggestions={team.suggestions}
            suggestionsLoading={team.suggestionsLoading}
            onSelect={(it) => {
              setValue("teamCode", it.code);
              setValue("teamName", it.name);
            }}
          />

          {/* 5. Operator */}
          <CodeBox
            kind="lcn"
            label={t("operator")}
            codeProps={{ ...register("operatorCode"), placeholder: "Code" }}
            nameProps={{ ...register("operatorName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={operator.onSearch}
            suggestions={operator.suggestions}
            suggestionsLoading={operator.suggestionsLoading}
            onSelect={(it) => {
              setValue("operatorCode", it.code);
              setValue("operatorName", it.name);
            }}
          />

          {/* 6. SalesMan */}
          <CodeBox
            kind="lcn"
            label={t("salesMan")}
            codeProps={{ ...register("salesManCode"), placeholder: "Code" }}
            nameProps={{ ...register("salesManName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={salesMan.onSearch}
            suggestions={salesMan.suggestions}
            suggestionsLoading={salesMan.suggestionsLoading}
            onSelect={(it) => {
              setValue("salesManCode", it.code);
              setValue("salesManName", it.name);
            }}
          />

          {/* 7. POL — POD와 독립 필드 */}
          <CodeBox
            kind="lcn"
            label={t("pol")}
            codeProps={{ ...register("polCode"), placeholder: "Code" }}
            nameProps={{ ...register("polName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={pol.onSearch}
            suggestions={pol.suggestions}
            suggestionsLoading={pol.suggestionsLoading}
            onSelect={(it) => {
              setValue("polCode", it.code);
              setValue("polName", it.name);
            }}
          />

          {/* 8. POD — POL과 독립 필드 */}
          <CodeBox
            kind="lcn"
            label={t("pod")}
            codeProps={{ ...register("podCode"), placeholder: "Code" }}
            nameProps={{ ...register("podName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={pod.onSearch}
            suggestions={pod.suggestions}
            suggestionsLoading={pod.suggestionsLoading}
            onSelect={(it) => {
              setValue("podCode", it.code);
              setValue("podName", it.name);
            }}
          />

          {/* 9. Party — partyKind LabelOption 토글 */}
          <Controller
            control={form.control}
            name="partyKind"
            render={({ field: kindField }) => (
              <CodeBox
                kind="lcn"
                labelOptions={partyKindOptions}
                labelValue={kindField.value ?? ""}
                onLabelChange={kindField.onChange}
                codeProps={{ ...register("partyCode"), placeholder: "Code" }}
                nameProps={{ ...register("partyName"), placeholder: "Name" }}
                onLookup={() => {}}
                onSearch={party.onSearch}
                suggestions={party.suggestions}
                suggestionsLoading={party.suggestionsLoading}
                onSelect={(it) => {
                  setValue("partyCode", it.code);
                  setValue("partyName", it.name);
                }}
              />
            )}
          />

        </div>
      </div>
    </div>
  );
}
