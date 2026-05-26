"use client";

import { Coins } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { CurrencyListClient } from "@/components/admin/code/currency/currency-list-client";

export default function AdminCurrencyListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_CURRENCY">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Coins size={14} /></div>
            Currency Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <CurrencyListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
