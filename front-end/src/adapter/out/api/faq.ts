import { z } from "zod";
import type { FaqPort, FaqPageResult } from "@/application/faq/ports";
import type {
  FaqRow,
  FaqDetail,
  FaqFilter,
  CreateFaqRequestDto,
  UpdateFaqRequestDto,
} from "@/domain/faq";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/cms/faq";

// BE 응답 필드: faqId → id 로 매핑
const FAQ_ROW_SCHEMA = z
  .object({
    faqId: z.number(),
    faqCategoryId: z.number(),
    question: z.string(),
    sortOrder: z.number(),
    active: z.boolean(),
    deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
    updatedAt: z.string(),
  })
  .transform((v) => ({
    id: v.faqId,
    faqCategoryId: v.faqCategoryId,
    question: v.question,
    sortOrder: v.sortOrder,
    active: v.active,
    deletedAt: v.deletedAt,
    updatedAt: v.updatedAt,
  })) satisfies z.ZodType<FaqRow>;

const FAQ_DETAIL_SCHEMA = z
  .object({
    faqId: z.number(),
    faqCategoryId: z.number(),
    question: z.string(),
    answer: z.string(),
    sortOrder: z.number(),
    active: z.boolean(),
    deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
    createdAt: z.string(),
    updatedAt: z.string(),
    createdBy: z.string().nullable().optional().transform((v) => v ?? null),
    updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
  })
  .transform((v) => ({
    id: v.faqId,
    faqCategoryId: v.faqCategoryId,
    question: v.question,
    answer: v.answer,
    sortOrder: v.sortOrder,
    active: v.active,
    deletedAt: v.deletedAt,
    createdAt: v.createdAt,
    updatedAt: v.updatedAt,
    createdBy: v.createdBy,
    updatedBy: v.updatedBy,
  })) satisfies z.ZodType<FaqDetail>;

const pagedResult = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({
    content: z.array(schema),
    totalElements: z.number(),
    totalPages: z.number(),
    page: z.number(),
    size: z.number(),
  });

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_FAQ_PORT: FaqPort = {
  async search(filter: FaqFilter, page: number, size = 20): Promise<FaqPageResult> {
    const body: Record<string, unknown> = {
      scope: filter.scope,
      page: page - 1,
      size,
    };
    if (filter.faqCategoryId !== null) body.faqCategoryId = filter.faqCategoryId;
    if (filter.question) body.question = filter.question;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(FAQ_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid faq search response: ${parsed.error.message}`);
    }
    const d = parsed.data.data;
    return {
      content: d.content as FaqRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number): Promise<FaqDetail> {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(FAQ_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid faq detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as FaqDetail;
  },

  async create(req: CreateFaqRequestDto): Promise<number> {
    const json = await adminFetchJson(`${BASE}`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid faq create response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateFaqRequestDto): Promise<void> {
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(req),
    });
  },

  async delete(id: number): Promise<void> {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },
};
