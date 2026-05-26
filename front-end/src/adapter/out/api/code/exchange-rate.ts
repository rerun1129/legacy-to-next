import { z } from "zod";
import type { ExchangeRatePort, ExchangeRatePageResult } from "@/application/code/exchange-rate/ports";
import type {
  ExchangeRateRow,
  ExchangeRateDetail,
  ExchangeRateFilter,
  ExchangeRateScope,
  CreateExchangeRateRequestDto,
  UpdateExchangeRateRequestDto,
} from "@/domain/code/exchange-rate";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/code/exchange-rate";

const EXCHANGE_RATE_ROW_SCHEMA = z.object({
  id: z.number(),
  baseCurrency: z.string(),
  targetCurrency: z.string(),
  rate: z.number(),
  name: z.string(),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<ExchangeRateRow>;

const EXCHANGE_RATE_DETAIL_SCHEMA = z.object({
  id: z.number(),
  baseCurrency: z.string(),
  targetCurrency: z.string(),
  rate: z.number(),
  name: z.string(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<ExchangeRateDetail>;

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

function scopeForBackend(scope: ExchangeRateScope): ExchangeRateScope {
  return scope;
}

export const API_EXCHANGE_RATE_PORT: ExchangeRatePort = {
  async search(filter: ExchangeRateFilter, page: number, size = 20): Promise<ExchangeRatePageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.baseCurrency) body.baseCurrency = filter.baseCurrency;
    if (filter.targetCurrency) body.targetCurrency = filter.targetCurrency;
    if (filter.name) body.name = filter.name;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(EXCHANGE_RATE_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid exchange-rate search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as ExchangeRateRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(EXCHANGE_RATE_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid exchange-rate detail response: ${parsed.error.message}`);
    return parsed.data.data as ExchangeRateDetail;
  },

  async create(req: CreateExchangeRateRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid exchange-rate create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateExchangeRateRequestDto) {
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
