"use client";

import { Tag } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessAttributeListClient } from "@/components/admin/access/attribute-list-client";

export default function AdminAccessAttributePage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_ATTRIBUTE">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Tag size={14} /></div>
            Access - Attribute
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <AccessAttributeListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
