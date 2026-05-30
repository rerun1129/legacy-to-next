"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { LcnLabel } from "@/components/shared/inputs/lcn-label";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useSeaHouseEnums } from "@/lib/use-sea-house-enums";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import type { SeaHouseFilter } from "@/domain/sea-house";
import { usePathname } from "next/navigation";
import {
  DATE_KIND_OPTIONS,
  MASTER_BL_KIND_OPTIONS,
  PARTY_KIND_OPTIONS,
  PARTNER_KIND_OPTIONS,
  PORT_KIND_OPTIONS,
} from "./sea-house-list-filter-options";

interface Props {
  form: UseFormReturn<SeaHouseFilter>;
}

export function SeaHouseListFilter({ form }: Props) {
  const pathname = usePathname();
  useListFilterSync(form, pathname);
  const { register, setValue } = form;

  const { shipmentType, salesClass, incoterms, loadType } = useSeaHouseEnums();
  const shipmentTypeOptionsWithAll = [{ value: "", label: "ALL" }, ...shipmentType.options];
  const salesClassOptionsWithAll   = [{ value: "", label: "ALL" }, ...salesClass.options];
  const incotermsOptionsWithAll    = [{ value: "", label: "ALL" }, ...incoterms.options];
  const loadTypeOptionsWithAll     = [{ value: "", label: "ALL" }, ...loadType.options];

  // 자동완성 훅 — 소스별 1:1
  const party           = useCodeAutocomplete(CODE_SOURCES.customer);
  const actualCustomer  = useCodeAutocomplete(CODE_SOURCES.customer);
  const partner         = useCodeAutocomplete(CODE_SOURCES.partner);
  const liner           = useCodeAutocomplete(CODE_SOURCES.carrierSea);
  const port            = useCodeAutocomplete(CODE_SOURCES.portSea);
  const operator        = useCodeAutocomplete(CODE_SOURCES.user);
  const salesMan        = useCodeAutocomplete(CODE_SOURCES.user);

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

          {/* 2. Master B/L No / Master Reference No. */}
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

          {/* 3. House B/L No */}
          <div className="lcn">
            <span className="lcn__label">House B/L No</span>
            <input
              {...register("hblNo")}
              placeholder="House B/L No"
              className="lcn__name"
              style={{ gridColumn: "2 / span 2" }}
            />
          </div>

          {/* 4. Shipper/Consignee/Notify */}
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

          {/* 5. Actual Customer */}
          <CodeBox
            kind="lcn"
            label="Actual Customer"
            codeProps={{ ...register("actualCustomerCode"), placeholder: "Code" }}
            nameProps={{ ...register("actualCustomerName"), placeholder: "Name" }}
            onLookup={() => {}}
            onSearch={actualCustomer.onSearch}
            suggestions={actualCustomer.suggestions}
            suggestionsLoading={actualCustomer.suggestionsLoading}
            onSelect={(it) => {
              setValue("actualCustomerCode", it.code);
              setValue("actualCustomerName", it.name);
            }}
          />

          {/* 6. Settle Partner / Doc Partner */}
          <Controller
            control={form.control}
            name="partnerKind"
            render={({ field: kindField }) => (
              <CodeBox
                kind="lcn"
                labelOptions={PARTNER_KIND_OPTIONS}
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

          {/* 7. Liner */}
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

          {/* 8. POL/POD */}
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

          {/* 9. Vessel Name */}
          <div className="lcn">
            <span className="lcn__label">Vessel Name</span>
            <input
              {...register("vesselName")}
              placeholder="Vessel Name"
              className="lcn__name"
              style={{ gridColumn: "2 / span 2" }}
            />
          </div>

          {/* 10. Voyage */}
          <div className="lcn">
            <span className="lcn__label">Voyage</span>
            <input
              {...register("voyageNo")}
              placeholder="Voyage No"
              className="lcn__name"
              style={{ gridColumn: "2 / span 2" }}
            />
          </div>

          {/* 10. Shipment Type */}
          <div className="lcn">
            <span className="lcn__label">Shipment Type</span>
            <Controller
              control={form.control}
              name="shipmentType"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={shipmentTypeOptionsWithAll}
                  disabled={shipmentType.isLoading}
                  placeholder={shipmentType.placeholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>

          {/* 11. Team — autocomplete 미배선 (모달 방식 유지) */}
          <CodeBox
            kind="lcn"
            label="Team"
            codeProps={{ ...register("teamCode"), placeholder: "Code" }}
            nameProps={{ ...register("teamName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* 12. Operator */}
          <CodeBox
            kind="lcn"
            label="Operator"
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

          {/* 13. Sales Class */}
          <div className="lcn">
            <span className="lcn__label">Sales Class</span>
            <Controller
              control={form.control}
              name="salesClass"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={salesClassOptionsWithAll}
                  disabled={salesClass.isLoading}
                  placeholder={salesClass.placeholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>

          {/* 14. Sales Man */}
          <CodeBox
            kind="lcn"
            label="Sales Man"
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

          {/* 15. Incoterms */}
          <div className="lcn">
            <span className="lcn__label">Incoterms</span>
            <Controller
              control={form.control}
              name="incoterms"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={incotermsOptionsWithAll}
                  disabled={incoterms.isLoading}
                  placeholder={incoterms.placeholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>

          {/* 16. Load Type */}
          <div className="lcn">
            <span className="lcn__label">Load Type</span>
            <Controller
              control={form.control}
              name="loadType"
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={loadTypeOptionsWithAll}
                  disabled={loadType.isLoading}
                  placeholder={loadType.placeholder}
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
