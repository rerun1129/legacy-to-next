"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import type { CodeMasterFilter } from "@/domain/code-master";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";

interface Props {
  form: UseFormReturn<CodeMasterFilter>;
}

const ACTIVE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
] as const;

export function CodeMasterListFilter({ form }: Props) {
  const { register } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox kind="code-only" label="Master Code" onLookup={() => {}} codeProps={{ placeholder: "Master Code", ...register("masterCode") }} />
          <div className="lcn">
            <span className="lcn__label">Master Name</span>
            <input
              className="box-panel"
              placeholder="Master Name"
              {...register("masterName")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Status</span>
            <Controller
              name="active"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...ACTIVE_OPTIONS]}
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
