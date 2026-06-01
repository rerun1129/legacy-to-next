"use client";

import { useState } from "react";
import { Topbar } from "./topbar";
import { Sidebar } from "./sidebar";
import { ToastViewport } from "@/components/shared/toast";

export function AppShell({ children }: { children: React.ReactNode }) {
  const [collapsed, setCollapsed] = useState(false);
  const [quickSearch, setQuickSearch] = useState(false);

  return (
    <div className={`app${collapsed ? " is-side-collapsed" : ""}${quickSearch ? " is-side-quicksearch" : ""}`}>
      <Topbar
        onToggleSidebar={() => setCollapsed((v) => !v)}
        sidebarCollapsed={collapsed}
      />
      <Sidebar
        quickSearchOpen={quickSearch}
        onOpenQuickSearch={() => { setCollapsed(false); setQuickSearch(true); }}
        onCloseQuickSearch={() => setQuickSearch(false)}
      />
      <main className="app__main">{children}</main>
      <ToastViewport />
    </div>
  );
}
