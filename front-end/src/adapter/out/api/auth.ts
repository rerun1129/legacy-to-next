import { z } from "zod";
import type { AuthPort, MeInfo } from "@/application/auth/ports";
import { ADMIN_API_URL } from "@/lib/api-base";
import { ApiError, ResponseParseError } from "./errors";

const ME_SCHEMA = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string().nullable().optional().transform((v) => v ?? null),
  role: z.enum(["ADMIN", "USER"]),
  permissions: z.array(z.enum(["CODE_MANAGE", "USER_MANAGE", "PARTNER_MANAGE", "CMS_MANAGE"])),
}) satisfies z.ZodType<MeInfo>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_AUTH_PORT: AuthPort = {
  async me(authHeader): Promise<MeInfo> {
    let res: Response;
    try {
      res = await fetch(`${ADMIN_API_URL}/api/admin/auth/me`, {
        method: "GET",
        headers: { Authorization: authHeader },
      });
    } catch (e) {
      throw new ApiError("Network error", undefined, e);
    }
    if (!res.ok) {
      throw new ApiError(`HTTP ${res.status}`, res.status);
    }
    const json = await res.json();
    const parsed = apiResponse(ME_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid me response: ${parsed.error.message}`);
    return parsed.data.data as MeInfo;
  },
};
