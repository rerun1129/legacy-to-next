"use client";

import { Box } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { PackageUnitListClient } from "@/components/admin/code/package-unit/package-unit-list-client";

export default function AdminPackageUnitListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_PACKAGE">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Box size={14} /></div>
            Package Unit Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <PackageUnitListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
