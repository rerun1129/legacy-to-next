"use client";

import { useMemo } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { LcnLabel } from "@/components/shared/inputs/lcn-label";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useEnumOptions } from "@/application/enums/use-enum";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import type { AirMasterFilter } from "@/domain/air-master";
import { usePathname } from "next/navigation";
import {
  DATE_KIND_OPTIONS,
  MASTER_AWB_KIND_OPTIONS,
  PARTY_KIND_OPTIONS,
  PORT_KIND_OPTIONS,
} from "./air-master-list-filter-options";
import type { LabelOption } from "@/components/shared/inputs/_types";

interface Props {
  form: UseFormReturn<AirMasterFilter>;
}

export function AirMasterListFilter({ form }: Props) {
  const pathname = usePathname();
  useListFilterSync(form, pathname);
  const { register, setValue } = form;
  const t = useTranslations("fms.airMaster.list.filter");

  // labelKey 배열 → 해석된 LabelOption 배열 (useMemo로 t 참조 변경 시에만 재계산)
  const dateKindOptions = useMemo<LabelOption[]>(
    () => DATE_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const masterAwbKindOptions = useMemo<LabelOption[]>(
    () => MASTER_AWB_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
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

  // 자동완성 훅 — 소스별 1:1
  const party   = useCodeAutocomplete(CODE_SOURCES.customer);
  const airline = useCodeAutocomplete(CODE_SOURCES.carrierAir);
  const port    = useCodeAutocomplete(CODE_SOURCES.portAir);
  const team    = useCodeAutocomplete(CODE_SOURCES.team);

  const { options: shipmentTypeOptions, isLoading: shipmentTypeLoading, placeholder: shipmentTypePlaceholder } = useEnumOptions("ShipmentType");
  const allOption = useMemo(() => ({ value: "", label: t("all") }), [t]);
  const shipmentTypeOptionsWithAll = useMemo(
    () => [allOption, ...shipmentTypeOptions],
    [allOption, shipmentTypeOptions]
  );

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          {/* 1. ETD/ETA */}
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

          {/* 2. Master AWB / Master Reference */}
          <Controller
            control={form.control}
            name="masterAwbKind"
            render={({ field: kindField }) => (
              <div className="lcn">
                <LcnLabel
                  options={masterAwbKindOptions}
                  value={kindField.value}
                  onChange={kindField.onChange}
                />
                <input
                  {...register("masterAwbValue")}
                  placeholder="No"
                  className="lcn__name"
                />
              </div>
            )}
          />

          {/* 3. Shipper/Consignee/Notify */}
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

          {/* 4. Airline */}
          <CodeBox
            kind="lcn"
            label={t("airline")}
            codeProps={{ ...register("airlineCode"), placeholder: "Code" }}
            nameProps={{ ...register("airlineName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={airline.onSearch}
            suggestions={airline.suggestions}
            suggestionsLoading={airline.suggestionsLoading}
            onSelect={(it) => {
              setValue("airlineCode", it.code);
              setValue("airlineName", it.name);
            }}
          />

          {/* 5. Departure/Destination */}
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

          {/* 6. Shipment Type */}
          <div className="lcn">
            <span className="lcn__label">{t("shipmentType")}</span>
            <Controller
              control={form.control}
              name="shipmentType"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={shipmentTypeOptionsWithAll}
                  disabled={shipmentTypeLoading}
                  placeholder={shipmentTypePlaceholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>

          {/* 7. Team */}
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
