"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import type { CustomerFilter } from "@/domain/customer";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<CustomerFilter>;
}

const CUSTOMER_TYPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "FORWARDER", label: "FORWARDER" },
  { value: "SHIPPER", label: "SHIPPER" },
  { value: "CONSIGNEE", label: "CONSIGNEE" },
  { value: "CARRIER", label: "CARRIER" },
  { value: "AGENT", label: "AGENT" },
  { value: "CUSTOMS_BROKER", label: "CUSTOMS_BROKER" },
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
          <CodeBox kind="code-only" label="Customer Code" onLookup={() => {}} codeProps={{ placeholder: "Customer Code", ...register("customerCode") }} />
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
