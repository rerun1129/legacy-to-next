"use client";

import { create } from "zustand";

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
}

// ─── Nav label mapping (mirrors sidebar.tsx NAV data) ───────
const PATH_LABEL_MAP: Record<string, string> = {
  "/fms/house-bl/sea-exp":      "Sea Export — List",
  "/fms/house-bl/sea-exp/new":  "Sea Export — Entry",
  "/fms/house-bl/sea-imp":      "Sea Import — List",
  "/fms/house-bl/sea-imp/new":  "Sea Import — Entry",
  "/fms/house-bl/air-exp":      "Air Export — List",
  "/fms/house-bl/air-exp/new":  "Air Export — Entry",
  "/fms/house-bl/air-imp":      "Air Import — List",
  "/fms/house-bl/air-imp/new":  "Air Import — Entry",
  "/fms/master-bl/sea-exp":     "Sea Export — List",
  "/fms/master-bl/sea-exp/new": "Sea Export — Entry",
  "/fms/master-bl/sea-imp":     "Sea Import — List",
  "/fms/master-bl/sea-imp/new": "Sea Import — Entry",
  "/fms/master-bl/air-exp":     "Air Export — List",
  "/fms/master-bl/air-exp/new": "Air Export — Entry",
  "/fms/master-bl/air-imp":     "Air Import — List",
  "/fms/master-bl/air-imp/new": "Air Import — Entry",
  "/fms/truck-bl":              "List",
  "/fms/truck-bl/new":          "Entry",
  "/fms/non-bl":                "List",
  "/fms/non-bl/new":            "Entry",
};

export function inferLabelFromPath(pathname: string): string {
  if (PATH_LABEL_MAP[pathname]) return PATH_LABEL_MAP[pathname];
  // Fallback: last path segment, capitalized
  const segments = pathname.split("/").filter(Boolean);
  const last = segments[segments.length - 1] ?? "Page";
  return last.charAt(0).toUpperCase() + last.slice(1).replace(/-/g, " ");
}

// ─── Derive initial tabs from current URL (client-only) ─────
function getInitialTabs(): Tab[] {
  if (typeof window === "undefined") return []; // SSR: empty
  const pathname = window.location.pathname;
  // Dashboard is not included in the tab strip
  if (!pathname || pathname === "/" || pathname === "/dashboard") return [];
  return [{ id: pathname, label: inferLabelFromPath(pathname), href: pathname }];
}

export const useTabs = create<TabStore>((set, get) => ({
  tabs: getInitialTabs(),

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
    // navigate to: previous tab, or next tab, or dashboard fallback
    const target = next[Math.max(0, idx - 1)];
    return target?.href ?? "/dashboard";
  },

  activateTab(_id) {
    // no-op — active tab is derived from current pathname
  },
}));
