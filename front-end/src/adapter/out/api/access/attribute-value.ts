import { z } from "zod";
import type { AttributeValuePort } from "@/application/access/attribute-value/ports";
import type {
  AttributeValueRow,
  CreateAttributeValueDto,
  UpdateAttributeValueDto,
  SaveAttributeValueChangesRequest,
} from "@/domain/access/attribute-value";
import type { SaveChangesResult } from "@/domain/access/attribute";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/access/attribute-value";

const ATTRIBUTE_VALUE_ROW_SCHEMA = z.object({
  id: z.number(),
  attributeKey: z.string(),
  value: z.string(),
  label: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
}) satisfies z.ZodType<AttributeValueRow>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_ATTRIBUTE_VALUE_PORT: AttributeValuePort = {
  async listByKey(attributeKey) {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ attributeKey, page: 0, size: 200 }),
    });
    const parsed = apiResponse(
      z.object({ content: z.array(ATTRIBUTE_VALUE_ROW_SCHEMA) })
    ).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid attribute-value list response: ${parsed.error.message}`);
    return parsed.data.data.content as AttributeValueRow[];
  },

  async listAll() {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ page: 0, size: 200 }),
    });
    const parsed = apiResponse(
      z.object({ content: z.array(ATTRIBUTE_VALUE_ROW_SCHEMA) })
    ).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid attribute-value list-all response: ${parsed.error.message}`);
    return parsed.data.data.content as AttributeValueRow[];
  },

  async create(req: CreateAttributeValueDto) {
    await adminFetchJson(BASE, { method: "POST", body: JSON.stringify(req) });
  },

  async update(attributeKey, value, req: UpdateAttributeValueDto) {
    await adminFetchJson(`${BASE}/${attributeKey}/${value}`, { method: "PUT", body: JSON.stringify(req) });
  },

  async delete(attributeKey, value) {
    await adminFetchJson(`${BASE}/${attributeKey}/${value}`, { method: "DELETE" });
  },

  async deleteMany(attributeKey, values) {
    // BE BulkDeleteByCodeRequest DTO 필드명이 codes이므로 values를 codes로 매핑
    await adminFetchJson(`${BASE}/${attributeKey}/bulk`, {
      method: "DELETE",
      body: JSON.stringify({ codes: values }),
    });
  },

  async saveChanges(req: SaveAttributeValueChangesRequest): Promise<SaveChangesResult> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = z
      .object({
        data: z.object({
          createdCount: z.number(),
          updatedCount: z.number(),
          deletedCount: z.number(),
        }),
        message: z.string().optional(),
      })
      .safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid attribute-value save-changes response: ${parsed.error.message}`);
    }
    return parsed.data.data as SaveChangesResult;
  },
};
