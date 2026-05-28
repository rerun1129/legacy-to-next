import { z } from "zod";
import type { UserPermissionPresetPort } from "@/application/access/user-permission-preset/ports";
import type { UserPermissionPresetRef } from "@/domain/access/user-permission-preset";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/access/user-permission-preset";

const USER_PERMISSION_PRESET_REF_SCHEMA = z.object({
  id: z.number(),
  userId: z.number(),
  presetId: z.number(),
  presetCode: z.string(),
  presetName: z.string(),
  presetActive: z.boolean(),
}) satisfies z.ZodType<UserPermissionPresetRef>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const apiUserPermissionPresetPort: UserPermissionPresetPort = {
  async listByUser(userId: number): Promise<UserPermissionPresetRef[]> {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ userId }),
    });
    const parsed = apiResponse(z.array(USER_PERMISSION_PRESET_REF_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid user-permission-preset search response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async assign(userId: number, presetId: number): Promise<number> {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify({ userId, presetId }),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid user-permission-preset assign response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async revoke(id: number): Promise<void> {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },
};
