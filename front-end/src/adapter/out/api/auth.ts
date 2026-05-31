import { z } from "zod";
import type { AuthPort, MeInfo, LoginResult, RefreshResult, AccessibleButtonInfo } from "@/application/auth/ports";
import { ADMIN_API_URL } from "@/lib/api-base";
import { getAuthHeader } from "@/lib/admin-session";
import { ApiError, ResponseParseError } from "./errors";

const ACCESSIBLE_BUTTON_SCHEMA = z.object({
  code: z.string(),
  label: z.string(),
  labelEn: z.string().nullish(),
}) satisfies z.ZodType<AccessibleButtonInfo>;

const ME_SCHEMA = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string().nullable().optional().transform((v) => v ?? null),
  attributes: z.record(z.string(), z.array(z.string())),
  accessibleMenus: z.array(z.string()),
  accessibleButtons: z.array(ACCESSIBLE_BUTTON_SCHEMA),
}) satisfies z.ZodType<MeInfo>;

const LOGIN_SCHEMA = z.object({
  accessToken: z.string(),
  refreshToken: z.string(),
  me: ME_SCHEMA,
}) satisfies z.ZodType<LoginResult>;

const REFRESH_SCHEMA = z.object({
  accessToken: z.string(),
  refreshToken: z.string(),
}) satisfies z.ZodType<RefreshResult>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

async function postJson(path: string, body: unknown, authHeader?: string): Promise<unknown> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (authHeader) headers.Authorization = authHeader;
  let res: Response;
  try {
    res = await fetch(`${ADMIN_API_URL}${path}`, {
      method: "POST",
      headers,
      body: JSON.stringify(body),
    });
  } catch (e) {
    throw new ApiError("Network error", undefined, e);
  }
  if (!res.ok) {
    const isProblem = res.headers.get("content-type")?.includes("application/problem+json")
      || res.headers.get("content-type")?.includes("application/json");
    if (isProblem) {
      const pd = await res.json().catch(() => null) as
        | { type?: string; title?: string; detail?: string; status?: number; errorCode?: string }
        | null;
      throw new ApiError(pd?.detail ?? pd?.title ?? `HTTP ${res.status}`, res.status, pd, pd?.errorCode, pd?.detail);
    }
    throw new ApiError(`HTTP ${res.status}`, res.status);
  }
  if (res.status === 204 || res.headers.get("content-length") === "0") return null;
  try {
    return await res.json();
  } catch (e) {
    throw new ResponseParseError("Failed to parse response JSON", e);
  }
}

export const API_AUTH_PORT: AuthPort = {
  async login(username, password): Promise<LoginResult> {
    const json = await postJson("/api/admin/auth/login", { username, password });
    const parsed = apiResponse(LOGIN_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid login response: ${parsed.error.message}`);
    return parsed.data.data as LoginResult;
  },

  async refresh(refreshToken): Promise<RefreshResult> {
    const json = await postJson("/api/admin/auth/refresh", { refreshToken });
    const parsed = apiResponse(REFRESH_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid refresh response: ${parsed.error.message}`);
    return parsed.data.data as RefreshResult;
  },

  async logout(refreshToken): Promise<void> {
    const headers = getAuthHeader();
    await postJson("/api/admin/auth/logout", { refreshToken }, headers.Authorization);
  },

  async me(): Promise<MeInfo> {
    const headers = getAuthHeader();
    if (!headers.Authorization) throw new ApiError("No access token", 401);
    let res: Response;
    try {
      res = await fetch(`${ADMIN_API_URL}/api/admin/auth/me`, {
        method: "GET",
        headers: { Authorization: headers.Authorization },
      });
    } catch (e) {
      throw new ApiError("Network error", undefined, e);
    }
    if (!res.ok) throw new ApiError(`HTTP ${res.status}`, res.status);
    const json = await res.json();
    const parsed = apiResponse(ME_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid me response: ${parsed.error.message}`);
    return parsed.data.data as MeInfo;
  },
};
