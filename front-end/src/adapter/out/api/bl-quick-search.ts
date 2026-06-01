import { z } from "zod";
import type { BlQuickSearchPort } from "@/application/bl-quick-search/ports";
import type { BlQuickSearchFilters, BlQuickSearchItem } from "@/domain/bl-quick-search";
import { fetchJson } from "./utils";
import { ResponseParseError } from "./errors";

const BASE = "/api/bl/quick-search";

const BL_QUICK_SEARCH_ITEM_SCHEMA = z.object({
  id: z.number(),
  blType: z.enum(["HOUSE", "MASTER"]),
  blNo: z.string(),
  jobDiv: z.string(),
  bound: z.string(),
  shipperCode: z.string().nullable(),
  polCode: z.string().nullable(),
  podCode: z.string().nullable(),
  etd: z.string().nullable(),
  label: z.string(),
}) satisfies z.ZodType<BlQuickSearchItem>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_BL_QUICK_SEARCH_PORT: BlQuickSearchPort = {
  async autocomplete(
    q: string,
    filters: BlQuickSearchFilters,
    limit = 20
  ): Promise<BlQuickSearchItem[]> {
    // 빈 값/undefined 파라미터는 URLSearchParams에 포함하지 않음
    const params = new URLSearchParams();
    params.set("q", q);
    params.set("limit", String(limit));
    if (filters.jobDiv)        params.set("jobDiv",        filters.jobDiv);
    if (filters.bound)         params.set("bound",         filters.bound);
    if (filters.dateKind)      params.set("dateKind",      filters.dateKind);
    if (filters.dateFrom)      params.set("dateFrom",      filters.dateFrom);
    if (filters.dateTo)        params.set("dateTo",        filters.dateTo);
    if (filters.teamCode)      params.set("teamCode",      filters.teamCode);
    if (filters.operatorCode)  params.set("operatorCode",  filters.operatorCode);
    if (filters.salesManCode)  params.set("salesManCode",  filters.salesManCode);
    if (filters.polCode)       params.set("polCode",       filters.polCode);
    if (filters.podCode)       params.set("podCode",       filters.podCode);
    if (filters.partyKind)     params.set("partyKind",     filters.partyKind);
    if (filters.partyCode)     params.set("partyCode",     filters.partyCode);

    const json = await fetchJson(`${BASE}/autocomplete?${params}`);
    const parsed = apiResponse(z.array(BL_QUICK_SEARCH_ITEM_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid bl-quick-search autocomplete response: ${parsed.error.message}`
      );
    }
    return parsed.data.data;
  },
};
