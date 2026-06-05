"use client";

import type { UseFormReturn } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import type { FinancialDocumentFilter } from "./use-financial-document-list-filter-model";

interface AutocompleteState {
  onSearch: (q: string) => void;
  suggestions: CodeBoxSuggestion[];
  suggestionsLoading: boolean;
}

interface Props {
  register: UseFormReturn<FinancialDocumentFilter>["register"];
  setValue: UseFormReturn<FinancialDocumentFilter>["setValue"];
  t: (key: string) => string;
  customer: AutocompleteState;
  team: AutocompleteState;
  operator: AutocompleteState;
}

/** 거래처 / 팀 / 담당자 자동완성 필드 */
export function FinancialDocumentFilterPartyFields({
  register,
  setValue,
  t,
  customer,
  team,
  operator,
}: Props) {
  function handleSelectCustomer(item: CodeBoxSuggestion) {
    setValue("customerCode", item.code, { shouldDirty: true });
    setValue("customerName", item.name, { shouldDirty: true });
  }

  function handleSelectTeam(item: CodeBoxSuggestion) {
    setValue("teamCode", item.code, { shouldDirty: true });
    setValue("teamName", item.name, { shouldDirty: true });
  }

  function handleSelectOperator(item: CodeBoxSuggestion) {
    setValue("operator", item.code, { shouldDirty: true });
    setValue("operatorName", item.name, { shouldDirty: true });
  }

  return (
    <>
      <CodeBox
        kind="lcn"
        label={t("customer")}
        codeProps={{ ...register("customerCode"), placeholder: t("customerPlaceholder") }}
        nameProps={{ ...register("customerName"), readOnly: true }}
        suggestions={customer.suggestions}
        onSearch={customer.onSearch}
        onSelect={handleSelectCustomer}
        suggestionsLoading={customer.suggestionsLoading}
      />
      <CodeBox
        kind="lcn"
        label={t("team")}
        codeProps={{ ...register("teamCode"), placeholder: t("teamPlaceholder") }}
        nameProps={{ ...register("teamName"), readOnly: true }}
        suggestions={team.suggestions}
        onSearch={team.onSearch}
        onSelect={handleSelectTeam}
        suggestionsLoading={team.suggestionsLoading}
      />
      <CodeBox
        kind="lcn"
        label={t("operator")}
        codeProps={{ ...register("operator"), placeholder: t("operatorPlaceholder") }}
        nameProps={{ ...register("operatorName"), readOnly: true }}
        suggestions={operator.suggestions}
        onSearch={operator.onSearch}
        onSelect={handleSelectOperator}
        suggestionsLoading={operator.suggestionsLoading}
      />
    </>
  );
}
