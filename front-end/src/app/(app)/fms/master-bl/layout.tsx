"use client";

import { AdminGuard } from "@/components/admin/admin-guard";

export default function MasterBlLayout({ children }: { children: React.ReactNode }) {
  return <AdminGuard requiredMenuCode="MENU_FMS_MASTER_BL">{children}</AdminGuard>;
}
