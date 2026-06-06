"use client";

import { useMemo } from "react";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import { usePmsEnumOptions } from "@/application/pms/enums/use-pms-enum";
import type { LabelOption } from "@/components/shared/inputs/_types";

/**
 * PS-01 필터 바 전용 PMS enum 옵션 배열 + 자동완성 훅.
 * list-client에서 분리하여 파일 크기를 300줄 이하로 유지.
 * 모든 콤보 옵션은 PMS 백엔드 /api/enums/{name} 에서 조회한다.
 */
export function usePmsPerformanceFilterOptions(t: (key: string) => string) {
  const allOption = useMemo<LabelOption>(
    () => ({ value: "", label: t("all") }),
    [t]
  );

  // PMS 백엔드 enum 훅 — 전체(allOption) 없는 항목
  const { options: basisRaw }   = usePmsEnumOptions("AggregationBasis");
  const { options: dateKindRaw } = usePmsEnumOptions("DateKind");
  const { options: portKindRaw } = usePmsEnumOptions("PortKind");

  // PMS 백엔드 enum 훅 — 전체(allOption) 붙는 항목
  const { options: documentTypeRaw }   = usePmsEnumOptions("DocumentType");
  const { options: documentStatusRaw } = usePmsEnumOptions("DocumentStatus");
  const { options: yesNoRaw }          = usePmsEnumOptions("YesNo");
  const {
    options: jobDivOptions,
    isLoading: jobDivLoading,
    placeholder: jobDivPlaceholder,
  } = usePmsEnumOptions("JobDiv");
  const {
    options: boundOptions,
    isLoading: boundLoading,
    placeholder: boundPlaceholder,
  } = usePmsEnumOptions("Bound");

  // 전체 없음
  const basisOptions    = useMemo<LabelOption[]>(() => basisRaw,    [basisRaw]);
  const dateKindOptions = useMemo<LabelOption[]>(() => dateKindRaw, [dateKindRaw]);
  const portKindOptions = useMemo<LabelOption[]>(() => portKindRaw, [portKindRaw]);

  // 전체 있음
  const documentTypeOptions = useMemo<LabelOption[]>(
    () => [allOption, ...documentTypeRaw],
    [allOption, documentTypeRaw]
  );
  const documentStatusOptions = useMemo<LabelOption[]>(
    () => [allOption, ...documentStatusRaw],
    [allOption, documentStatusRaw]
  );
  const groupedOptions = useMemo<LabelOption[]>(
    () => [allOption, ...yesNoRaw],
    [allOption, yesNoRaw]
  );
  const issuedOptions = useMemo<LabelOption[]>(
    () => [allOption, ...yesNoRaw],
    [allOption, yesNoRaw]
  );
  const jobDivOptionsWithAll = useMemo<LabelOption[]>(
    () => [allOption, ...jobDivOptions],
    [allOption, jobDivOptions]
  );
  const boundOptionsWithAll = useMemo<LabelOption[]>(
    () => [allOption, ...boundOptions],
    [allOption, boundOptions]
  );

  // 자동완성
  const actualCustomer = useCodeAutocomplete(CODE_SOURCES.customer);
  const settlePartner  = useCodeAutocomplete(CODE_SOURCES.partner);
  const carrier        = useCodeAutocomplete(CODE_SOURCES.carrier);
  const port           = useCodeAutocomplete(CODE_SOURCES.port);
  const salesMan       = useCodeAutocomplete(CODE_SOURCES.user);
  const team           = useCodeAutocomplete(CODE_SOURCES.team);
  const operator       = useCodeAutocomplete(CODE_SOURCES.user);

  return {
    basisOptions,
    dateKindOptions,
    portKindOptions,
    groupedOptions,
    issuedOptions,
    documentTypeOptions,
    documentStatusOptions,
    jobDivOptionsWithAll,
    jobDivLoading,
    jobDivPlaceholder,
    boundOptionsWithAll,
    boundLoading,
    boundPlaceholder,
    actualCustomer,
    settlePartner,
    carrier,
    port,
    salesMan,
    team,
    operator,
  };
}
