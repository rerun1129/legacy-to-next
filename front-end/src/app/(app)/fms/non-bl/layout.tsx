"use client";

import { AdminGuard } from "@/components/admin/admin-guard";

export default function NonBlLayout({ children }: { children: React.ReactNode }) {
  return <AdminGuard requiredMenuCode="MENU_FMS_NON_BL">{children}</AdminGuard>;
}
