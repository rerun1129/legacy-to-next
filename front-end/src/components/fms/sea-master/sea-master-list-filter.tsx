"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
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

const DATE_KIND_OPTIONS = [
  { value: "ETD", label: "ETD" },
  { value: "ETA", label: "ETA" },
];
const MASTER_BL_KIND_OPTIONS = [
  { value: "MBL", label: "Master B/L No" },
  { value: "REF", label: "Master Ref. No" },
];
const PARTY_KIND_OPTIONS = [
  { value: "SHIPPER", label: "Shipper" },
  { value: "CONSIGNEE", label: "Consignee" },
  { value: "NOTIFY", label: "Notify" },
];
const PORT_KIND_OPTIONS = [
  { value: "POL", label: "POL" },
  { value: "POD", label: "POD" },
];

interface Props {
  form: UseFormReturn<SeaMasterFilter>;
}

export function SeaMasterListFilter({ form }: Props) {
  const pathname = usePathname();
  useListFilterSync(form, pathname);
  const { register, setValue } = form;

  // 자동완성 훅 — 소스별 1:1
  const party = useCodeAutocomplete(CODE_SOURCES.customer);
  const liner = useCodeAutocomplete(CODE_SOURCES.carrierSea);
  const port  = useCodeAutocomplete(CODE_SOURCES.portSea);

  const { options: shipmentTypeOptions, isLoading: shipmentTypeLoading, placeholder: shipmentTypePlaceholder } = useEnumOptions("ShipmentType");
  const shipmentTypeOptionsWithAll = [{ value: "", label: "ALL" }, ...shipmentTypeOptions];

  const { options: loadTypeOptions, isLoading: loadTypeLoading, placeholder: loadTypePlaceholder } = useEnumOptions("LoadType");
  const loadTypeOptionsWithAll = [{ value: "", label: "ALL" }, ...loadTypeOptions];

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
                        labelOptions={DATE_KIND_OPTIONS}
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
                  options={MASTER_BL_KIND_OPTIONS}
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
                labelOptions={PARTY_KIND_OPTIONS}
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
            label="Liner"
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

          {/* 5. Departure/Destination */}
          <Controller
            control={form.control}
            name="portKind"
            render={({ field: kindField }) => (
              <CodeBox
                kind="lcn"
                labelOptions={PORT_KIND_OPTIONS}
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
            <span className="lcn__label">Vessel</span>
            <input {...register("vesselName")} placeholder="Vessel Name" className="lcn__name" style={{ gridColumn: "2 / span 2" }} />
          </div>

          {/* 7. Voyage */}
          <div className="lcn">
            <span className="lcn__label">Voyage</span>
            <input {...register("voyageNo")} placeholder="Voyage No" className="lcn__name" style={{ gridColumn: "2 / span 2" }} />
          </div>

          {/* 8. Shipment Type */}
          <div className="lcn">
            <span className="lcn__label">Shipment Type</span>
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
            <span className="lcn__label">Load Type</span>
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
        </div>
      </div>
    </div>
  );
}
