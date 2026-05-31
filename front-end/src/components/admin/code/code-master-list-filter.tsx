"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import type { CodeMasterFilter } from "@/domain/code-master";
import { codeMasterUseCases } from "@/application/code-master/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

interface Props {
  form: UseFormReturn<CodeMasterFilter>;
}

export function CodeMasterListFilter({ form }: Props) {
  const t = useTranslations("admin.code.master.filter");
  const { register, setValue } = form;

  const activeOptions = [
    { value: "ALL", label: t("all") },
    { value: "ACTIVE", label: t("active") },
    { value: "INACTIVE", label: t("inactive") },
  ];

  const [acQuery, setAcQuery] = useState("");
  const { data: suggestions = [] } = useQuery({
    queryKey: ["admin-code-master", "autocomplete", acQuery],
    queryFn: () => codeMasterUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
  });

  function handleSelect(item: CodeBoxSuggestion) {
    setValue("masterCode", item.code);
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label={t("masterCode")}
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: t("masterCodePlaceholder"), ...register("masterCode") }}
          />
          <div className="lcn">
            <span className="lcn__label">{t("masterName")}</span>
            <input
              className="box-panel"
              placeholder={t("masterNamePlaceholder")}
              {...register("masterName")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("status")}</span>
            <Controller
              name="active"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={activeOptions}
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
