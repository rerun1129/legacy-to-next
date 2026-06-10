"use client";

import { useMemo } from "react";
import { usePmsEnumOptions } from "@/application/pms/enums/use-pms-enum";
import type { LabelOption } from "@/components/shared/inputs/_types";

/**
 * PS-01 필터 바 전용 PMS enum 옵션 배열.
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

  // PMS 백엔드 enum 훅 — 전체(allOption) 붙는 항목
  const { options: documentTypeRaw }   = usePmsEnumOptions("DocumentType");
  const { options: documentStatusRaw } = usePmsEnumOptions("DocumentStatus");
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

  // 전체 있음
  const documentTypeOptions = useMemo<LabelOption[]>(
    () => [allOption, ...documentTypeRaw],
    [allOption, documentTypeRaw]
  );
  const documentStatusOptions = useMemo<LabelOption[]>(
    () => [allOption, ...documentStatusRaw],
    [allOption, documentStatusRaw]
  );
  const jobDivOptionsWithAll = useMemo<LabelOption[]>(
    () => [allOption, ...jobDivOptions],
    [allOption, jobDivOptions]
  );
  const boundOptionsWithAll = useMemo<LabelOption[]>(
    () => [allOption, ...boundOptions],
    [allOption, boundOptions]
  );

  return {
    basisOptions,
    dateKindOptions,
    documentTypeOptions,
    documentStatusOptions,
    jobDivOptionsWithAll,
    jobDivLoading,
    jobDivPlaceholder,
    boundOptionsWithAll,
    boundLoading,
    boundPlaceholder,
  };
}
