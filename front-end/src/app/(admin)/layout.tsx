"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { AppShell } from "@/components/layout/app-shell";
import { AdminLogoutButton } from "@/components/admin/admin-logout-button";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <AdminGuard>
      <AppShell>
        <div style={{
          display: "flex",
          justifyContent: "flex-end",
          padding: "8px 16px",
          borderBottom: "1px solid var(--border)",
        }}>
          <AdminLogoutButton />
        </div>
        {children}
      </AppShell>
    </AdminGuard>
  );
}
