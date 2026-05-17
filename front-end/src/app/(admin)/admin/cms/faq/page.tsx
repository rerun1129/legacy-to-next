"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { FaqPanelClient } from "@/components/admin/cms/faq/faq-panel-client";

export default function AdminFaqPage() {
  return (
    <AdminGuard requiredPermission="CMS_MANAGE">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>FAQ 관리</h1>
        <FaqPanelClient />
      </div>
    </AdminGuard>
  );
}
