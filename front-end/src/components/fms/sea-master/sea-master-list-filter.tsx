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
import type { SeaMasterFilter } from "@/domain/sea-master";
import { usePathname } from "next/navigation";
import {
  DATE_KIND_OPTIONS,
  MASTER_BL_KIND_OPTIONS,
  PARTY_KIND_OPTIONS,
  PORT_KIND_OPTIONS,
} from "./sea-master-list-filter-options";
import type { LabelOption } from "@/components/shared/inputs/_types";

interface Props {
  form: UseFormReturn<SeaMasterFilter>;
}

export function SeaMasterListFilter({ form }: Props) {
  const pathname = usePathname();
  useListFilterSync(form, pathname);
  const { register, setValue } = form;
  const t = useTranslations("fms.seaMaster.list.filter");

  // labelKey 배열 → 해석된 LabelOption 배열 (useMemo로 t 참조 변경 시에만 재계산)
  const dateKindOptions = useMemo<LabelOption[]>(
    () => DATE_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );
  const masterBlKindOptions = useMemo<LabelOption[]>(
    () => MASTER_BL_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
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
  const party = useCodeAutocomplete(CODE_SOURCES.customer);
  const liner = useCodeAutocomplete(CODE_SOURCES.carrierSea);
  const port  = useCodeAutocomplete(CODE_SOURCES.portSea);
  const team  = useCodeAutocomplete(CODE_SOURCES.team);

  const { options: shipmentTypeOptions, isLoading: shipmentTypeLoading, placeholder: shipmentTypePlaceholder } = useEnumOptions("ShipmentType");
  const allOption = useMemo(() => ({ value: "", label: t("all") }), [t]);
  const shipmentTypeOptionsWithAll = useMemo(
    () => [allOption, ...shipmentTypeOptions],
    [allOption, shipmentTypeOptions]
  );

  const { options: loadTypeOptions, isLoading: loadTypeLoading, placeholder: loadTypePlaceholder } = useEnumOptions("LoadType");
  const loadTypeOptionsWithAll = useMemo(
    () => [allOption, ...loadTypeOptions],
    [allOption, loadTypeOptions]
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

          {/* 2. Master B/L / Master Reference */}
          <Controller
            control={form.control}
            name="masterBlKind"
            render={({ field: kindField }) => (
              <div className="lcn">
                <LcnLabel
                  options={masterBlKindOptions}
                  value={kindField.value}
                  onChange={kindField.onChange}
                />
                <input
                  {...register("masterBlValue")}
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

          {/* 4. Liner */}
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

          {/* 5. POL/POD */}
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

          {/* 6. Vessel */}
          <div className="lcn">
            <span className="lcn__label">{t("vesselName")}</span>
            <input {...register("vesselName")} placeholder="Vessel Name" className="lcn__name" style={{ gridColumn: "2 / span 2" }} />
          </div>

          {/* 7. Voyage */}
          <div className="lcn">
            <span className="lcn__label">{t("voyageNo")}</span>
            <input {...register("voyageNo")} placeholder="Voyage No" className="lcn__name" style={{ gridColumn: "2 / span 2" }} />
          </div>

          {/* 8. Shipment Type */}
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

          {/* 9. Load Type */}
          <div className="lcn">
            <span className="lcn__label">{t("loadType")}</span>
            <Controller
              control={form.control}
              name="loadType"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={loadTypeOptionsWithAll}
                  disabled={loadTypeLoading}
                  placeholder={loadTypePlaceholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>

          {/* 10. Team */}
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
