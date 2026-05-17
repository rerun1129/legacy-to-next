"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { PartnerListClient } from "@/components/admin/partner/partner-list-client";

export default function AdminPartnerListPage() {
  return (
    <AdminGuard requiredPermission="PARTNER_MANAGE">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>협력사 관리</h1>
        <PartnerListClient />
      </div>
    </AdminGuard>
  );
}
