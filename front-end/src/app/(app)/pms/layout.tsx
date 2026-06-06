"use client";

import { AdminGuard } from "@/components/admin/admin-guard";

/** PMS 모듈 공통 레이아웃 — MENU_PMS_PERFORMANCE 메뉴 접근 가드 */
export default function PmsLayout({ children }: { children: React.ReactNode }) {
  return <AdminGuard requiredMenuCode="MENU_PMS_PERFORMANCE">{children}</AdminGuard>;
}
