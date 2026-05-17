import type { Permission } from "@/domain/permission";

const STORAGE_KEY = "admin.session";

export interface AdminSession {
  accessToken: string;
  refreshToken: string;
  role: "ADMIN" | "USER";
  permissions: Permission[];
}

export function getSession(): AdminSession | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as AdminSession;
    if (!parsed?.accessToken || !parsed?.refreshToken) return null;
    if (parsed.role !== "ADMIN" && parsed.role !== "USER") return null;
    if (!Array.isArray(parsed.permissions)) return null;
    return parsed;
  } catch {
    return null;
  }
}

export function setSession(session: AdminSession): void {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function updateTokens(accessToken: string, refreshToken: string): void {
  const current = getSession();
  if (!current) return;
  setSession({ ...current, accessToken, refreshToken });
}

export function clearSession(): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(STORAGE_KEY);
}

export function getAuthHeader(): Record<string, string> {
  const s = getSession();
  return s ? { Authorization: `Bearer ${s.accessToken}` } : {};
}

export function hasPermission(session: AdminSession | null, permission: Permission): boolean {
  if (!session) return false;
  if (session.role === "ADMIN") return true;
  return session.permissions.includes(permission);
}

// login·guard 양측에서 공유하는 "이 세션으로 진입 가능한 첫 번째 라우트" 헬퍼
export function firstAccessibleRoute(session: AdminSession): string | null {
  if (session.role === "ADMIN") return "/admin/code/list";
  if (hasPermission(session, "CODE_MANAGE")) return "/admin/code/list";
  if (hasPermission(session, "USER_MANAGE")) return "/admin/user/list";
  return null;
}
