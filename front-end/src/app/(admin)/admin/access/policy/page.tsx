"use client";

import { ShieldCheck } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { AccessPolicyListClient } from "@/components/admin/access/policy-list-client";

export default function AdminAccessPolicyPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_ACCESS_POLICY">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><ShieldCheck size={14} /></div>
            Access - Policy
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <AccessPolicyListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
