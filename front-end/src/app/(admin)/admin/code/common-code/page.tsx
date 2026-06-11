"use client";

import { Tag } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { CommonCodeListClient } from "@/components/admin/code/common-code/common-code-list-client";

export default function AdminCommonCodePage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_COMMON_CODE">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Tag size={14} /></div>
            Code - Common Code
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <CommonCodeListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
