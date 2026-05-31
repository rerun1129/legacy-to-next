"use client";

import { useMemo } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { TextBox } from "@/components/shared/inputs/text-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useEnumOptions } from "@/application/enums/use-enum";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import type { NonBlFilter } from "@/domain/non-bl";
import {
  DATE_KIND_OPTIONS,
  PARTY_KIND_OPTIONS,
  PORT_KIND_OPTIONS,
} from "./non-bl-list-filter-options";
import type { LabelOption } from "@/components/shared/inputs/_types";

interface Props {
  form: UseFormReturn<NonBlFilter>;
}

export function NonBlListFilter({ form }: Props) {
  useListFilterSync(form, "/fms/non-bl/list");
  const t = useTranslations("fms.nonBl.list.filter");

  // labelKey 배열 → 해석된 LabelOption 배열 (useMemo로 t 참조 변경 시에만 재계산)
  const dateKindOptions = useMemo<LabelOption[]>(
    () => DATE_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const partyKindOptions = useMemo<LabelOption[]>(
    () => PARTY_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const portKindOptions = useMemo<LabelOption[]>(
    () => PORT_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );

  const { options: boundOptions, isLoading: boundLoading, placeholder: boundPlaceholder } = useEnumOptions("Bound");
  const allOption = useMemo(() => ({ value: "", label: t("all") }), [t]);
  const boundOptionsWithAll = useMemo(
    () => [allOption, ...boundOptions],
    [allOption, boundOptions]
  );

  const { register, setValue } = form;

  // party 토글이 SETTLE_PARTNER이면 partner 소스, 그 외(Shipper/Consignee/Notify)는 customer 소스
  const partyKind = form.watch("partyKind");
  const party    = useCodeAutocomplete(partyKind === "SETTLE_PARTNER" ? CODE_SOURCES.partner : CODE_SOURCES.customer);
  const liner    = useCodeAutocomplete(CODE_SOURCES.carrier);
  const port     = useCodeAutocomplete(CODE_SOURCES.port);
  const operator = useCodeAutocomplete(CODE_SOURCES.user);
  const team     = useCodeAutocomplete(CODE_SOURCES.team);

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          {/* Row 1 */}
          <div className="lcn">
            <span className="lcn__label">{t("bound")}</span>
            <Controller
              name="bound"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={boundOptionsWithAll}
                  disabled={boundLoading}
                  placeholder={boundPlaceholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>

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
                        required
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

          <CodeBox
            kind="lcn"
            label={t("liner")}
            codeProps={{ ...register("linerCode"), placeholder: "Code" }}
            nameProps={{ ...register("linerName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={liner.onSearch}
            suggestions={liner.suggestions}
            suggestionsLoading={liner.suggestionsLoading}
            onSelect={(it) => {
              setValue("linerCode", it.code);
              setValue("linerName", it.name);
            }}
          />

          <div className="lcn">
            <span className="lcn__label">{t("nonBlNo")}</span>
            <TextBox
              variant="panel"
              placeholder="Non B/L No"
              style={{ gridColumn: "2 / span 2" }}
              {...register("nonBlNo")}
            />
          </div>

          {/* Row 2 */}
          <Controller
            control={form.control}
            name="partyKind"
            render={({ field: kindField }) => (
              <CodeBox
                kind="lcn"
                labelOptions={partyKindOptions}
                labelValue={kindField.value}
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

          <Controller
            control={form.control}
            name="portKind"
            render={({ field: kindField }) => (
              <CodeBox
                kind="lcn"
                labelOptions={portKindOptions}
                labelValue={kindField.value}
                onLabelChange={kindField.onChange}
                codeProps={{ ...register("portCode"), placeholder: "Code" }}
                nameProps={{ ...register("portName"), placeholder: "Name" }}
                onLookup={() => {}}
                onSearch={port.onSearch}
                suggestions={port.suggestions}
                suggestionsLoading={port.suggestionsLoading}
                onSelect={(it) => {
                  setValue("portCode", it.code);
                  setValue("portName", it.name);
                }}
              />
            )}
          />

          <div className="lcn">
            <span className="lcn__label">{t("vesselVoyage")}</span>
            <TextBox variant="panel" placeholder="Vessel" {...register("vessel")} />
            <TextBox variant="panel" placeholder="Voyage" {...register("voyage")} />
          </div>

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

          {/* Row 3 */}
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
        </div>
      </div>
    </div>
  );
}
