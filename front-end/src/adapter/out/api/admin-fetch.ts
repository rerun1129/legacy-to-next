import { ADMIN_API_URL } from "@/lib/api-base";
import { getAuthHeader } from "@/lib/admin-session";
import { ApiError, NotFoundError, ResponseParseError } from "./errors";

export async function adminFetchJson(path: string, init?: RequestInit): Promise<unknown> {
  const headers = new Headers(init?.headers);
  if (init?.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  for (const [k, v] of Object.entries(getAuthHeader())) headers.set(k, v);

  let res: Response;
  try {
    res = await fetch(`${ADMIN_API_URL}${path}`, { ...init, headers });
  } catch (e) {
    throw new ApiError("Network error", undefined, e);
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
