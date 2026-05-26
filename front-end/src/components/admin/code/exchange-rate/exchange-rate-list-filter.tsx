"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import type { ExchangeRateFilter } from "@/domain/code/exchange-rate";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<ExchangeRateFilter>;
}

const SCOPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
  { value: "DELETED", label: "Deleted" },
] as const;

export function ExchangeRateListFilter({ form }: Props) {
  useListFilterSync(form, "/admin/code/exchange-rate/list");
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">Base Currency</span>
            <input
              className="box-panel"
              placeholder="e.g. USD"
              {...register("baseCurrency")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Target Currency</span>
            <input
              className="box-panel"
              placeholder="e.g. KRW"
              {...register("targetCurrency")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Name</span>
            <input
              className="box-panel"
              placeholder="Name (partial)"
              {...register("name")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Status</span>
            <Controller
              name="scope"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...SCOPE_OPTIONS]}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
