import { z } from "zod";
import type { TermsPort, TermsPageResult } from "@/application/terms/ports";
import type {
  TermsRow,
  TermsDetail,
  TermsFilter,
  CreateTermsRequestDto,
  UpdateTermsRequestDto,
} from "@/domain/terms";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/cms/terms";

const TERMS_TYPE_SCHEMA = z.enum(["TOS", "PRIVACY", "MARKETING"]);

const TERMS_ROW_SCHEMA = z.object({
  id: z.number(),
  type: TERMS_TYPE_SCHEMA,
  version: z.number(),
  effectiveAt: z.string(),
  summary: z.string().nullable().optional().transform((v) => v ?? null),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<TermsRow>;

const TERMS_DETAIL_SCHEMA = z.object({
  id: z.number(),
  type: TERMS_TYPE_SCHEMA,
  version: z.number(),
  effectiveAt: z.string(),
  content: z.string(),
  summary: z.string().nullable().optional().transform((v) => v ?? null),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<TermsDetail>;

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

export const API_TERMS_PORT: TermsPort = {
  async search(filter: TermsFilter, page: number, size = 20): Promise<TermsPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: filter.scope,
    };
    if (filter.type !== "ALL") body.type = filter.type;
    if (filter.version !== "") body.version = filter.version;
    if (filter.summary) body.summary = filter.summary;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(TERMS_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid terms search response: ${parsed.error.message}`);
    }
    const d = parsed.data.data;
    return {
      content: d.content as TermsRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/get/${id}`);
    const parsed = apiResponse(TERMS_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid terms detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as TermsDetail;
  },

  async create(req: CreateTermsRequestDto) {
    const json = await adminFetchJson(`${BASE}/post`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid terms create response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateTermsRequestDto) {
    await adminFetchJson(`${BASE}/put/${id}`, {
      method: "PUT",
      body: JSON.stringify(req),
    });
  },

  async delete(id: number) {
    await adminFetchJson(`${BASE}/delete/${id}`, { method: "DELETE" });
  },
};
