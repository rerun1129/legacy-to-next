import { z } from "zod";
import type { CommonCodePort } from "@/application/common-code/ports";
import type {
  CommonCodeGroupRow,
  CommonCodeRow,
  SaveCommonCodeChangesRequest,
  SaveChangesResult,
} from "@/domain/common-code";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/common-code";

const COMMON_CODE_GROUP_SCHEMA = z.object({
  id: z.number(),
  groupCode: z.string(),
  sourceModule: z.string(),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
}) satisfies z.ZodType<CommonCodeGroupRow>;

const COMMON_CODE_ROW_SCHEMA = z.object({
  id: z.number(),
  groupCode: z.string(),
  code: z.string(),
  label: z.string(),
  labelKo: z.string(),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
}) satisfies z.ZodType<CommonCodeRow>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

const SAVE_CHANGES_RESULT_SCHEMA = z.object({
  createdCount: z.number(),
  updatedCount: z.number(),
  deletedCount: z.number(),
});

export const API_COMMON_CODE_PORT: CommonCodePort = {
  async listGroups(): Promise<CommonCodeGroupRow[]> {
    const json = await adminFetchJson(`${BASE}/groups`);
    const parsed = apiResponse(z.array(COMMON_CODE_GROUP_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid common-code groups response: ${parsed.error.message}`);
    }
    return parsed.data.data as CommonCodeGroupRow[];
  },

  async listByGroup(groupCode: string): Promise<CommonCodeRow[]> {
    const url = `${BASE}?group=${encodeURIComponent(groupCode)}`;
    const json = await adminFetchJson(url);
    const parsed = apiResponse(z.array(COMMON_CODE_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid common-code list response: ${parsed.error.message}`);
    }
    return parsed.data.data as CommonCodeRow[];
  },

  async saveChanges(req: SaveCommonCodeChangesRequest): Promise<SaveChangesResult> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid common-code save-changes response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },
};
