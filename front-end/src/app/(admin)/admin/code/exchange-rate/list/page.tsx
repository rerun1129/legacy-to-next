"use client";

import { ArrowLeftRight } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { ExchangeRateListClient } from "@/components/admin/code/exchange-rate/exchange-rate-list-client";

export default function AdminExchangeRateListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_EXCHANGE_RATE">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><ArrowLeftRight size={14} /></div>
            Exchange Rate Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <ExchangeRateListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
