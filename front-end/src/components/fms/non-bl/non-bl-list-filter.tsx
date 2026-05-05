"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { TextBox } from "@/components/shared/inputs/text-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { NonBlFilter } from "@/domain/non-bl";

const DATE_KIND_OPTIONS = [{ value: 'ETD', label: 'ETD' }, { value: 'ETA', label: 'ETA' }]
const PARTY_KIND_OPTIONS = [
  { value: 'SHIPPER', label: 'Shipper' },
  { value: 'CONSIGNEE', label: 'Consignee' },
  { value: 'NOTIFY', label: 'Notify' },
  { value: 'SETTLE_PARTNER', label: 'Settle Partner' },
]
const PORT_KIND_OPTIONS = [{ value: 'POL', label: 'POL' }, { value: 'POD', label: 'POD' }]

interface Props {
  form: UseFormReturn<NonBlFilter>;
}

export function NonBlListFilter({ form }: Props) {
  useListFilterSync(form, "/fms/non-bl/list");
  const { options: boundOptions, isLoading: boundLoading, placeholder: boundPlaceholder } = useEnumOptions("Bound");
  const boundOptionsWithAll = [{ value: "", label: "ALL" }, ...boundOptions];
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          {/* Row 1 */}
          <div className="lcn">
            <span className="lcn__label">Bound</span>
            <DropBox
              variant="panel"
              options={boundOptionsWithAll}
              disabled={boundLoading}
              placeholder={boundPlaceholder}
              style={{ gridColumn: "2 / span 2" }}
              {...register("bound")}
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

          <CodeBox
            kind="lcn"
            label="Liner"
            codeProps={{ ...register("linerCode"), placeholder: "Code" }}
            nameProps={{ ...register("linerName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          <div className="lcn">
            <span className="lcn__label">Non B/L No</span>
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
                labelOptions={PARTY_KIND_OPTIONS}
                labelValue={kindField.value}
                onLabelChange={kindField.onChange}
                codeProps={{ ...register("partyCode"), placeholder: "Code" }}
                nameProps={{ ...register("partyName"), placeholder: "Name" }}
                onLookup={() => {}}
              />
            )}
          />

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

          <div className="lcn">
            <span className="lcn__label">Vessel/Voyage</span>
            <TextBox variant="panel" placeholder="Vessel" {...register("vessel")} />
            <TextBox variant="panel" placeholder="Voyage" {...register("voyage")} />
          </div>

          <CodeBox
            kind="lcn"
            label="Operator"
            codeProps={{ ...register("operatorCode"), placeholder: "Code" }}
            nameProps={{ ...register("operatorName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* Row 3 */}
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
