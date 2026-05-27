"use client";

import { useState } from "react";
import { Controller } from "react-hook-form";
import type { UseFormReturn } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import type { ExchangeRateFilter } from "@/domain/code/exchange-rate";
import { exchangeRateUseCases } from "@/application/code/exchange-rate/use-cases";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { useListFilterSync } from "@/lib/use-list-filter-sync";

interface Props {
  form: UseFormReturn<ExchangeRateFilter>;
}

const SCOPE_OPTIONS = [
  { value: "ALL", label: "All" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
  { value: "DELETED", label: "Deleted" },
] as const;

export function ExchangeRateListFilter({ form }: Props) {
  useListFilterSync(form, "/admin/code/exchange-rate/list");
  const { register, setValue } = form;

  const [fromAcQuery, setFromAcQuery] = useState("");
  const [toAcQuery, setToAcQuery] = useState("");

  const { data: fromSuggestions = [] } = useQuery({
    queryKey: ["admin-code-exchange-rate", "autocomplete", "from", fromAcQuery],
    queryFn: () => exchangeRateUseCases.autocomplete(fromAcQuery),
    enabled: fromAcQuery.length >= 1,
    staleTime: 30_000,
  });

  const { data: toSuggestions = [] } = useQuery({
    queryKey: ["admin-code-exchange-rate", "autocomplete", "to", toAcQuery],
    queryFn: () => exchangeRateUseCases.autocomplete(toAcQuery),
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
            label="From Currency"
            onSearch={setFromAcQuery}
            suggestions={fromSuggestions}
            onSelect={handleFromSelect}
            codeProps={{ placeholder: "e.g. USD", ...register("fromCurrencyCode") }}
          />
          <CodeBox
            kind="code-only"
            label="To Currency"
            onSearch={setToAcQuery}
            suggestions={toSuggestions}
            onSelect={handleToSelect}
            codeProps={{ placeholder: "e.g. KRW", ...register("toCurrencyCode") }}
          />
          <div className="lcn">
            <span className="lcn__label">Name</span>
            <input
              className="box-panel"
              placeholder="Name (partial)"
              {...register("name")}
            />
          </div>
          <div className="lcn">
            <span className="lcn__label">Status</span>
            <Controller
              name="scope"
              control={form.control}
              render={({ field }) => (
                <ComboBox
                  variant="panel"
                  options={[...SCOPE_OPTIONS]}
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
