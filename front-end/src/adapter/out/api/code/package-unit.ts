import { z } from "zod";
import type { PackageUnitPort, PackageUnitPageResult } from "@/application/code/package-unit/ports";
import type {
  PackageUnitRow,
  PackageUnitDetail,
  PackageUnitFilter,
  PackageUnitScope,
  CreatePackageUnitRequestDto,
  UpdatePackageUnitRequestDto,
  SavePackageUnitChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/package-unit";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/code/package";

const PACKAGE_UNIT_ROW_SCHEMA = z.object({
  id: z.number(),
  packageCode: z.string(),
  name: z.string().nullable(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<PackageUnitRow>;

const PACKAGE_UNIT_DETAIL_SCHEMA = z.object({
  id: z.number(),
  packageCode: z.string(),
  name: z.string().nullable(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<PackageUnitDetail>;

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

function scopeForBackend(scope: PackageUnitScope): PackageUnitScope {
  return scope;
}

export const API_PACKAGE_UNIT_PORT: PackageUnitPort = {
  async search(filter: PackageUnitFilter, page: number, size = 20): Promise<PackageUnitPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.packageCode) body.packageCode = filter.packageCode;
    if (filter.name) body.name = filter.name;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(PACKAGE_UNIT_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid package-unit search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as PackageUnitRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(PACKAGE_UNIT_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid package-unit detail response: ${parsed.error.message}`);
    return parsed.data.data as PackageUnitDetail;
  },

  async create(req: CreatePackageUnitRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid package-unit create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdatePackageUnitRequestDto) {
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

  async saveChanges(req: SavePackageUnitChangesRequestDto): Promise<SaveChangesResultDto> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid package-unit save-changes response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async autocomplete(q: string, limit = 20): Promise<CodeBoxSuggestion[]> {
    const params = new URLSearchParams({ q, limit: String(limit) });
    const json = await adminFetchJson(`${BASE}/autocomplete?${params}`);
    const parsed = apiResponse(z.array(AUTOCOMPLETE_ITEM_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid package-unit autocomplete response: ${parsed.error.message}`);
    return parsed.data.data;
  },
};
