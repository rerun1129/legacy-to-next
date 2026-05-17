"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { NoticeListClient } from "@/components/admin/cms/notice/notice-list-client";

export default function AdminNoticeListPage() {
  return (
    <AdminGuard requiredPermission="CMS_MANAGE">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>공지사항 관리</h1>
        <NoticeListClient />
      </div>
    </AdminGuard>
  );
}
