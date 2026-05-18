import { z } from "zod";
import type { ModulePort, ModulePageResult } from "@/application/access/module/ports";
import type { ModuleRow, ModuleDetail, CreateModuleDto, UpdateModuleDto } from "@/domain/access/module";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/access/module";

const MODULE_ROW_SCHEMA = z.object({
  moduleCode: z.string(),
  name: z.string(),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
}) satisfies z.ZodType<ModuleRow>;

const MODULE_DETAIL_SCHEMA = z.object({
  moduleCode: z.string(),
  name: z.string(),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
  createdAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<ModuleDetail>;

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

export const API_MODULE_PORT: ModulePort = {
  async search(page, size = 100): Promise<ModulePageResult> {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ page: page - 1, size }),
    });
    const parsed = apiResponse(pagedResult(MODULE_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid module search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return { content: d.content as ModuleRow[], totalPages: d.totalPages, totalElements: d.totalElements, page: d.page + 1, size: d.size };
  },

  async getById(moduleCode) {
    const json = await adminFetchJson(`${BASE}/${moduleCode}`);
    const parsed = apiResponse(MODULE_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid module detail response: ${parsed.error.message}`);
    return parsed.data.data as ModuleDetail;
  },

  async create(req: CreateModuleDto) {
    const json = await adminFetchJson(BASE, { method: "POST", body: JSON.stringify(req) });
    const parsed = apiResponse(z.object({ moduleCode: z.string() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid module create response: ${parsed.error.message}`);
    return parsed.data.data.moduleCode;
  },

  async update(moduleCode, req: UpdateModuleDto) {
    await adminFetchJson(`${BASE}/${moduleCode}`, { method: "PUT", body: JSON.stringify(req) });
  },

  async delete(moduleCode) {
    await adminFetchJson(`${BASE}/${moduleCode}`, { method: "DELETE" });
  },
};
