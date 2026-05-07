"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { LcnLabel } from "@/components/shared/inputs/lcn-label";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { SeaHouseFilter } from "@/domain/sea-house";
import { usePathname } from "next/navigation";

const DATE_KIND_OPTIONS = [
  { value: "ETD", label: "ETD" },
  { value: "ETA", label: "ETA" },
];
const MASTER_BL_KIND_OPTIONS = [
  { value: "MBL", label: "Master B/L No" },
  { value: "REF", label: "Master Reference No." },
];
const PARTY_KIND_OPTIONS = [
  { value: "SHIPPER", label: "Shipper" },
  { value: "CONSIGNEE", label: "Consignee" },
  { value: "NOTIFY", label: "Notify" },
];
const PARTNER_KIND_OPTIONS = [
  { value: "SETTLE_PARTNER", label: "Settle Partner" },
  { value: "DOC_PARTNER", label: "Doc Partner" },
];
const PORT_KIND_OPTIONS = [
  { value: "POL", label: "POL" },
  { value: "POD", label: "POD" },
];

interface Props {
  form: UseFormReturn<SeaHouseFilter>;
}

export function SeaHouseListFilter({ form }: Props) {
  const pathname = usePathname();
  useListFilterSync(form, pathname);
  const { register } = form;

  const { options: shipmentTypeOptions, isLoading: shipmentTypeLoading, placeholder: shipmentTypePlaceholder } = useEnumOptions("ShipmentType");
  const shipmentTypeOptionsWithAll = [{ value: "", label: "ALL" }, ...shipmentTypeOptions];

  const { options: salesClassOptions, isLoading: salesClassLoading, placeholder: salesClassPlaceholder } = useEnumOptions("SalesClass");
  const salesClassOptionsWithAll = [{ value: "", label: "ALL" }, ...salesClassOptions];

  const { options: incotermsOptions, isLoading: incotermsLoading, placeholder: incotermsPlateholder } = useEnumOptions("Incoterms");
  const incotermsOptionsWithAll = [{ value: "", label: "ALL" }, ...incotermsOptions];

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

          {/* 11. Team */}
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
          />

          {/* 13. Sales Class */}
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

          {/* 14. Sales Man */}
          <CodeBox
            kind="lcn"
            label="Sales Man"
            codeProps={{ ...register("salesManCode"), placeholder: "Code" }}
            nameProps={{ ...register("salesManName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* 15. Incoterms */}
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

          {/* 16. Load Type */}
          <div className="lcn">
            <span className="lcn__label">Load Type</span>
            <Controller
              control={form.control}
              name="loadType"
              render={({ field }) => (
                <DropBox
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
