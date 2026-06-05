"use client";

import { useQuery } from "@tanstack/react-query";
import type { BlQuickSearchFilters } from "@/domain/bl-quick-search";
import type {
  SearchFinancialDocumentInput,
  FinancialDocumentSearchRow,
} from "@/application/bms/financial-document/ports";
import { financialDocumentPort } from "@/lib/ports";

// 모듈 상수로 빈 배열 고정 — data ?? [] 인라인 시 매 렌더 새 참조가 생겨 react-query 무한루프를 유발
const EMPTY: FinancialDocumentSearchRow[] = [];

function toDocSearchInput(
  query: string,
  filters: BlQuickSearchFilters,
): SearchFinancialDocumentInput {
  const dateFrom = filters.dateFrom || null;
  const dateTo = filters.dateTo || null;

  return {
    documentTypes: ["INVOICE", "PAYMENT", "DEBIT", "CREDIT"],
    documentNoLike: query,
    jobDiv: filters.jobDiv || null,
    bound: filters.bound || null,
    teamCode: filters.teamCode || null,
    operator: filters.operatorCode || null,
    etdFrom: filters.dateKind === "ETD" ? dateFrom : null,
    etdTo: filters.dateKind === "ETD" ? dateTo : null,
    etaFrom: filters.dateKind === "ETA" ? dateFrom : null,
    etaTo: filters.dateKind === "ETA" ? dateTo : null,
    customerCode: null,
    documentStatus: null,
    documentDtFrom: null,
    documentDtTo: null,
    performanceDtFrom: null,
    performanceDtTo: null,
  };
}

export function useBmsDocQuickSearch(query: string, filters: BlQuickSearchFilters) {
  const { data, isFetching } = useQuery({
    queryKey: ["bms-doc-quick-search", query, filters],
    queryFn: () => financialDocumentPort.search(toDocSearchInput(query, filters), 0, 20),
    enabled: query.trim().length >= 1,
    staleTime: 30_000,
  });
  return { items: data?.content ?? EMPTY, isLoading: isFetching };
}
