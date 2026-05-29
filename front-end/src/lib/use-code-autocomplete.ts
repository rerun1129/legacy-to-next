"use client";
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

// 모듈 상수로 빈 배열 고정 — data ?? [] 인라인 시 매 렌더 새 참조가 생겨 react-query 무한루프를 유발
const EMPTY: CodeBoxSuggestion[] = [];

export interface AutocompleteSource {
  key: string;
  fetch: (q: string) => Promise<CodeBoxSuggestion[]>;
}

export function useCodeAutocomplete(source: AutocompleteSource) {
  const [query, setQuery] = useState("");
  const { data, isFetching } = useQuery({
    queryKey: [source.key, "autocomplete", query],
    queryFn: () => source.fetch(query),
    enabled: query.length >= 1,
    staleTime: 30_000,
  });
  return { onSearch: setQuery, suggestions: data ?? EMPTY, suggestionsLoading: isFetching };
}
