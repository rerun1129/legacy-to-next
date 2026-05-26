"use client";

import { Package } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { FreightListClient } from "@/components/admin/code/freight/freight-list-client";

export default function AdminFreightListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_FREIGHT">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Package size={14} /></div>
            Freight Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <FreightListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
