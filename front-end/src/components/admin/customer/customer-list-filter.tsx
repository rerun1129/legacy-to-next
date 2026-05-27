"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import type { CustomerFilter } from "@/domain/customer";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<CustomerFilter>;
}

const CUSTOMER_TYPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "CUSTOMER", label: "CUSTOMER" },
  { value: "PARTNER", label: "PARTNER" },
  { value: "AIRCARRIER", label: "AIRCARRIER" },
  { value: "LINER", label: "LINER" },
  { value: "TRUCKER", label: "TRUCKER" },
  { value: "WAREHOUSE", label: "WAREHOUSE" },
  { value: "OTHER", label: "OTHER" },
] as const;

const SCOPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
  { value: "DELETED", label: "Deleted" },
] as const;

export function CustomerListFilter({ form }: Props) {
  useListFilterSync(form, "/admin/customer/list");
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">Customer Code</span>
            <input
              className="box-panel"
              placeholder="Customer Code"
              {...register("customerCode")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Customer Name</span>
            <input
              className="box-panel"
              placeholder="Customer Name (partial)"
              {...register("name")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Type</span>
            <Controller
              name="customerType"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...CUSTOMER_TYPE_OPTIONS]}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
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
