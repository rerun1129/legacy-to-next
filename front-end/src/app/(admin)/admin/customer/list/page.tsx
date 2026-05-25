"use client";

import { Building2 } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { CustomerListClient } from "@/components/admin/customer/customer-list-client";

export default function AdminCustomerListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CUSTOMER_LIST">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Building2 size={14} /></div>
            Customer Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <CustomerListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
