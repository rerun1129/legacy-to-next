import { z } from "zod";
import type { MenuPort, MenuPageResult } from "@/application/access/menu/ports";
import type { MenuRow, MenuDetail, CreateMenuDto, UpdateMenuDto } from "@/domain/access/menu";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/access/menu";

const MENU_ROW_SCHEMA = z.object({
  id: z.number(),
  menuCode: z.string(),
  parentId: z.number().nullable().optional().transform((v) => v ?? null),
  path: z.string().nullable().optional().transform((v) => v ?? null),
  label: z.string(),
  labelEn: z.string().nullable().optional().transform((v) => v ?? null),
  icon: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  moduleCode: z.string(),
  updatedAt: z.string(),
}) satisfies z.ZodType<MenuRow>;

const MENU_DETAIL_SCHEMA = z.object({
  id: z.number(),
  menuCode: z.string(),
  parentId: z.number().nullable().optional().transform((v) => v ?? null),
  path: z.string().nullable().optional().transform((v) => v ?? null),
  label: z.string(),
  labelEn: z.string().nullable().optional().transform((v) => v ?? null),
  icon: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  moduleCode: z.string(),
  updatedAt: z.string(),
  createdAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<MenuDetail>;

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

export const API_MENU_PORT: MenuPort = {
  async search(page, size = 100): Promise<MenuPageResult> {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ page: page - 1, size }),
    });
    const parsed = apiResponse(pagedResult(MENU_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid menu search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return { content: d.content as MenuRow[], totalPages: d.totalPages, totalElements: d.totalElements, page: d.page + 1, size: d.size };
  },

  async getById(id) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(MENU_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid menu detail response: ${parsed.error.message}`);
    return parsed.data.data as MenuDetail;
  },

  async create(req: CreateMenuDto) {
    const json = await adminFetchJson(BASE, { method: "POST", body: JSON.stringify(req) });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid menu create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id, req: UpdateMenuDto) {
    await adminFetchJson(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(req) });
  },

  async delete(id) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },
};
