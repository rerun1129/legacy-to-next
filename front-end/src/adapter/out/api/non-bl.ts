import { z } from 'zod';
import type { NonBlPort, NonBlPageResult } from '@/application/non-bl/ports';
import type { NonBlRow, NonBlFilter } from '@/domain/non-bl';
import { ResponseParseError } from './errors';
import { fetchJson } from './utils';

const NON_BL_BASE = '/api/non-bl';

/**
 * BE HouseBlSummaryResponse 필드(camelCase) 기준 스키마.
 * Name 컬럼 6개(shipperName 등)는 현재 BE summary에 미포함으로 null fallback 처리.
 * pkgQty/grossWeightKg/cbm은 숫자로 오므로 string 변환.
 */
const NON_BL_ROW_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string().nullable().transform((v) => v ?? ''),
  bound: z.string(),
  etd: z.string().nullable().transform((v) => v ?? ''),
  eta: z.string().nullable().transform((v) => v ?? ''),
  polCode: z.string().nullable().transform((v) => v ?? ''),
  podCode: z.string().nullable().transform((v) => v ?? ''),
  vesselName: z.string().nullable().transform((v) => v ?? ''),
  voyageNo: z.string().nullable().transform((v) => v ?? ''),
  shipperCode: z.string().nullable().transform((v) => v ?? ''),
  consigneeCode: z.string().nullable().transform((v) => v ?? ''),
  notifyCode: z.string().nullable().transform((v) => v ?? ''),
  settlePartnerCode: z.string().nullable().transform((v) => v ?? ''),
  actualCustomerCode: z.string().nullable().transform((v) => v ?? ''),
  linerCode: z.string().nullable().transform((v) => v ?? ''),
  linerName: z.string().nullable().transform((v) => v ?? ''),
  pkgQty: z.number().nullable().transform((v) => String(v ?? '')),
  pkgUnit: z.string().nullable().transform((v) => v ?? ''),
  grossWeightKg: z.number().nullable().transform((v) => String(v ?? '')),
  cbm: z.number().nullable().transform((v) => String(v ?? '')),
}).transform((raw) => ({
  id: raw.id,
  nonBlNo: raw.hblNo,
  bound: raw.bound,
  etd: raw.etd,
  eta: raw.eta,
  pol: raw.polCode,
  pod: raw.podCode,
  vesselName: raw.vesselName,
  voyNo: raw.voyageNo,
  shipperCode: raw.shipperCode,
  shipperName: '',
  consigneeCode: raw.consigneeCode,
  consigneeName: '',
  notifyCode: raw.notifyCode,
  notifyName: '',
  settlePartnerCode: raw.settlePartnerCode,
  settlePartnerName: '',
  linerCode: raw.linerCode,
  linerName: raw.linerName,
  actualCustomerCode: raw.actualCustomerCode,
  actualCustomerName: '',
  pkgQty: raw.pkgQty,
  pkgUnit: raw.pkgUnit,
  grossWt: raw.grossWeightKg,
  cbm: raw.cbm,
  teamName: '',
} satisfies NonBlRow));

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

export const API_NON_BL_PORT: NonBlPort = {
  // BE는 0-based page, FE는 1-based page — 어댑터에서 변환
  async list(filter: NonBlFilter, page: number, size = 50): Promise<NonBlPageResult> {
    // FE NonBlFilter 키 → BE JSON body 키 명시 매핑 (이름 불일치 항목: nonBlNo→hblNo, dateFrom→etdFrom, dateTo→etdTo)
    const body: Record<string, unknown> = { page: page - 1, size };
    if (filter.bound)        body.bound        = filter.bound;
    if (filter.nonBlNo)      body.hblNo        = filter.nonBlNo;
    if (filter.dateFrom)     body.etdFrom      = filter.dateFrom;
    if (filter.dateTo)       body.etdTo        = filter.dateTo;
    if (filter.linerCode)    body.linerCode    = filter.linerCode;
    if (filter.partyCode)    body.partyCode    = filter.partyCode;
    if (filter.portCode)     body.portCode     = filter.portCode;
    if (filter.vessel)       body.vessel       = filter.vessel;
    if (filter.voyage)       body.voyage       = filter.voyage;
    if (filter.operatorCode) body.operatorCode = filter.operatorCode;
    if (filter.teamCode)     body.teamCode     = filter.teamCode;
    if (filter.dateKind)     body.dateKind     = filter.dateKind;
    if (filter.partyKind)    body.partyKind    = filter.partyKind;
    if (filter.portKind)     body.portKind     = filter.portKind;
    const json = await fetchJson(`${NON_BL_BASE}/search`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(NON_BL_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid non-bl list response: ${parsed.error.message}`);
    const { content, totalPages, totalElements, page: p, size: s } = parsed.data.data;
    return { content, totalPages, totalElements, page: p + 1, size: s };
  },
};
