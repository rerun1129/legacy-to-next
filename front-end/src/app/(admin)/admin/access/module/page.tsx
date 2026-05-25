"use client";

import { Boxes } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessModuleListClient } from "@/components/admin/access/module-list-client";

export default function AdminAccessModulePage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_MODULE">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Boxes size={14} /></div>
            Access - Module
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <AccessModuleListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
