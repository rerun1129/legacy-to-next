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
        // 제거 직전 재확인: findAll 이후 짧은 간격에 observer가 붙었을 수 있으므로
        // observer > 0(활성) 쿼리는 절대 제거하지 않는다(무한 refetch 방지).
        if (target && target.getObserversCount() === 0) queryCache.remove(target);
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
    // subscribe 콜백에서 queryCache.remove()를 동기 호출하면 'added'/'removed' 이벤트가
    // 재진입하며, dev StrictMode의 observer 전환 레이스와 맞물려 활성 list 쿼리가
    // 제거→refetch되는 무한 루프가 발생한다. microtask로 분리하고 재진입 가드를 둔다.
    const queryCache = c.getQueryCache();
    let prunePending = false;
    queryCache.subscribe((event) => {
      if (event.type !== "added" && event.type !== "observerRemoved") return;
      if (prunePending) return;
      prunePending = true;
      queueMicrotask(() => {
        prunePending = false;
        pruneListLru(queryCache);
      });
    });

    return c;
  });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
