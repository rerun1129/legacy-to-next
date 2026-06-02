"use client";

import { createContext, useContext } from "react";
import type { ReactNode } from "react";

const EntryScopeContext = createContext<string>("");

export function EntryScopeProvider({ scope, children }: { scope: string; children: ReactNode }) {
  return <EntryScopeContext.Provider value={scope}>{children}</EntryScopeContext.Provider>;
}

export function useEntryScope(): string {
  return useContext(EntryScopeContext);
}
