"use client";

import { Users } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { UserListClient } from "@/components/admin/user/user-list-client";

export default function AdminUserListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_USER_LIST">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Users size={14} /></div>
            User Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <UserListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
