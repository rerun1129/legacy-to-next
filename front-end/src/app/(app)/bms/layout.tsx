"use client";

import { AdminGuard } from "@/components/admin/admin-guard";

/** BMS 모듈 공통 레이아웃 — BMS_FINANCIAL 메뉴 접근 가드 */
export default function BmsLayout({ children }: { children: React.ReactNode }) {
  return <AdminGuard requiredMenuCode="MENU_BMS_FINANCIAL">{children}</AdminGuard>;
}
