import { z } from "zod";
import type { CodeMasterPort, CodeMasterPageResult } from "@/application/code-master/ports";
import type {
  CodeMasterRow,
  CodeMasterDetail,
  CodeMasterFilter,
  CreateCodeMasterRequestDto,
  UpdateCodeMasterRequestDto,
} from "@/domain/code-master";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/code-master";

const CODE_MASTER_ROW_SCHEMA = z.object({
  id: z.number(),
  masterCode: z.string(),
  masterName: z.string(),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
}) satisfies z.ZodType<CodeMasterRow>;

const CODE_MASTER_DETAIL_SCHEMA = z.object({
  id: z.number(),
  masterCode: z.string(),
  masterName: z.string(),
  description: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
  createdAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<CodeMasterDetail>;

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

function activeForBackend(active: CodeMasterFilter["active"]): boolean | null {
  if (active === "ALL") return null;
  return active === "ACTIVE";
}

export const API_CODE_MASTER_PORT: CodeMasterPort = {
  async search(filter: CodeMasterFilter, page: number, size = 20): Promise<CodeMasterPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
    };
    if (filter.masterCode.trim()) body.masterCode = filter.masterCode.trim();
    if (filter.masterName.trim()) body.masterName = filter.masterName.trim();
    const activeFlag = activeForBackend(filter.active);
    if (activeFlag !== null) body.active = activeFlag;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(CODE_MASTER_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid code-master search response: ${parsed.error.message}`);
    }
    const d = parsed.data.data;
    return {
      content: d.content as CodeMasterRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(CODE_MASTER_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid code-master detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as CodeMasterDetail;
  },

  async create(req: CreateCodeMasterRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid code-master create response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateCodeMasterRequestDto) {
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
