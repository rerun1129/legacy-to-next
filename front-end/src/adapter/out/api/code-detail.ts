import { z } from "zod";
import type { CodeDetailPort, CodeDetailPageResult } from "@/application/code-detail/ports";
import type {
  CodeDetailRow,
  CodeDetailDetail,
  CreateCodeDetailRequestDto,
  UpdateCodeDetailRequestDto,
  SaveCodeDetailChangesRequest,
  SaveChangesResult,
} from "@/domain/code-detail";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/code-detail";

const CODE_DETAIL_ROW_SCHEMA = z.object({
  id: z.number(),
  masterId: z.number(),
  codeValue: z.string(),
  codeLabel: z.string(),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
}) satisfies z.ZodType<CodeDetailRow>;

const CODE_DETAIL_DETAIL_SCHEMA = z.object({
  id: z.number(),
  masterId: z.number(),
  codeValue: z.string(),
  codeLabel: z.string(),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
  remark: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<CodeDetailDetail>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

const SAVE_CHANGES_RESULT_SCHEMA = z.object({
  createdCount: z.number(),
  updatedCount: z.number(),
  deletedCount: z.number(),
});

const pagedResult = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({
    content: z.array(schema),
    totalElements: z.number(),
    totalPages: z.number(),
    page: z.number(),
    size: z.number(),
  });

export const API_CODE_DETAIL_PORT: CodeDetailPort = {
  async search(
    masterId: number,
    page: number,
    size = 50,
  ): Promise<CodeDetailPageResult> {
    const body: Record<string, unknown> = {
      masterId,
      page: page - 1,
      size,
    };

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(CODE_DETAIL_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid code-detail search response: ${parsed.error.message}`);
    }
    const d = parsed.data.data;
    return {
      content: d.content as CodeDetailRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(CODE_DETAIL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid code-detail detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as CodeDetailDetail;
  },

  async create(req: CreateCodeDetailRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid code-detail create response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateCodeDetailRequestDto) {
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

  async saveChanges(req: SaveCodeDetailChangesRequest): Promise<SaveChangesResult> {
    // BE CreateCodeDetailRequest.masterId 에 @NotNull + @Valid cascade 가 있어,
    // creates 각 항목에도 상위 masterId 를 주입해야 검증을 통과한다.
    const body = {
      masterId: req.masterId,
      creates: req.creates.map((c) => ({ ...c, masterId: req.masterId })),
      updates: req.updates,
      deleteIds: req.deleteIds,
    };
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid code-detail save-changes response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },
};
