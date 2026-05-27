import { z } from "zod";
import type { FreightPort, FreightPageResult } from "@/application/code/freight/ports";
import type {
  FreightRow,
  FreightDetail,
  FreightFilter,
  FreightGroup,
  FreightScope,
  CreateFreightRequestDto,
  UpdateFreightRequestDto,
  SaveFreightChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/freight";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/code/freight";

const FREIGHT_GROUP_SCHEMA = z.enum(["OTHER", "FREIGHT", "SURCHARGE", "WHARFAGE"]) satisfies z.ZodType<FreightGroup>;

const FREIGHT_ROW_SCHEMA = z.object({
  id: z.number(),
  freightCode: z.string(),
  name: z.string().nullable(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  freightUnit: z.string().nullable().optional().transform((v) => v ?? null),
  freightGroup: FREIGHT_GROUP_SCHEMA.nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<FreightRow>;

const FREIGHT_DETAIL_SCHEMA = z.object({
  id: z.number(),
  freightCode: z.string(),
  name: z.string().nullable(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  freightUnit: z.string().nullable().optional().transform((v) => v ?? null),
  freightGroup: FREIGHT_GROUP_SCHEMA.nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<FreightDetail>;

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

const SAVE_CHANGES_RESULT_SCHEMA = z.object({
  createdCount: z.number(),
  updatedCount: z.number(),
  deletedCount: z.number(),
});

const AUTOCOMPLETE_ITEM_SCHEMA = z.object({
  code: z.string(),
  name: z.string().nullable().transform((v) => v ?? ""),
}) satisfies z.ZodType<CodeBoxSuggestion>;

function scopeForBackend(scope: FreightScope): FreightScope {
  return scope;
}

export const API_FREIGHT_PORT: FreightPort = {
  async search(filter: FreightFilter, page: number, size = 20): Promise<FreightPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.freightCode) body.freightCode = filter.freightCode;
    if (filter.name) body.name = filter.name;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(FREIGHT_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid freight search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as FreightRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(FREIGHT_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid freight detail response: ${parsed.error.message}`);
    return parsed.data.data as FreightDetail;
  },

  async create(req: CreateFreightRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid freight create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateFreightRequestDto) {
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(req),
    });
  },

  async delete(id: number) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },

  async deleteMany(ids: number[]) {
    await adminFetchJson(`${BASE}/bulk`, {
      method: "DELETE",
      body: JSON.stringify({ ids }),
    });
  },

  async saveChanges(req: SaveFreightChangesRequestDto): Promise<SaveChangesResultDto> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid freight save-changes response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async autocomplete(q: string, limit = 20): Promise<CodeBoxSuggestion[]> {
    const params = new URLSearchParams({ q, limit: String(limit) });
    const json = await adminFetchJson(`${BASE}/autocomplete?${params}`);
    const parsed = apiResponse(z.array(AUTOCOMPLETE_ITEM_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid freight autocomplete response: ${parsed.error.message}`);
    return parsed.data.data;
  },
};
