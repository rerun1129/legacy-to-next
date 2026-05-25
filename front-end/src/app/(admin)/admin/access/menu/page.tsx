"use client";

import { LayoutList } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessMenuListClient } from "@/components/admin/access/menu-list-client";

export default function AdminAccessMenuPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_MENU">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><LayoutList size={14} /></div>
            Access - Menu
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <AccessMenuListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
