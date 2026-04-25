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

export const useTabs = create<TabStore>((set, get) => ({
  tabs: [{ id: "/dashboard", label: "Dashboard", href: "/dashboard" }],

  addTab(label, href) {
    const { tabs } = get();
    if (tabs.some((t) => t.id === href)) return; // already open
    set({ tabs: [...tabs, { id: href, label, href }] });
  },

  removeTab(id) {
    const { tabs } = get();
    const idx = tabs.findIndex((t) => t.id === id);
    if (idx === -1) return "/dashboard";
    const next = tabs.filter((t) => t.id !== id);
    set({ tabs: next.length ? next : [{ id: "/dashboard", label: "Dashboard", href: "/dashboard" }] });
    // navigate to: previous tab, or next tab, or dashboard
    const target = next[Math.max(0, idx - 1)];
    return target?.href ?? "/dashboard";
  },

  activateTab(_id) {
    // no-op — active tab is derived from current pathname
  },
}));
