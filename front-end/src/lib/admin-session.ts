import type { AccessibleButtonInfo } from "@/application/auth/ports";

const STORAGE_KEY = "admin.session";

export interface AdminSession {
  accessToken: string;
  refreshToken: string;
  attributes: Record<string, string[]>;
  accessibleMenus: string[];
  accessibleButtons: AccessibleButtonInfo[];
}

export function getSession(): AdminSession | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as AdminSession;
    if (!parsed?.accessToken || !parsed?.refreshToken) return null;
    if (!Array.isArray(parsed.accessibleMenus)) return null;
    if (!Array.isArray(parsed.accessibleButtons)) return null;
    return parsed;
  } catch {
    return null;
  }
}

export function setSession(session: AdminSession): void {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  document.cookie = "fms.auth=1; path=/; SameSite=Lax";
}

export function updateTokens(accessToken: string, refreshToken: string): void {
  const current = getSession();
  if (!current) return;
  setSession({ ...current, accessToken, refreshToken });
}

export function clearSession(): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(STORAGE_KEY);
  document.cookie = "fms.auth=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}

export function getAuthHeader(): Record<string, string> {
  const s = getSession();
  return s ? { Authorization: `Bearer ${s.accessToken}` } : {};
}

export function hasMenuAccess(session: AdminSession | null, menuCode: string): boolean {
  if (!session) return false;
  return session.accessibleMenus.includes(menuCode);
}

export function hasButtonAccess(session: AdminSession | null, buttonCode: string): boolean {
  if (!session) return false;
  return session.accessibleButtons.some(b => b.code === buttonCode);
}

export function getButtonLabel(session: AdminSession | null, buttonCode: string): string | null {
  if (!session) return null;
  return session.accessibleButtons.find(b => b.code === buttonCode)?.label ?? null;
}

// login·guard 양측에서 공유하는 "이 세션으로 진입 가능한 첫 번째 라우트" 헬퍼
export function firstAccessibleRoute(session: AdminSession): string | null {
  const order: Array<[string, string]> = [
    ["MENU_ADMIN_CODE_LIST", "/admin/code/list"],
    ["MENU_ADMIN_USER_LIST", "/admin/user/list"],
    ["MENU_ADMIN_CUSTOMER_LIST", "/admin/customer/list"],
    ["MENU_ADMIN_CMS_NOTICE_LIST", "/admin/cms/notice/list"],
    ["MENU_FMS_HOUSE_BL_SEA_EXP_LIST", "/fms/house-bl/sea-exp/list"],
    ["MENU_FMS_HOUSE_BL_SEA_IMP_LIST", "/fms/house-bl/sea-imp/list"],
    ["MENU_FMS_HOUSE_BL_AIR_EXP_LIST", "/fms/house-bl/air-exp/list"],
    ["MENU_FMS_HOUSE_BL_AIR_IMP_LIST", "/fms/house-bl/air-imp/list"],
    ["MENU_FMS_MASTER_BL_SEA_EXP_LIST", "/fms/master-bl/sea-exp/list"],
    ["MENU_FMS_MASTER_BL_SEA_IMP_LIST", "/fms/master-bl/sea-imp/list"],
    ["MENU_FMS_MASTER_BL_AIR_EXP_LIST", "/fms/master-bl/air-exp/list"],
    ["MENU_FMS_MASTER_BL_AIR_IMP_LIST", "/fms/master-bl/air-imp/list"],
    ["MENU_FMS_TRUCK_BL_LIST", "/fms/truck-bl/list"],
    ["MENU_FMS_NON_BL_LIST", "/fms/non-bl/list"],
    // variant intermediate nodes (path=NULL in DB) — root fallback
    ["MENU_FMS_HOUSE_BL", "/fms/house-bl/sea-exp/list"],
    ["MENU_FMS_MASTER_BL", "/fms/master-bl/sea-exp/list"],
    ["MENU_FMS_TRUCK_BL", "/fms/truck-bl/list"],
    ["MENU_FMS_NON_BL", "/fms/non-bl/list"],
  ];
  for (const [code, route] of order) {
    if (hasMenuAccess(session, code)) return route;
  }
  return null;
}
