"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessModuleListClient } from "@/components/admin/access/module-list-client";

export default function AdminAccessModulePage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_MODULE">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>Access - Module</h1>
        <AccessModuleListClient />
      </div>
    </AdminGuard>
  );
}
