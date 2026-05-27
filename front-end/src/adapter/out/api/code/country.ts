import { z } from "zod";
import type { CountryPort, CountryPageResult } from "@/application/code/country/ports";
import type {
  CountryRow,
  CountryDetail,
  CountryFilter,
  CountryScope,
  CreateCountryRequestDto,
  UpdateCountryRequestDto,
} from "@/domain/code/country";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/code/country";

const COUNTRY_ROW_SCHEMA = z.object({
  id: z.number(),
  countryCode: z.string(),
  name: z.string(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<CountryRow>;

const COUNTRY_DETAIL_SCHEMA = z.object({
  id: z.number(),
  countryCode: z.string(),
  name: z.string(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<CountryDetail>;

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

function scopeForBackend(scope: CountryScope): CountryScope {
  return scope;
}

export const API_COUNTRY_PORT: CountryPort = {
  async search(filter: CountryFilter, page: number, size = 20): Promise<CountryPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.countryCode) body.countryCode = filter.countryCode;
    if (filter.name) body.name = filter.name;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(COUNTRY_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid country search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as CountryRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(COUNTRY_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid country detail response: ${parsed.error.message}`);
    return parsed.data.data as CountryDetail;
  },

  async create(req: CreateCountryRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid country create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateCountryRequestDto) {
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
};
