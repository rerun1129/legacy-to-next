import { z } from "zod";
import type { ButtonPort, ButtonPageResult } from "@/application/access/button/ports";
import type {
  ButtonRow,
  ButtonDetail,
  SaveButtonChangesRequest,
  SaveChangesResult,
  ButtonAutocompleteItem,
  ButtonActionType,
} from "@/domain/access/button";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/access/button";

const BUTTON_ACTION_TYPE_SCHEMA = z.enum([
  "CREATE",
  "UPDATE",
  "DELETE",
  "EXPORT",
  "CUSTOM",
]) satisfies z.ZodType<ButtonActionType>;

const BUTTON_ROW_SCHEMA = z.object({
  id: z.number(),
  menuId: z.number(),
  buttonCode: z.string(),
  label: z.string(),
  actionType: BUTTON_ACTION_TYPE_SCHEMA,
  apiMethod: z.string().nullable().optional().transform((v) => v ?? null),
  apiPath: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
}) satisfies z.ZodType<ButtonRow>;

const BUTTON_DETAIL_SCHEMA = z.object({
  id: z.number(),
  menuId: z.number(),
  buttonCode: z.string(),
  label: z.string(),
  actionType: BUTTON_ACTION_TYPE_SCHEMA,
  apiMethod: z.string().nullable().optional().transform((v) => v ?? null),
  apiPath: z.string().nullable().optional().transform((v) => v ?? null),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  updatedAt: z.string(),
  createdAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<ButtonDetail>;

const SAVE_CHANGES_RESULT_SCHEMA = z.object({
  createdCount: z.number(),
  updatedCount: z.number(),
  deletedCount: z.number(),
}) satisfies z.ZodType<SaveChangesResult>;

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

export const API_BUTTON_PORT: ButtonPort = {
  async search(page, size = 100): Promise<ButtonPageResult> {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ page: page - 1, size }),
    });
    const parsed = apiResponse(pagedResult(BUTTON_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid button search response: ${parsed.error.message}`);
    }
    const d = parsed.data.data;
    return {
      content: d.content as ButtonRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(BUTTON_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid button detail response: ${parsed.error.message}`);
    }
    return parsed.data.data as ButtonDetail;
  },

  async saveChanges(req: SaveButtonChangesRequest): Promise<SaveChangesResult> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid button save-changes response: ${parsed.error.message}`);
    }
    return parsed.data.data as SaveChangesResult;
  },

  async autocomplete(query: string): Promise<ButtonAutocompleteItem[]> {
    const json = await adminFetchJson(
      `${BASE}/autocomplete?query=${encodeURIComponent(query)}`
    );
    const parsed = apiResponse(
      z.array(z.object({ code: z.string(), name: z.string() }))
    ).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid button autocomplete response: ${parsed.error.message}`);
    }
    return parsed.data.data as ButtonAutocompleteItem[];
  },
};
