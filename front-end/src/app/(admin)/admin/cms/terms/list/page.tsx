"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { TermsListClient } from "@/components/admin/cms/terms/terms-list-client";

export default function AdminTermsListPage() {
  return (
    <AdminGuard requiredPermission="CMS_MANAGE">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>약관·정책 관리</h1>
        <TermsListClient />
      </div>
    </AdminGuard>
  );
}
