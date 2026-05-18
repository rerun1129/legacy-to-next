import { z } from "zod";
import type { NoticePort, NoticePageResult } from "@/application/notice/ports";
import type {
  NoticeRow,
  NoticeDetail,
  NoticeFilter,
  NoticeScope,
  CreateNoticeRequestDto,
  UpdateNoticeRequestDto,
} from "@/domain/notice";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/cms/notice";

const NOTICE_ROW_SCHEMA = z.object({
  id: z.number(),
  title: z.string(),
  pinned: z.boolean(),
  active: z.boolean(),
  publishedAt: z.string().nullable().optional().transform((v) => v ?? null),
  expiresAt: z.string().nullable().optional().transform((v) => v ?? null),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<NoticeRow>;

const NOTICE_DETAIL_SCHEMA = z.object({
  id: z.number(),
  title: z.string(),
  content: z.string(),
  pinned: z.boolean(),
  active: z.boolean(),
  publishedAt: z.string().nullable().optional().transform((v) => v ?? null),
  expiresAt: z.string().nullable().optional().transform((v) => v ?? null),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<NoticeDetail>;

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

function scopeForBackend(scope: NoticeScope): NoticeScope {
  return scope;
}

export const API_NOTICE_PORT: NoticePort = {
  async search(filter: NoticeFilter, page: number, size = 20): Promise<NoticePageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
      publishedOnly: filter.publishedOnly,
    };
    if (filter.title) body.title = filter.title;
    if (filter.pinned !== "ALL") body.pinned = filter.pinned === "PINNED";

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(NOTICE_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid notice search response: ${parsed.error.message}`);
    }
    const d = parsed.data.data;
    return {
      content: d.content as NoticeRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(NOTICE_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid notice detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as NoticeDetail;
  },

  async create(req: CreateNoticeRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid notice create response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateNoticeRequestDto) {
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
