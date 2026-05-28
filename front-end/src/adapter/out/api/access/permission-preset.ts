import { z } from "zod";
import type { PermissionPresetPort } from "@/application/access/permission-preset/ports";
import type {
  PermissionPresetSummary,
  PermissionPreset,
  SearchPermissionPresetCriteria,
  CreatePermissionPresetCommand,
  UpdatePermissionPresetCommand,
  AssignAttributeValuesCommand,
} from "@/domain/access/permission-preset";
import { adminFetchJson } from "../admin-fetch";
import { ApiError, ResponseParseError } from "../errors";

const BASE = "/api/admin/access/permission-preset";

const ATTRIBUTE_VALUE_REF_SCHEMA = z.object({
  id: z.number(),
  attributeKey: z.string(),
  value: z.string(),
  label: z.string(),
});

const PRESET_SUMMARY_SCHEMA = z.object({
  id: z.number(),
  code: z.string(),
  name: z.string(),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  attributeValueIds: z.array(z.number()),
}) satisfies z.ZodType<PermissionPresetSummary>;

const PRESET_DETAIL_SCHEMA = z.object({
  id: z.number(),
  code: z.string(),
  name: z.string(),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  attributeValueIds: z.array(z.number()),
  attributeValues: z.array(ATTRIBUTE_VALUE_REF_SCHEMA),
}) satisfies z.ZodType<PermissionPreset>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_PERMISSION_PRESET_PORT: PermissionPresetPort = {
  async search(criteria: SearchPermissionPresetCriteria): Promise<PermissionPresetSummary[]> {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(criteria),
    });
    const parsed = apiResponse(z.array(PRESET_SUMMARY_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid permission-preset search response: ${parsed.error.message}`);
    }
    return parsed.data.data as PermissionPresetSummary[];
  },

  async getById(id: number): Promise<PermissionPreset> {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(PRESET_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid permission-preset detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as PermissionPreset;
  },

  async create(cmd: CreatePermissionPresetCommand): Promise<number> {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(cmd),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid permission-preset create response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async update(id: number, cmd: UpdatePermissionPresetCommand): Promise<void> {
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(cmd),
    });
  },

  async delete(id: number): Promise<void> {
    try {
      await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
    } catch (e) {
      // 409 = 부여된 user가 존재해 삭제 불가 (RESTRICT 정책)
      if (e instanceof ApiError && e.statusCode === 409) {
        throw new ApiError("PermissionPreset is in use, cannot delete", 409, e);
      }
      throw e;
    }
  },

  async assignAttributeValues(id: number, cmd: AssignAttributeValuesCommand): Promise<void> {
    await adminFetchJson(`${BASE}/${id}/attribute-values`, {
      method: "POST",
      body: JSON.stringify(cmd),
    });
  },
};
