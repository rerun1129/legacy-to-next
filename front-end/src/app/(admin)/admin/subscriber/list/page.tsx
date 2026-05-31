"use client";

import { Users } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { SubscriberListClient } from "@/components/admin/subscriber/subscriber-list-client";

export default function AdminSubscriberListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_SUBSCRIBER_LIST">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Users size={14} /></div>
            Subscriber Management
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <SubscriberListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
