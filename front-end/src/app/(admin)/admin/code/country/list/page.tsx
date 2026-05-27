"use client";

import { Globe } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { CountryListClient } from "@/components/admin/code/country/country-list-client";

export default function AdminCountryListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CODE_COUNTRY">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Globe size={14} /></div>
            Country Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <CountryListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
