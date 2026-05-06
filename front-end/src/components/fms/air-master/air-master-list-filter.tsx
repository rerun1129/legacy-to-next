"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { LcnLabel } from "@/components/shared/inputs/lcn-label";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { AirMasterFilter } from "@/domain/air-master";
import { usePathname } from "next/navigation";

const DATE_KIND_OPTIONS = [
  { value: "ETD", label: "ETD" },
  { value: "ETA", label: "ETA" },
];
const MASTER_AWB_KIND_OPTIONS = [
  { value: "MBL", label: "Master AWB No" },
  { value: "REF", label: "Master Ref. No" },
];
const PARTY_KIND_OPTIONS = [
  { value: "SHIPPER", label: "Shipper" },
  { value: "CONSIGNEE", label: "Consignee" },
  { value: "NOTIFY", label: "Notify" },
];
const PORT_KIND_OPTIONS = [
  { value: "POL", label: "Departure" },
  { value: "POD", label: "Destination" },
];

interface Props {
  form: UseFormReturn<AirMasterFilter>;
}

export function AirMasterListFilter({ form }: Props) {
  const pathname = usePathname();
  useListFilterSync(form, pathname);
  const { register } = form;

  const { options: shipmentTypeOptions, isLoading: shipmentTypeLoading, placeholder: shipmentTypePlaceholder } = useEnumOptions("ShipmentType");
  const shipmentTypeOptionsWithAll = [{ value: "", label: "ALL" }, ...shipmentTypeOptions];

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

          {/* 2. Master AWB / Master Reference */}
          <Controller
            control={form.control}
            name="masterAwbKind"
            render={({ field: kindField }) => (
              <div className="lcn">
                <LcnLabel
                  options={MASTER_AWB_KIND_OPTIONS}
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
                labelOptions={PARTY_KIND_OPTIONS}
                labelValue={kindField.value}
                onLabelChange={kindField.onChange}
                codeProps={{ ...register("partyCode"), placeholder: "Code" }}
                nameProps={{ ...register("partyName"), placeholder: "Name" }}
                onLookup={() => {}}
              />
            )}
          />

          {/* 4. Airline */}
          <CodeBox
            kind="lcn"
            label="Airline"
            codeProps={{ ...register("airlineCode"), placeholder: "Code" }}
            nameProps={{ ...register("airlineName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* 6. Departure/Destination */}
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
              />
            )}
          />

          {/* 7. Shipment Type */}
          <div className="lcn">
            <span className="lcn__label">Shipment Type</span>
            <Controller
              control={form.control}
              name="shipmentType"
              render={({ field }) => (
                <DropBox
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

          {/* 8. Team */}
          <CodeBox
            kind="lcn"
            label="Team"
            codeProps={{ ...register("teamCode"), placeholder: "Code" }}
            nameProps={{ ...register("teamName"), placeholder: "Name" }}
            onLookup={() => {}}
          />
        </div>
      </div>
    </div>
  );
}
