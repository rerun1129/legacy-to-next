"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import type { UserFilter } from "@/domain/user";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<UserFilter>;
}

const SCOPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
  { value: "DELETED", label: "Deleted" },
] as const;

export function UserListFilter({ form }: Props) {
  useListFilterSync(form, "/admin/user/list");
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">Username</span>
            <input
              className="box-panel"
              placeholder="Username"
              {...register("username")}
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
