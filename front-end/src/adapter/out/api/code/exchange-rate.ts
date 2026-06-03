import { z } from "zod";
import type { ExchangeRatePort, ExchangeRatePageResult } from "@/application/code/exchange-rate/ports";
import type {
  ExchangeRateRow,
  ExchangeRateDetail,
  ExchangeRateFilter,
  ExchangeRateValue,
  ExchangeRateScope,
  CreateExchangeRateRequestDto,
  UpdateExchangeRateRequestDto,
  SaveExchangeRateChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/exchange-rate";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/code/exchange-rate";

/** 환율 자동완성의 기준 통화. BE 계약 고정값이므로 상수로 분리. */
export const BASE_CURRENCY = "KRW";

const EXCHANGE_RATE_ROW_SCHEMA = z.object({
  id: z.number(),
  fromCurrencyCode: z.string(),
  toCurrencyCode: z.string(),
  exchangeDate: z.string().nullable().optional().transform((v) => v ?? null),
  cashSellExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  cashBuyExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  wireSendExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  wireReceiveExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  standardExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  name: z.string().nullable(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<ExchangeRateRow>;

const EXCHANGE_RATE_DETAIL_SCHEMA = z.object({
  id: z.number(),
  fromCurrencyCode: z.string(),
  toCurrencyCode: z.string(),
  name: z.string().nullable(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  exchangeDate: z.string().nullable().optional().transform((v) => v ?? null),
  cashSellExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  cashBuyExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  wireSendExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  wireReceiveExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
  standardExchangeRate: z.number().nullable().optional().transform((v) => v ?? null),
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

const SAVE_CHANGES_RESULT_SCHEMA = z.object({
  createdCount: z.number(),
  updatedCount: z.number(),
  deletedCount: z.number(),
});

const AUTOCOMPLETE_ITEM_SCHEMA = z.object({
  code: z.string(),
  name: z.string().nullable().transform((v) => v ?? ""),
}) satisfies z.ZodType<CodeBoxSuggestion>;

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
    if (filter.fromCurrencyCode) body.fromCurrencyCode = filter.fromCurrencyCode;
    if (filter.toCurrencyCode) body.toCurrencyCode = filter.toCurrencyCode;
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

  async saveChanges(req: SaveExchangeRateChangesRequestDto): Promise<SaveChangesResultDto> {
    const json = await adminFetchJson(`${BASE}/save-changes`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid exchange-rate save-changes response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async autocomplete(q: string, limit = 20): Promise<CodeBoxSuggestion[]> {
    const params = new URLSearchParams({ q, limit: String(limit) });
    const json = await adminFetchJson(`${BASE}/autocomplete?${params}`);
    const parsed = apiResponse(z.array(AUTOCOMPLETE_ITEM_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid exchange-rate autocomplete response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async findRatesByDateCurrency(exchangeDate: string, fromCurrencyCode: string, toCurrencyCode: string): Promise<ExchangeRateValue[]> {
    const params = new URLSearchParams({ exchangeDate, fromCurrencyCode, toCurrencyCode });
    const json = await adminFetchJson(`${BASE}/rates?${params}`);
    const parsed = apiResponse(z.array(z.object({ kind: z.string(), rate: z.number() }))).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid exchange-rate rates response: ${parsed.error.message}`);
    return parsed.data.data;
  },
};
