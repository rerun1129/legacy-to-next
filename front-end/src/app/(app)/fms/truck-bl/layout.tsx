"use client";

import { AdminGuard } from "@/components/admin/admin-guard";

export default function TruckBlLayout({ children }: { children: React.ReactNode }) {
  return <AdminGuard requiredMenuCode="MENU_FMS_TRUCK_BL">{children}</AdminGuard>;
}
