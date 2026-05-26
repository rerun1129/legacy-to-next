"use client";

import { AdminGuard } from "@/components/admin/admin-guard";

export default function HouseBlLayout({ children }: { children: React.ReactNode }) {
  return <AdminGuard requiredMenuCode="MENU_FMS_HOUSE_BL">{children}</AdminGuard>;
}
