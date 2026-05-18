"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessButtonListClient } from "@/components/admin/access/button-list-client";

export default function AdminAccessButtonPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_BUTTON">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>Access - Button</h1>
        <AccessButtonListClient />
      </div>
    </AdminGuard>
  );
}
