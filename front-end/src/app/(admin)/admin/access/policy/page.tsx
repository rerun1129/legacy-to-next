"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessPolicyListClient } from "@/components/admin/access/policy-list-client";

export default function AdminAccessPolicyPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_POLICY">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>Access - Policy</h1>
        <AccessPolicyListClient />
      </div>
    </AdminGuard>
  );
}
