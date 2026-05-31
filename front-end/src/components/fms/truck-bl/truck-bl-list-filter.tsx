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
import type { TruckBlFilter } from "@/domain/truck-bl";
import {
  DATE_KIND_OPTIONS,
  PARTY_KIND_OPTIONS,
  PARTNER_KIND_OPTIONS,
  PORT_KIND_OPTIONS,
} from "./truck-bl-list-filter-options";
import type { LabelOption } from "@/components/shared/inputs/_types";

interface Props {
  form: UseFormReturn<TruckBlFilter>;
}

export function TruckBlListFilter({ form }: Props) {
  useListFilterSync(form, "/fms/truck-bl/list");
  const t = useTranslations("fms.truckBl.list.filter");

  // labelKey 배열 → 해석된 LabelOption 배열 (useMemo로 t 참조 변경 시에만 재계산)
  const dateKindOptions = useMemo<LabelOption[]>(
    () => DATE_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const partyKindOptions = useMemo<LabelOption[]>(
    () => PARTY_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const partnerKindOptions = useMemo<LabelOption[]>(
    () => PARTNER_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
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

  // 자동완성 훅 — 소스별 1:1
  const party    = useCodeAutocomplete(CODE_SOURCES.customer);
  const partner  = useCodeAutocomplete(CODE_SOURCES.partner);
  const trucker  = useCodeAutocomplete(CODE_SOURCES.trucker);
  const port     = useCodeAutocomplete(CODE_SOURCES.port);
  const operator = useCodeAutocomplete(CODE_SOURCES.user);
  const team     = useCodeAutocomplete(CODE_SOURCES.team);

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          {/* 1. Bound */}
          <div className="lcn">
            <span className="lcn__label">{t("bound")}</span>
            <ComboBox
              variant="panel"
              options={boundOptionsWithAll}
              disabled={boundLoading}
              placeholder={boundPlaceholder}
              style={{ gridColumn: "2 / span 2" }}
              {...register("bound")}
            />
          </div>

          {/* 2. ETD/ETA */}
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
                        labelValue={kindField.value ?? "ETD"}
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

          {/* 3. Trucker */}
          <CodeBox
            kind="lcn"
            label={t("trucker")}
            codeProps={{ ...register("truckerCode"), placeholder: "Code" }}
            nameProps={{ ...register("truckerName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={trucker.onSearch}
            suggestions={trucker.suggestions}
            suggestionsLoading={trucker.suggestionsLoading}
            onSelect={(it) => {
              setValue("truckerCode", it.code);
              setValue("truckerName", it.name);
            }}
          />

          {/* 4. Truck B/L No */}
          <div className="lcn">
            <span className="lcn__label">{t("truckBlNo")}</span>
            <TextBox
              variant="panel"
              placeholder="Truck B/L No"
              style={{ gridColumn: "2 / span 2" }}
              {...register("truckBlNo")}
            />
          </div>

          {/* 5. Shipper/Consignee/Notify */}
          <Controller
            control={form.control}
            name="partyKind"
            render={({ field: kindField }) => (
              <CodeBox
                kind="lcn"
                labelOptions={partyKindOptions}
                labelValue={kindField.value ?? "SHIPPER"}
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

          {/* 6. POL/POD */}
          <Controller
            control={form.control}
            name="portKind"
            render={({ field: kindField }) => (
              <CodeBox
                kind="lcn"
                labelOptions={portKindOptions}
                labelValue={kindField.value ?? "POL"}
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

          {/* 7. Settle Partner / Doc Partner */}
          <Controller
            control={form.control}
            name="partnerKind"
            render={({ field: kindField }) => (
              <CodeBox
                kind="lcn"
                labelOptions={partnerKindOptions}
                labelValue={kindField.value ?? ""}
                onLabelChange={kindField.onChange}
                codeProps={{ ...register("partnerCode"), placeholder: "Code" }}
                nameProps={{ ...register("partnerName"), placeholder: "Name" }}
                onLookup={() => {}}
                onSearch={partner.onSearch}
                suggestions={partner.suggestions}
                suggestionsLoading={partner.suggestionsLoading}
                onSelect={(it) => {
                  setValue("partnerCode", it.code);
                  setValue("partnerName", it.name);
                }}
              />
            )}
          />

          {/* 8. Operator */}
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

          {/* 9. Team */}
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
