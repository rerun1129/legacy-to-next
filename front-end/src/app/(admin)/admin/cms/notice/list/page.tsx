"use client";

import { Megaphone } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { NoticeListClient } from "@/components/admin/cms/notice/notice-list-client";

export default function AdminNoticeListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_ADMIN_CMS_NOTICE_LIST">
      <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Megaphone size={14} /></div>
            Notice
          </div>
        </div>
        <div style={{ padding: 16, display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
          <NoticeListClient />
        </div>
      </div>
    </AdminGuard>
  );
}
