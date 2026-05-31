import { z } from "zod";
import type { SubscriberPort, SubscriberPageResult } from "@/application/subscriber/ports";
import type {
  SubscriberRow,
  SubscriberDetail,
  SubscriberFilter,
  SubscriberScope,
  CreateSubscriberRequestDto,
  UpdateSubscriberRequestDto,
  SaveSubscriberChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/subscriber";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/subscriber";

const SUBSCRIBER_ROW_SCHEMA = z.object({
  id: z.number(),
  subscriberCode: z.string(),
  name: z.string(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  businessNo: z.string().nullable().optional().transform((v) => v ?? null),
  representative: z.string().nullable().optional().transform((v) => v ?? null),
  phone: z.string().nullable().optional().transform((v) => v ?? null),
  email: z.string().nullable().optional().transform((v) => v ?? null),
  memo: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<SubscriberRow>;

const SUBSCRIBER_DETAIL_SCHEMA = z.object({
  id: z.number(),
  subscriberCode: z.string(),
  name: z.string(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  businessNo: z.string().nullable().optional().transform((v) => v ?? null),
  representative: z.string().nullable().optional().transform((v) => v ?? null),
  phone: z.string().nullable().optional().transform((v) => v ?? null),
  email: z.string().nullable().optional().transform((v) => v ?? null),
  memo: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<SubscriberDetail>;

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

function scopeForBackend(scope: SubscriberScope): SubscriberScope {
  return scope;
}

export const API_SUBSCRIBER_PORT: SubscriberPort = {
  async search(filter: SubscriberFilter, page: number, size = 20): Promise<SubscriberPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.subscriberCode) body.subscriberCode = filter.subscriberCode;
    if (filter.name) body.name = filter.name;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(SUBSCRIBER_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid subscriber search response: ${parsed.error.message}`);
    }
    const d = parsed.data.data;
    return {
      content: d.content as SubscriberRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async listAll(): Promise<SubscriberRow[]> {
    const body = { page: 0, size: 100, scope: "ACTIVE" };
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(SUBSCRIBER_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid subscriber list response: ${parsed.error.message}`);
    }
    return parsed.data.data.content as SubscriberRow[];
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(SUBSCRIBER_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid subscriber detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as SubscriberDetail;
  },

  async create(req: CreateSubscriberRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid subscriber create response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateSubscriberRequestDto) {
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(req),
    });
  },

  async delete(id: number) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },

  async saveChanges(req: SaveSubscriberChangesRequestDto): Promise<SaveChangesResultDto> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid subscriber save-changes response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async autocomplete(q: string, limit = 20): Promise<CodeBoxSuggestion[]> {
    const params = new URLSearchParams({ q, limit: String(limit) });
    const json = await adminFetchJson(`${BASE}/autocomplete?${params}`);
    const parsed = apiResponse(z.array(AUTOCOMPLETE_ITEM_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid subscriber autocomplete response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },
};
