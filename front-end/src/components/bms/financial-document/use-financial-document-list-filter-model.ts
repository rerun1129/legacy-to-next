"use client";

import { useMemo } from "react";
import type { UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import { useEnumOptions } from "@/application/enums/use-enum";
import type { LabelOption } from "@/components/shared/inputs/_types";
import {
  DATE_KIND_OPTIONS,
  DOCUMENT_STATUS_OPTIONS,
} from "./financial-document-list-filter-options";

/** 필터 폼 값 인터페이스 */
export interface FinancialDocumentFilter {
  jobDiv: string;
  bound: string;
  dateKind: string;
  dateFrom: string;
  dateTo: string;
  documentStatus: string;
  customerCode: string;
  customerName: string;
  documentNoLike: string;
  teamCode: string;
  teamName: string;
  operator: string;
  operatorName: string;
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

export const DEFAULT_FILTER: FinancialDocumentFilter = {
  jobDiv: "",
  bound: "",
  dateKind: "DOCUMENT_DT",
  dateFrom: from,
  dateTo: to,
  documentStatus: "",
  customerCode: "",
  customerName: "",
  documentNoLike: "",
  teamCode: "",
  teamName: "",
  operator: "",
  operatorName: "",
};

export function useFinancialDocumentListFilterModel(
  form: UseFormReturn<FinancialDocumentFilter>,
  scope: string
) {
  // config.routeKey 기준 영속화 — 각 화면(invoice/payment/dc-note)이 독립 scope를 가짐
  useListFilterSync(form, scope);

  const { register, setValue } = form;
  const t = useTranslations("bms.list.filter");

  // 업무구분 (housebl.JobDiv 열거형)
  const { options: jobDivOptions, isLoading: jobDivLoading, placeholder: jobDivPlaceholder } =
    useEnumOptions("housebl.JobDiv");

  // 수출입 (Bound 열거형)
  const { options: boundOptions, isLoading: boundLoading, placeholder: boundPlaceholder } =
    useEnumOptions("Bound");

  // 일자 종류 옵션 — useMemo로 t 변경 시에만 재계산
  const dateKindOptions = useMemo<LabelOption[]>(
    () => DATE_KIND_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) })),
    [t]
  );

  // 서류 Status 옵션 — 정적(재사용 가능 라벨은 기존 freight.issue.documentStatus 활용)
  const allOption = useMemo<LabelOption>(() => ({ value: "", label: t("all") }), [t]);

  const documentStatusOptions = useMemo<LabelOption[]>(
    () => [allOption, ...DOCUMENT_STATUS_OPTIONS.map((o) => ({ value: o.value, label: t(o.labelKey) }))],
    [allOption, t]
  );

  const jobDivOptionsWithAll = useMemo<LabelOption[]>(
    () => [allOption, ...jobDivOptions],
    [allOption, jobDivOptions]
  );

  const boundOptionsWithAll = useMemo<LabelOption[]>(
    () => [allOption, ...boundOptions],
    [allOption, boundOptions]
  );

  // 자동완성 훅
  const customer  = useCodeAutocomplete(CODE_SOURCES.customer);
  const team      = useCodeAutocomplete(CODE_SOURCES.team);
  const operator  = useCodeAutocomplete(CODE_SOURCES.user);

  return {
    t,
    register,
    setValue,
    dateKindOptions,
    documentStatusOptions,
    jobDivOptionsWithAll,
    jobDivLoading,
    jobDivPlaceholder,
    boundOptionsWithAll,
    boundLoading,
    boundPlaceholder,
    customer,
    team,
    operator,
  };
}
