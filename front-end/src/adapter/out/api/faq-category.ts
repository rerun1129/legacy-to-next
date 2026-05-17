import { z } from "zod";
import type { FaqCategoryPort } from "@/application/faq-category/ports";
import type {
  FaqCategoryRow,
  FaqCategoryDetail,
  CreateFaqCategoryRequestDto,
  UpdateFaqCategoryRequestDto,
} from "@/domain/faq-category";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/cms/faq-category";

// BE 응답 필드: faqCategoryId → id 로 매핑
const FAQ_CATEGORY_ROW_SCHEMA = z
  .object({
    faqCategoryId: z.number(),
    name: z.string(),
    sortOrder: z.number(),
    active: z.boolean(),
    deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
    updatedAt: z.string(),
  })
  .transform((v) => ({
    id: v.faqCategoryId,
    name: v.name,
    sortOrder: v.sortOrder,
    active: v.active,
    deletedAt: v.deletedAt,
    updatedAt: v.updatedAt,
  })) satisfies z.ZodType<FaqCategoryRow>;

const FAQ_CATEGORY_DETAIL_SCHEMA = z
  .object({
    faqCategoryId: z.number(),
    name: z.string(),
    sortOrder: z.number(),
    active: z.boolean(),
    deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
    createdAt: z.string(),
    updatedAt: z.string(),
    createdBy: z.string().nullable().optional().transform((v) => v ?? null),
    updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
  })
  .transform((v) => ({
    id: v.faqCategoryId,
    name: v.name,
    sortOrder: v.sortOrder,
    active: v.active,
    deletedAt: v.deletedAt,
    createdAt: v.createdAt,
    updatedAt: v.updatedAt,
    createdBy: v.createdBy,
    updatedBy: v.updatedBy,
  })) satisfies z.ZodType<FaqCategoryDetail>;

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

export const API_FAQ_CATEGORY_PORT: FaqCategoryPort = {
  async search(): Promise<FaqCategoryRow[]> {
    // 전체 목록 로드(좌 패널용). sort_order ASC, id ASC 정렬은 BE SSOT.
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ scope: "ALL", page: 0, size: 1000 }),
    });
    const parsed = apiResponse(pagedResult(FAQ_CATEGORY_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid faq-category search response: ${parsed.error.message}`);
    }
    return parsed.data.data.content as FaqCategoryRow[];
  },

  async getById(id: number): Promise<FaqCategoryDetail> {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(FAQ_CATEGORY_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid faq-category detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as FaqCategoryDetail;
  },

  async create(req: CreateFaqCategoryRequestDto): Promise<number> {
    const json = await adminFetchJson(`${BASE}`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid faq-category create response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateFaqCategoryRequestDto): Promise<void> {
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(req),
    });
  },

  async delete(id: number): Promise<void> {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },
};
