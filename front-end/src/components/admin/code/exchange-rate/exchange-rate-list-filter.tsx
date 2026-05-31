"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import type { ExchangeRateFilter } from "@/domain/code/exchange-rate";
import { currencyUseCases } from "@/application/code/currency/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<ExchangeRateFilter>;
}

export function ExchangeRateListFilter({ form }: Props) {
  const t = useTranslations("admin.exchangeRate.filter");
  useListFilterSync(form, "/admin/code/exchange-rate/list");
  const { register, setValue } = form;

  const scopeOptions = [
    { value: "ALL", label: t("all") },
    { value: "ACTIVE", label: t("active") },
    { value: "INACTIVE", label: t("inactive") },
    { value: "DELETED", label: t("deleted") },
  ];

  const [fromAcQuery, setFromAcQuery] = useState("");
  const [toAcQuery, setToAcQuery] = useState("");

  const { data: fromSuggestions = [] } = useQuery({
    queryKey: ["admin-code-currency", "autocomplete", fromAcQuery],
    queryFn: () => currencyUseCases.autocomplete(fromAcQuery),
    enabled: fromAcQuery.length >= 1,
    staleTime: 30_000,
  });

  const { data: toSuggestions = [] } = useQuery({
    queryKey: ["admin-code-currency", "autocomplete", toAcQuery],
    queryFn: () => currencyUseCases.autocomplete(toAcQuery),
    enabled: toAcQuery.length >= 1,
    staleTime: 30_000,
  });

  function handleFromSelect(item: CodeBoxSuggestion) {
    setValue("fromCurrencyCode", item.code);
  }

  function handleToSelect(item: CodeBoxSuggestion) {
    setValue("toCurrencyCode", item.code);
  }

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
          <CodeBox
            kind="code-only"
            label={t("fromCurrencyCode")}
            onSearch={setFromAcQuery}
            suggestions={fromSuggestions}
            onSelect={handleFromSelect}
            codeProps={{ placeholder: t("fromCurrencyCodePlaceholder"), ...register("fromCurrencyCode") }}
          />
          <CodeBox
            kind="code-only"
            label={t("toCurrencyCode")}
            onSearch={setToAcQuery}
            suggestions={toSuggestions}
            onSelect={handleToSelect}
            codeProps={{ placeholder: t("toCurrencyCodePlaceholder"), ...register("toCurrencyCode") }}
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
