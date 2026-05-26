"use client";

import { Hash } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { HsCodeListClient } from "@/components/admin/code/hs-code/hs-code-list-client";

export default function AdminHsCodeListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_HSCODE">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Hash size={14} /></div>
            HS Code Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <HsCodeListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
