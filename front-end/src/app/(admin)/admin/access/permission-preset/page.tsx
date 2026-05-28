"use client";

import { BookKey } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { PermissionPresetListClient } from "@/components/admin/access/permission-preset-list-client";

export default function AdminAccessPermissionPresetPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_PERMISSION_PRESET">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><BookKey size={14} /></div>
            Access - Permission Preset
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <PermissionPresetListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
