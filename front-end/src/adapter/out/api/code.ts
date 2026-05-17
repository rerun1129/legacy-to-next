import { z } from "zod";
import type { CodePort, CodePageResult } from "@/application/code/ports";
import type {
  CodeRow,
  CodeDetail,
  CodeFilter,
  CreateCodeRequestDto,
  UpdateCodeRequestDto,
} from "@/domain/code";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/code";

const CODE_ROW_SCHEMA = z.object({
  id: z.number(),
  codeGroup: z.string(),
  codeValue: z.string(),
  codeLabel: z.string(),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
}) satisfies z.ZodType<CodeRow>;

const CODE_DETAIL_SCHEMA = z.object({
  id: z.number(),
  codeGroup: z.string(),
  codeValue: z.string(),
  codeLabel: z.string(),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  remark: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<CodeDetail>;

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

function activeForBackend(active: CodeFilter["active"]): boolean | null {
  if (active === "ALL") return null;
  return active === "ACTIVE";
}

export const API_CODE_PORT: CodePort = {
  async search(filter, page, size = 20): Promise<CodePageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
    };
    if (filter.codeGroup) body.codeGroup = filter.codeGroup;
    if (filter.codeValue) body.codeValue = filter.codeValue;
    if (filter.codeLabel) body.codeLabel = filter.codeLabel;
    const activeFlag = activeForBackend(filter.active);
    if (activeFlag !== null) body.active = activeFlag;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(CODE_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid code search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as CodeRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(CODE_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid code detail response: ${parsed.error.message}`);
    return parsed.data.data as CodeDetail;
  },

  async create(req: CreateCodeRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid code create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id, req: UpdateCodeRequestDto) {
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(req),
    });
  },

  async delete(id) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },
};
