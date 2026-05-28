import { z } from "zod";
import type { AttributeDefinitionPort, AttributeDefinitionPageResult } from "@/application/access/attribute/ports";
import type {
  AttributeDefinitionRow,
  AttributeDefinitionDetail,
  CreateAttributeDefinitionDto,
  UpdateAttributeDefinitionDto,
  AttributeValueType,
  ModuleAttributeDto,
  SaveAttributeDefinitionChangesRequest,
  SaveChangesResult,
  AttributeAutocompleteItem,
} from "@/domain/access/attribute";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/access/attribute";

const ATTRIBUTE_VALUE_TYPE_SCHEMA = z.enum(["STRING", "NUMBER", "BOOLEAN", "ENUM"]) satisfies z.ZodType<AttributeValueType>;

const ATTRIBUTE_ROW_SCHEMA = z.object({
  attributeKey: z.string(),
  name: z.string(),
  valueType: ATTRIBUTE_VALUE_TYPE_SCHEMA,
  allowMulti: z.boolean(),
  active: z.boolean(),
  updatedAt: z.string(),
}) satisfies z.ZodType<AttributeDefinitionRow>;

const ATTRIBUTE_DETAIL_SCHEMA = z.object({
  attributeKey: z.string(),
  name: z.string(),
  valueType: ATTRIBUTE_VALUE_TYPE_SCHEMA,
  allowMulti: z.boolean(),
  active: z.boolean(),
  updatedAt: z.string(),
  createdAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<AttributeDefinitionDetail>;

const MODULE_ATTRIBUTE_VALUE_SCHEMA = z.object({
  value: z.string(),
  label: z.string(),
});

const MODULE_ATTRIBUTE_SCHEMA = z.object({
  attributeKey: z.string(),
  name: z.string(),
  valueType: ATTRIBUTE_VALUE_TYPE_SCHEMA,
  allowMulti: z.boolean(),
  values: z.array(MODULE_ATTRIBUTE_VALUE_SCHEMA),
}) satisfies z.ZodType<ModuleAttributeDto>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

const pagedResult = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({
    content: z.array(schema),
    totalElements: z.number(),
    totalPages: z.number(),
    page: z.number(),
    size: z.number(),
  });

export const API_ATTRIBUTE_PORT: AttributeDefinitionPort = {
  async search(page, size = 100): Promise<AttributeDefinitionPageResult> {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ page: page - 1, size }),
    });
    const parsed = apiResponse(pagedResult(ATTRIBUTE_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid attribute search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return { content: d.content as AttributeDefinitionRow[], totalPages: d.totalPages, totalElements: d.totalElements, page: d.page + 1, size: d.size };
  },

  async getById(attributeKey) {
    const json = await adminFetchJson(`${BASE}/${attributeKey}`);
    const parsed = apiResponse(ATTRIBUTE_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid attribute detail response: ${parsed.error.message}`);
    return parsed.data.data as AttributeDefinitionDetail;
  },

  async create(req: CreateAttributeDefinitionDto) {
    const json = await adminFetchJson(BASE, { method: "POST", body: JSON.stringify(req) });
    const parsed = apiResponse(z.object({ attributeKey: z.string() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid attribute create response: ${parsed.error.message}`);
    return parsed.data.data.attributeKey;
  },

  async update(attributeKey, req: UpdateAttributeDefinitionDto) {
    await adminFetchJson(`${BASE}/${attributeKey}`, { method: "PUT", body: JSON.stringify(req) });
  },

  async delete(attributeKey) {
    await adminFetchJson(`${BASE}/${attributeKey}`, { method: "DELETE" });
  },

  async deleteMany(keys) {
    // BE BulkDeleteByCodeRequest DTO 필드명이 codes이므로 keys를 codes로 매핑
    await adminFetchJson(`${BASE}/bulk`, {
      method: "DELETE",
      body: JSON.stringify({ codes: keys }),
    });
  },

  async getByModule(moduleCode) {
    const json = await adminFetchJson(`${BASE}/by-module/${encodeURIComponent(moduleCode)}`);
    const parsed = apiResponse(z.array(MODULE_ATTRIBUTE_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid module attributes response: ${parsed.error.message}`);
    return parsed.data.data as ModuleAttributeDto[];
  },

  async saveChanges(req: SaveAttributeDefinitionChangesRequest): Promise<SaveChangesResult> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(
      z.object({
        createdCount: z.number(),
        updatedCount: z.number(),
        deletedCount: z.number(),
      })
    ).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid attribute save-changes response: ${parsed.error.message}`);
    }
    return parsed.data.data as SaveChangesResult;
  },

  async autocomplete(query: string): Promise<AttributeAutocompleteItem[]> {
    const json = await adminFetchJson(
      `${BASE}/autocomplete?query=${encodeURIComponent(query)}`
    );
    const parsed = apiResponse(z.array(z.object({ code: z.string(), name: z.string() }))).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid attribute autocomplete response: ${parsed.error.message}`);
    }
    return parsed.data.data as AttributeAutocompleteItem[];
  },
};
