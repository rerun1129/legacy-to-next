"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessAttributeListClient } from "@/components/admin/access/attribute-list-client";

export default function AdminAccessAttributePage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_ATTRIBUTE">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>Access - Attribute</h1>
        <AccessAttributeListClient />
      </div>
    </AdminGuard>
  );
}
