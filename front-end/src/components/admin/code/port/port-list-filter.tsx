"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import type { PortFilter } from "@/domain/code/port";
import { portUseCases } from "@/application/code/port/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<PortFilter>;
}

export function PortListFilter({ form }: Props) {
  const t = useTranslations("admin.port.filter");
  useListFilterSync(form, "/admin/code/port/list");
  const { register, setValue } = form;

  const scopeOptions = [
    { value: "ALL", label: t("all") },
    { value: "ACTIVE", label: t("active") },
    { value: "INACTIVE", label: t("inactive") },
    { value: "DELETED", label: t("deleted") },
  ];

  const portTypeOptions = [
    { value: "ALL", label: t("typeAll") },
    { value: "SEA", label: t("typeSea") },
    { value: "AIR", label: t("typeAir") },
  ];

  const [acQuery, setAcQuery] = useState("");
  const { data: suggestions = [] } = useQuery({
    queryKey: ["admin-code-port", "autocomplete", acQuery],
    queryFn: () => portUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
  });

  function handleSelect(item: CodeBoxSuggestion) {
    setValue("portCode", item.code);
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label={t("portCode")}
            onSearch={setAcQuery}
            suggestions={suggestions}
            onSelect={handleSelect}
            codeProps={{ placeholder: t("portCodePlaceholder"), ...register("portCode") }}
          />
          <div className="lcn">
            <span className="lcn__label">{t("name")}</span>
            <input
              className="box-panel"
              placeholder={t("namePlaceholder")}
              {...register("name")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("countryCode")}</span>
            <input
              className="box-panel"
              placeholder={t("countryCodePlaceholder")}
              {...register("countryCode")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("type")}</span>
            <Controller
              name="portType"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={portTypeOptions}
                  value={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">{t("status")}</span>
            <Controller
              name="scope"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={scopeOptions}
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
