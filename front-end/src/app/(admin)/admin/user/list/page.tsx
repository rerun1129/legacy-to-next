"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { UserListClient } from "@/components/admin/user/user-list-client";

export default function AdminUserListPage() {
  return (
    <AdminGuard requiredPermission="USER_MANAGE">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>사용자 관리</h1>
        <UserListClient />
      </div>
    </AdminGuard>
  );
}
