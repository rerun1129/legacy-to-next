"use client";

import { AdminGuard } from "@/components/admin/admin-guard";
import { CustomerListClient } from "@/components/admin/customer/customer-list-client";

export default function AdminCustomerListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CUSTOMER_LIST">
      <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
        <h1 style={{ fontSize: "var(--fs-lg)", fontWeight: 600, marginBottom: 12 }}>고객 관리</h1>
        <CustomerListClient />
      </div>
    </AdminGuard>
  );
}
