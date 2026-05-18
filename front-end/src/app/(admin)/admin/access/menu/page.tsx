"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessMenuListClient } from "@/components/admin/access/menu-list-client";

export default function AdminAccessMenuPage() {
  return (
    <AdminGuard>
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>Access - Menu</h1>
        <AccessMenuListClient />
      </div>
    </AdminGuard>
  );
}
