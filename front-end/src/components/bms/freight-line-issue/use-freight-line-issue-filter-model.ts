"use client";

import { useMemo } from "react";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { LabelOption } from "@/components/shared/inputs/_types";
import { ISSUED_STATUS_OPTIONS } from "./freight-line-issue-list-filter-options";

/** 필터 폼 값 인터페이스 */
export interface FreightLineIssueFilter {
  customerCode: string;
  customerName: string;
  financialDocType: string;
  jobDiv: string;
  bound: string;
  performanceDtFrom: string;
  performanceDtTo: string;
  issuedStatus: string;
}

function getDefaultMonthRange() {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const pad = (n: number) => String(n).padStart(2, "0");
  const lastDate = new Date(y, m + 1, 0).getDate();
  return {
    from: `${y}${pad(m + 1)}01`,
    to: `${y}${pad(m + 1)}${pad(lastDate)}`,
  };
}

const { from, to } = getDefaultMonthRange();

export const DEFAULT_ISSUE_FILTER: FreightLineIssueFilter = {
  customerCode: "",
  customerName: "",
  financialDocType: "",
  jobDiv: "",
  bound: "",
  performanceDtFrom: from,
  performanceDtTo: to,
  issuedStatus: "",
};

export function useFreightLineIssueFilterModel(
  form: UseFormReturn<FreightLineIssueFilter>,
  scope: string,
) {
  // routeKey 기준 영속화 — 세금계산서/전표 발급 화면이 독립 scope
  useListFilterSync(form, scope);

  const { register, setValue } = form;
  const t = useTranslations("bms.issue.filter");

  const { options: jobDivOptions, isLoading: jobDivLoading, placeholder: jobDivPlaceholder } =
    useEnumOptions("housebl.JobDiv");

  const { options: boundOptions, isLoading: boundLoading, placeholder: boundPlaceholder } =
    useEnumOptions("Bound");

  const allOption = useMemo<LabelOption>(() => ({ value: "", label: t("all") }), [t]);

  const issuedStatusOptions = useMemo<LabelOption[]>(
    () => [
      allOption,
      ...ISSUED_STATUS_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    ],
    [allOption, t],
  );

  const jobDivOptionsWithAll = useMemo<LabelOption[]>(
    () => [allOption, ...jobDivOptions],
    [allOption, jobDivOptions],
  );

  const boundOptionsWithAll = useMemo<LabelOption[]>(
    () => [allOption, ...boundOptions],
    [allOption, boundOptions],
  );

  const customer = useCodeAutocomplete(CODE_SOURCES.customer);

  return {
    t,
    register,
    setValue,
    issuedStatusOptions,
    jobDivOptionsWithAll,
    jobDivLoading,
    jobDivPlaceholder,
    boundOptionsWithAll,
    boundLoading,
    boundPlaceholder,
    customer,
  };
}
