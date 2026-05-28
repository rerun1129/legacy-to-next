"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { accessAttributeUseCases } from "@/application/access/attribute/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export type AttributeStatus = "ALL" | "ACTIVE" | "INACTIVE";

export interface AttributeFilter {
  attributeKey: string;
  status: AttributeStatus;
}

export const DEFAULT_ATTRIBUTE_FILTER: AttributeFilter = {
  attributeKey: "",
  status: "ALL",
};

const STATUS_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
] as const;

interface Props {
  form: UseFormReturn<AttributeFilter>;
}

export function AttributeListFilter({ form }: Props) {
  const { register, setValue, control } = form;

  const [acQuery, setAcQuery] = useState("");

  const { data: suggestions = [] } = useQuery({
    queryKey: ["access-attribute", "autocomplete", acQuery],
    queryFn: () => accessAttributeUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
    select: (items): CodeBoxSuggestion[] => items.map((item) => ({ code: item.code, name: item.name })),
  });

  function handleSelect(item: CodeBoxSuggestion) {
    setValue("attributeKey", item.code);
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label="Attribute Key"
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: "Attribute Key", ...register("attributeKey") }}
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
