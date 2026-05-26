"use client";

import { Anchor } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { PortListClient } from "@/components/admin/code/port/port-list-client";

export default function AdminPortListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_PORT">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Anchor size={14} /></div>
            Port Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <PortListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
