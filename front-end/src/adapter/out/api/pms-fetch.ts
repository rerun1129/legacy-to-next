import { PMS_API_URL } from "@/lib/api-base";
import { getAuthHeader, getSession, updateTokens, clearSession } from "@/lib/admin-session";
import { authUseCases } from "@/application/auth/use-cases";
import { ApiError, NotFoundError, ResponseParseError } from "./errors";

// 동시 다발 401 시 refresh 1회만 호출하도록 promise 재사용
let refreshPromise: Promise<void> | null = null;

async function performRefresh(): Promise<void> {
  if (refreshPromise) return refreshPromise;
  refreshPromise = (async () => {
    const session = getSession();
    if (!session) throw new ApiError("No session", 401);
    try {
      const r = await authUseCases.refresh(session.refreshToken);
      updateTokens(r.accessToken, r.refreshToken);
    } catch (e) {
      clearSession();
      if (typeof window !== "undefined") {
        window.location.replace("/login");
      }
      throw e;
    } finally {
      refreshPromise = null;
    }
  })();
  return refreshPromise;
}

async function fetchOnce(path: string, init?: RequestInit): Promise<Response> {
  const headers = new Headers(init?.headers);
  if (init?.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  for (const [k, v] of Object.entries(getAuthHeader())) headers.set(k, v);
  return fetch(`${PMS_API_URL}${path}`, { ...init, headers });
}

export async function pmsFetchJson(path: string, init?: RequestInit): Promise<unknown> {
  let res: Response;
  try {
    res = await fetchOnce(path, init);
  } catch (e) {
    // React Query가 queryKey 변경 시 signal을 abort함 — AbortError는 cancel로 인지하도록 그대로 throw
    if (e instanceof DOMException && e.name === "AbortError") throw e;
    throw new ApiError("Network error", undefined, e);
  }

  // 401 → refresh 1회 시도 후 재시도
  if (res.status === 401 && getSession()) {
    try {
      await performRefresh();
      res = await fetchOnce(path, init);
    } catch {
      // refresh 실패 → 이미 clearSession + redirect 처리됨
      throw new ApiError("Authentication failed", 401);
    }
  }

  const isProblem = res.headers.get("content-type")?.includes("application/problem+json");
  if (!res.ok) {
    if (isProblem) {
      const pd = await res.json().catch(() => null) as
        | { type?: string; title?: string; detail?: string; status?: number }
        | null;
      if (res.status === 404) throw new NotFoundError("resource", path);
      throw new ApiError(pd?.detail ?? pd?.title ?? `HTTP ${res.status}`, res.status, pd);
    }
    if (res.status === 404) throw new NotFoundError("resource", path);
    throw new ApiError(`HTTP ${res.status}`, res.status);
  }
  if (res.status === 204 || res.headers.get("content-length") === "0") return null;
  try {
    return await res.json();
  } catch (e) {
    throw new ResponseParseError("Failed to parse response JSON", e);
  }
}
