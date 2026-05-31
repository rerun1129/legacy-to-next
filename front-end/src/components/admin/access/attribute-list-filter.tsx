"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
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

interface Props {
  form: UseFormReturn<AttributeFilter>;
}

export function AttributeListFilter({ form }: Props) {
  const t = useTranslations("admin.attribute.filter");
  const { register, setValue, control } = form;

  const statusOptions = [
    { value: "ALL",      label: t("all")      },
    { value: "ACTIVE",   label: t("active")   },
    { value: "INACTIVE", label: t("inactive") },
  ];

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
            label={t("attributeKey")}
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: t("attributeKeyPlaceholder"), ...register("attributeKey") }}
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
