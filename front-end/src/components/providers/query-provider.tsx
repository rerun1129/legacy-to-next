"use client";

import { QueryClient, QueryClientProvider, QueryCache, MutationCache } from "@tanstack/react-query";
import { useState } from "react";
import { toast } from "@/lib/toast-store";
import { ApiError } from "@/adapter/out/api/errors";

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const [client] = useState(
    () =>
      new QueryClient({
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
      })
  );
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
