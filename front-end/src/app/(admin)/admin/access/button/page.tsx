"use client";

import { MousePointerClick } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessButtonListClient } from "@/components/admin/access/button-list-client";

export default function AdminAccessButtonPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_BUTTON">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><MousePointerClick size={14} /></div>
            Access - Button
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <AccessButtonListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
