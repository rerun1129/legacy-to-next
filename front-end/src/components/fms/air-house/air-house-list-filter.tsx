"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { LcnLabel } from "@/components/shared/inputs/lcn-label";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { AirHouseFilter } from "@/domain/air-house";
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
  form: UseFormReturn<AirHouseFilter>;
}

export function AirHouseListFilter({ form }: Props) {
  const pathname = usePathname();
  useListFilterSync(form, pathname);
  const { register } = form;

  const { options: shipmentTypeOptions, isLoading: shipmentTypeLoading, placeholder: shipmentTypePlaceholder } = useEnumOptions("ShipmentType");
  const shipmentTypeOptionsWithAll = [{ value: "", label: "ALL" }, ...shipmentTypeOptions];

  const { options: salesClassOptions, isLoading: salesClassLoading, placeholder: salesClassPlaceholder } = useEnumOptions("SalesClass");
  const salesClassOptionsWithAll = [{ value: "", label: "ALL" }, ...salesClassOptions];

  const { options: incotermsOptions, isLoading: incotermsLoading, placeholder: incotermsPlateholder } = useEnumOptions("Incoterms");
  const incotermsOptionsWithAll = [{ value: "", label: "ALL" }, ...incotermsOptions];

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

          {/* 3. House AWB No */}
          <div className="lcn">
            <span className="lcn__label">House AWB No</span>
            <input
              {...register("hblNo")}
              placeholder="House AWB No"
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
          />

          {/* 6. Settle Partner */}
          <CodeBox
            kind="lcn"
            label="Settle Partner"
            codeProps={{ ...register("settlePartnerCode"), placeholder: "Code" }}
            nameProps={{ ...register("settlePartnerName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* 7. Airline */}
          <CodeBox
            kind="lcn"
            label="Airline"
            codeProps={{ ...register("airlineCode"), placeholder: "Code" }}
            nameProps={{ ...register("airlineName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* 8. Departure/Destination */}
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

          {/* 9. Shipment Type */}
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

          {/* 10. Team */}
          <CodeBox
            kind="lcn"
            label="Team"
            codeProps={{ ...register("teamCode"), placeholder: "Code" }}
            nameProps={{ ...register("teamName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* 11. Operator */}
          <CodeBox
            kind="lcn"
            label="Operator"
            codeProps={{ ...register("operatorCode"), placeholder: "Code" }}
            nameProps={{ ...register("operatorName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* 12. Sales Class */}
          <div className="lcn">
            <span className="lcn__label">Sales Class</span>
            <Controller
              control={form.control}
              name="salesClass"
              render={({ field }) => (
                <DropBox
                  variant="panel"
                  options={salesClassOptionsWithAll}
                  disabled={salesClassLoading}
                  placeholder={salesClassPlaceholder}
                  style={{ gridColumn: "2 / span 2" }}
                  value={field.value}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  name={field.name}
                />
              )}
            />
          </div>

          {/* 13. Sales Man */}
          <CodeBox
            kind="lcn"
            label="Sales Man"
            codeProps={{ ...register("salesManCode"), placeholder: "Code" }}
            nameProps={{ ...register("salesManName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* 14. Incoterms */}
          <div className="lcn">
            <span className="lcn__label">Incoterms</span>
            <Controller
              control={form.control}
              name="incoterms"
              render={({ field }) => (
                <DropBox
                  variant="panel"
                  options={incotermsOptionsWithAll}
                  disabled={incotermsLoading}
                  placeholder={incotermsPlateholder}
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
