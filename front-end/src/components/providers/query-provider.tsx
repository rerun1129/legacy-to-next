"use client";

import { QueryClient, QueryClientProvider, QueryCache, MutationCache } from "@tanstack/react-query";
import { useState } from "react";
import { toast } from "@/lib/toast-store";
import { ApiError } from "@/adapter/out/api/errors";

const LIST_LRU_LIMIT = 5;

function lruKey(qk: readonly unknown[]): string {
  return [qk[0], qk[1], typeof qk[2] === "string" ? qk[2] : ""].join("::");
}

function pruneListLru(queryCache: QueryCache): void {
  const groups = new Map<string, { queryKey: unknown[]; updatedAt: number }[]>();
  queryCache
    .findAll({
      predicate: (q) =>
        Array.isArray(q.queryKey) &&
        q.queryKey[1] === "list" &&
        q.getObserversCount() === 0,
    })
    .forEach((q) => {
      const qk = q.queryKey as unknown[];
      const key = lruKey(qk);
      const arr = groups.get(key) ?? [];
      arr.push({ queryKey: qk, updatedAt: q.state.dataUpdatedAt });
      groups.set(key, arr);
    });

  for (const arr of groups.values()) {
    if (arr.length <= LIST_LRU_LIMIT) continue;
    arr
      .sort((a, b) => a.updatedAt - b.updatedAt)
      .slice(0, arr.length - LIST_LRU_LIMIT)
      .forEach(({ queryKey }) => {
        const target = queryCache.find({ queryKey, exact: true });
        if (target) queryCache.remove(target);
      });
  }
}

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const [client] = useState(() => {
    const c = new QueryClient({
      defaultOptions: {
        queries: {
          staleTime: 30_000,
          retry: 1,
          refetchOnWindowFocus: false,
        },
        mutations: {
          retry: 0,
        },
      },
      queryCache: new QueryCache({
        onError: (error) => {
          if (error instanceof ApiError) toast.error(error.message);
        },
      }),
      mutationCache: new MutationCache({
        onError: (error) => {
          if (error instanceof ApiError) toast.error(error.message);
        },
      }),
    });

    // List 캐시 LRU(화면별 5개) — gcTime: Infinity 환경에서 검색 조건 다양화 시 누적 차단 (§6.36)
    const queryCache = c.getQueryCache();
    queryCache.subscribe((event) => {
      if (event.type !== "added" && event.type !== "observerRemoved") return;
      pruneListLru(queryCache);
    });

    return c;
  });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
