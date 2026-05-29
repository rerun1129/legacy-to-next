"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { accessButtonUseCases } from "@/application/access/button/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export type ButtonStatus = "ALL" | "ACTIVE" | "INACTIVE";

export interface ButtonFilter {
  moduleCode: string;
  buttonCode: string;
  status: ButtonStatus;
}

export const DEFAULT_BUTTON_FILTER: ButtonFilter = {
  moduleCode: "",
  buttonCode: "",
  status: "ALL",
};

const STATUS_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
] as const;

interface Props {
  form: UseFormReturn<ButtonFilter>;
  moduleOptions: { value: string; label: string }[];
}

export function ButtonListFilter({ form, moduleOptions }: Props) {
  const { register, setValue, control } = form;

  const allModuleOptions = [{ value: "", label: "All" }, ...moduleOptions];

  const [acQuery, setAcQuery] = useState("");

  const { data: suggestions = [] } = useQuery({
    queryKey: ["access-button", "autocomplete", acQuery],
    queryFn: () => accessButtonUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
    select: (items): CodeBoxSuggestion[] =>
      items.map((i) => ({ code: i.code, name: i.name })),
  });

  function handleSelect(item: CodeBoxSuggestion) {
    setValue("buttonCode", item.code);
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
            label="Button Code"
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: "Button Code", ...register("buttonCode") }}
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
