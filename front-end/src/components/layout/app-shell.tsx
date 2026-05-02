"use client";

import { useState } from "react";
import { Topbar } from "./topbar";
import { Sidebar } from "./sidebar";
import { ToastViewport } from "@/components/shared/toast";

export function AppShell({ children }: { children: React.ReactNode }) {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className={`app${collapsed ? " is-side-collapsed" : ""}`}>
      <Topbar
        onToggleSidebar={() => setCollapsed((v) => !v)}
        sidebarCollapsed={collapsed}
      />
      <Sidebar />
      <main className="app__main">{children}</main>
      <ToastViewport />
    </div>
  );
}
