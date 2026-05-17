const STORAGE_KEY = "admin.session";

export interface AdminSession {
  authHeader: string; // "Basic xxx"
  role: "ADMIN";
}

export function getSession(): AdminSession | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as AdminSession;
    if (!parsed?.authHeader || parsed.role !== "ADMIN") return null;
    return parsed;
  } catch {
    return null;
  }
}

export function setSession(session: AdminSession): void {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function clearSession(): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(STORAGE_KEY);
}

export function getAuthHeader(): Record<string, string> {
  const s = getSession();
  return s ? { Authorization: s.authHeader } : {};
}
