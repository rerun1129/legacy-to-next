import { z } from "zod";
import type { PortPort, PortPageResult } from "@/application/code/port/ports";
import type {
  PortRow,
  PortDetail,
  PortFilter,
  PortType,
  PortScope,
  CreatePortRequestDto,
  UpdatePortRequestDto,
  SavePortChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/port";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/code/port";

const PORT_TYPE_SCHEMA = z.enum(["SEA", "AIR"]) satisfies z.ZodType<PortType>;

const PORT_ROW_SCHEMA = z.object({
  id: z.number(),
  portCode: z.string(),
  name: z.string().nullable(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  countryCode: z.string(),
  portType: PORT_TYPE_SCHEMA,
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<PortRow>;

const PORT_DETAIL_SCHEMA = z.object({
  id: z.number(),
  portCode: z.string(),
  portType: PORT_TYPE_SCHEMA,
  name: z.string().nullable(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  countryCode: z.string(),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<PortDetail>;

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

function scopeForBackend(scope: PortScope): PortScope {
  return scope;
}

export const API_PORT_PORT: PortPort = {
  async search(filter: PortFilter, page: number, size = 20): Promise<PortPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.portCode) body.portCode = filter.portCode;
    if (filter.name) body.name = filter.name;
    if (filter.countryCode) body.countryCode = filter.countryCode;
    if (filter.portType !== "ALL") body.portType = filter.portType;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(PORT_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid port search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as PortRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(PORT_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid port detail response: ${parsed.error.message}`);
    return parsed.data.data as PortDetail;
  },

  async create(req: CreatePortRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid port create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdatePortRequestDto) {
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

  async saveChanges(req: SavePortChangesRequestDto): Promise<SaveChangesResultDto> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid port save-changes response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async autocomplete(q: string, limit = 20): Promise<CodeBoxSuggestion[]> {
    const params = new URLSearchParams({ q, limit: String(limit) });
    const json = await adminFetchJson(`${BASE}/autocomplete?${params}`);
    const parsed = apiResponse(z.array(AUTOCOMPLETE_ITEM_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid port autocomplete response: ${parsed.error.message}`);
    return parsed.data.data;
  },
};
