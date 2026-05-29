"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { accessMenuUseCases } from "@/application/access/menu/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export type MenuStatus = "ALL" | "ACTIVE" | "INACTIVE";

export interface MenuFilter {
  moduleCode: string;
  menuCode: string;
  status: MenuStatus;
}

export const DEFAULT_MENU_FILTER: MenuFilter = {
  moduleCode: "",
  menuCode: "",
  status: "ALL",
};

const STATUS_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
] as const;

interface Props {
  form: UseFormReturn<MenuFilter>;
  moduleOptions: { value: string; label: string }[];
}

export function MenuListFilter({ form, moduleOptions }: Props) {
  const { register, setValue, control } = form;

  const allModuleOptions = [{ value: "", label: "All" }, ...moduleOptions];

  const [acQuery, setAcQuery] = useState("");

  const { data: suggestions = [] } = useQuery({
    queryKey: ["access-menu", "autocomplete", acQuery],
    queryFn: () => accessMenuUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
    select: (items): CodeBoxSuggestion[] =>
      items.map((i) => ({ code: i.code, name: i.name })),
  });

  function handleSelect(item: CodeBoxSuggestion) {
    setValue("menuCode", item.code);
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <div className="lcn">
            <span className="lcn__label">Module</span>
            <Controller
              name="moduleCode"
              control={control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={allModuleOptions}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
          <CodeBox
            kind="code-only"
            label="Menu Code"
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: "Menu Code", ...register("menuCode") }}
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
