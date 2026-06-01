"use client";

import { useQuery } from "@tanstack/react-query";
import type { BlQuickSearchFilters, BlQuickSearchItem } from "@/domain/bl-quick-search";
import { blQuickSearchPort } from "@/lib/ports";

// 모듈 상수로 빈 배열 고정 — data ?? [] 인라인 시 매 렌더 새 참조가 생겨 react-query 무한루프를 유발
const EMPTY: BlQuickSearchItem[] = [];

export function useBlQuickSearch(query: string, filters: BlQuickSearchFilters) {
  const { data, isFetching } = useQuery({
    queryKey: ["bl-quick-search", query, filters],
    queryFn: () => blQuickSearchPort.autocomplete(query, filters, 20),
    enabled: query.trim().length >= 1,
    staleTime: 30_000,
  });
  return { items: data ?? EMPTY, isLoading: isFetching };
}
