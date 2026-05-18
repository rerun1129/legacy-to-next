"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { CodeListClient } from "@/components/admin/code/code-list-client";

export default function AdminCodeListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_LIST">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>코드 마스터</h1>
        <CodeListClient />
      </div>
    </AdminGuard>
  );
}
