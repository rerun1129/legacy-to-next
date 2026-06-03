import { create } from "zustand";
import type { EntryDomain } from "./use-entry-focus-store";

interface EntryTabState {
  tabs: Partial<Record<EntryDomain, string>>;
  setTab: (domain: EntryDomain, tab: string) => void;
}

// SPA 네비게이션 중 마지막 탭 기억 (새로고침 시 리셋 — focus store와 동일 정책)
export const useEntryTabStore = create<EntryTabState>()((set) => ({
  tabs: {},
  setTab: (domain, tab) => set((s) => ({ tabs: { ...s.tabs, [domain]: tab } })),
}));
