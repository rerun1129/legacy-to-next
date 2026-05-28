"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export type PermissionPresetStatus = "ALL" | "ACTIVE" | "INACTIVE";

export interface PermissionPresetFilter {
  code: string;
  status: PermissionPresetStatus;
}

export const DEFAULT_PERMISSION_PRESET_FILTER: PermissionPresetFilter = {
  code: "",
  status: "ALL",
};

const STATUS_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
] as const;

interface Props {
  form: UseFormReturn<PermissionPresetFilter>;
  suggestions: CodeBoxSuggestion[];
  onCodeSearch: (query: string) => void;
  onCodeSelect: (item: CodeBoxSuggestion) => void;
}

export function PermissionPresetListFilter({
  form,
  suggestions,
  onCodeSearch,
  onCodeSelect,
}: Props) {
  const { register, control } = form;

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label="Code"
            onSearch={onCodeSearch}
            suggestions={suggestions}
            onSelect={onCodeSelect}
            codeProps={{ placeholder: "Code", ...register("code") }}
          />
          <div className="lcn">
            <span className="lcn__label">Status</span>
            <Controller
              name="status"
              control={control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...STATUS_OPTIONS]}
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
