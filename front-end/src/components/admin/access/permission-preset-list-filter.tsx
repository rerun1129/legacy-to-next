"use client";

import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
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
  const t = useTranslations("admin.permissionPreset.filter");
  const { register, control } = form;

  const statusOptions = [
    { value: "ALL",      label: t("all")      },
    { value: "ACTIVE",   label: t("active")   },
    { value: "INACTIVE", label: t("inactive") },
  ];

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label={t("code")}
            onSearch={onCodeSearch}
            suggestions={suggestions}
            onSelect={onCodeSelect}
            codeProps={{ placeholder: t("codePlaceholder"), ...register("code") }}
          />
          <div className="lcn">
            <span className="lcn__label">{t("status")}</span>
            <Controller
              name="status"
              control={control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={statusOptions}
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
