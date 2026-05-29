"use client";

import { Database } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { CodeListClient } from "@/components/admin/code/code-list-client";

export default function AdminCodeListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_LIST">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Database size={14} /></div>
            Common Code
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <CodeListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
