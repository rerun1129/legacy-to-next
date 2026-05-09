"use client";

import { create } from "zustand";
import { listFilterStore } from "./use-list-filter-store";

export interface Tab {
  id: string;
  label: string;
  href: string;
}

interface TabStore {
  tabs: Tab[];
  addTab: (label: string, href: string) => void;
  removeTab: (id: string) => string; // returns href to navigate to after close
  activateTab: (id: string) => void;
  initFromPath: (pathname: string) => void;
  reorderTabs: (fromId: string, toInsertIndex: number) => void;
  closeOtherTabs: (id: string) => void;
  closeTabsToRight: (id: string) => void;
  closeTabsToLeft: (id: string) => void;
}

// ─── Nav label mapping (mirrors sidebar.tsx NAV data) ───────
const PATH_LABEL_MAP: Record<string, string> = {
  "/fms/house-bl/sea-exp/list":  "House B/L Sea Export List",
  "/fms/house-bl/sea-exp/entry": "House B/L Sea Export Entry",
  "/fms/house-bl/sea-imp/list":  "House B/L Sea Import List",
  "/fms/house-bl/sea-imp/entry": "House B/L Sea Import Entry",
  "/fms/house-bl/air-exp/list":  "House B/L Air Export List",
  "/fms/house-bl/air-exp/entry": "House B/L Air Export Entry",
  "/fms/house-bl/air-imp/list":  "House B/L Air Import List",
  "/fms/house-bl/air-imp/entry": "House B/L Air Import Entry",
  "/fms/master-bl/sea-exp/list":  "Master B/L Sea Export List",
  "/fms/master-bl/sea-exp/entry": "Master B/L Sea Export Entry",
  "/fms/master-bl/sea-imp/list":  "Master B/L Sea Import List",
  "/fms/master-bl/sea-imp/entry": "Master B/L Sea Import Entry",
  "/fms/master-bl/air-exp/list":  "Master B/L Air Export List",
  "/fms/master-bl/air-exp/entry": "Master B/L Air Export Entry",
  "/fms/master-bl/air-imp/list":  "Master B/L Air Import List",
  "/fms/master-bl/air-imp/entry": "Master B/L Air Import Entry",
  "/fms/truck-bl/list":  "Truck B/L List",
  "/fms/truck-bl/entry": "Truck B/L Entry",
  "/fms/non-bl/list":    "Non B/L List",
  "/fms/non-bl/entry":   "Non B/L Entry",
};

export function inferLabelFromPath(pathname: string): string {
  if (PATH_LABEL_MAP[pathname]) return PATH_LABEL_MAP[pathname];
  // Fallback: last path segment, capitalized
  const segments = pathname.split("/").filter(Boolean);
  const last = segments[segments.length - 1] ?? "Page";
  return last.charAt(0).toUpperCase() + last.slice(1).replace(/-/g, " ");
}

export const useTabs = create<TabStore>((set, get) => ({
  tabs: [],

  addTab(label, href) {
    // Dashboard is not tracked as a tab
    if (href === "/dashboard") return;
    const { tabs } = get();
    if (tabs.some((t) => t.id === href)) return; // already open
    set({ tabs: [...tabs, { id: href, label, href }] });
  },

  removeTab(id) {
    const { tabs } = get();
    const idx = tabs.findIndex((t) => t.id === id);
    if (idx === -1) return "/dashboard";
    const next = tabs.filter((t) => t.id !== id);
    set({ tabs: next });
    listFilterStore.getState().clearFilter(id);
    // navigate to: previous tab, or next tab, or dashboard fallback
    const target = next[Math.max(0, idx - 1)];
    return target?.href ?? "/dashboard";
  },

  activateTab(_id) {
    // no-op — active tab is derived from current pathname
  },

  reorderTabs(fromId, toInsertIndex) {
    const { tabs } = get();
    const fromIdx = tabs.findIndex(t => t.id === fromId);
    if (fromIdx === -1) return;
    const next = [...tabs];
    const [moved] = next.splice(fromIdx, 1);
    const idx = Math.max(0, Math.min(toInsertIndex, next.length));
    next.splice(idx, 0, moved);
    set({ tabs: next });
  },

  closeOtherTabs(id) {
    const { tabs } = get();
    const target = tabs.find(t => t.id === id);
    if (!target) return;
    tabs.filter(t => t.id !== id).forEach(t => listFilterStore.getState().clearFilter(t.id));
    set({ tabs: [target] });
  },

  closeTabsToRight(id) {
    const { tabs } = get();
    const idx = tabs.findIndex(t => t.id === id);
    if (idx === -1) return;
    tabs.slice(idx + 1).forEach(t => listFilterStore.getState().clearFilter(t.id));
    set({ tabs: tabs.slice(0, idx + 1) });
  },

  closeTabsToLeft(id) {
    const { tabs } = get();
    const idx = tabs.findIndex(t => t.id === id);
    if (idx === -1) return;
    tabs.slice(0, idx).forEach(t => listFilterStore.getState().clearFilter(t.id));
    set({ tabs: tabs.slice(idx) });
  },

  initFromPath(pathname) {
    if (!pathname || pathname === "/" || pathname === "/dashboard") return;
    const { tabs } = get();
    if (tabs.some((t) => t.id === pathname)) return;
    set({ tabs: [...tabs, { id: pathname, label: inferLabelFromPath(pathname), href: pathname }] });
  },
}));
