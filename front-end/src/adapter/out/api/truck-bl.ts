import { z } from 'zod';
import type { TruckBlPort, TruckBlPageResult } from '@/application/truck-bl/ports';
import type {
  TruckBlRow,
  TruckBlFilter,
  TruckBlDetail,
  CreateTruckBlRequest,
  UpdateTruckBlRequest,
} from '@/domain/truck-bl';
import { ResponseParseError } from './errors';
import { fetchJson } from './utils';

const TRUCK_BL_BASE = '/api/truck-bl';

/**
 * BE HouseBlSummaryResponse 필드(camelCase) 기준 스키마.
 * Name 컬럼 6개(shipperName 등)는 현재 BE summary에 미포함으로 null fallback 처리.
 * pkgQty/grossWeightKg/cbm은 숫자로 오므로 string 변환.
 */
const TRUCK_BL_ROW_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string().nullable().transform((v) => v ?? ''),
  bound: z.string(),
  etd: z.string().nullable().transform((v) => v ?? ''),
  eta: z.string().nullable().transform((v) => v ?? ''),
  polCode: z.string().nullable().transform((v) => v ?? ''),
  podCode: z.string().nullable().transform((v) => v ?? ''),
  truckerCode: z.string().nullable().transform((v) => v ?? ''),
  shipperCode: z.string().nullable().transform((v) => v ?? ''),
  consigneeCode: z.string().nullable().transform((v) => v ?? ''),
  notifyCode: z.string().nullable().transform((v) => v ?? ''),
  docPartnerCode: z.string().nullable().transform((v) => v ?? ''),
  pkgQty: z.number().nullable().transform((v) => String(v ?? '')),
  pkgUnit: z.string().nullable().transform((v) => v ?? ''),
  grossWeightKg: z.number().nullable().transform((v) => String(v ?? '')),
  cbm: z.number().nullable().transform((v) => String(v ?? '')),
}).transform((raw) => ({
  id: raw.id,
  truckBlNo: raw.hblNo,
  bound: raw.bound,
  etd: raw.etd,
  eta: raw.eta,
  pol: raw.polCode,
  pod: raw.podCode,
  truckerCode: raw.truckerCode,
  truckerName: '',
  shipperCode: raw.shipperCode,
  shipperName: '',
  consigneeCode: raw.consigneeCode,
  consigneeName: '',
  notifyCode: raw.notifyCode,
  notifyName: '',
  docPartnerCode: raw.docPartnerCode,
  docPartnerName: '',
  pkgQty: raw.pkgQty,
  pkgUnit: raw.pkgUnit,
  grossWt: raw.grossWeightKg,
  cbm: raw.cbm,
  teamName: '',
} satisfies TruckBlRow));

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

const TRUCK_ORDER_DETAIL_SCHEMA = z.object({
  id: z.number().optional(),
  truckOrderNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  pkgQty: z.number().nullable().optional().transform((v) => v ?? undefined),
  pkgUnit: z.string().nullable().optional().transform((v) => v ?? undefined),
  grossWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  cbm: z.number().nullable().optional().transform((v) => v ?? undefined),
  truckNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  truckType: z.string().nullable().optional().transform((v) => v ?? undefined),
  driver: z.string().nullable().optional().transform((v) => v ?? undefined),
  mobileNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  containerNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  containerType: z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo1: z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo2: z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo3: z.string().nullable().optional().transform((v) => v ?? undefined),
});

const DESC_DETAIL_SCHEMA = z.object({
  marks: z.string().nullable().optional().transform((v) => v ?? undefined),
  description: z.string().nullable().optional().transform((v) => v ?? undefined),
  descClause1: z.string().nullable().optional().transform((v) => v ?? undefined),
  descClause2: z.string().nullable().optional().transform((v) => v ?? undefined),
}).optional();

const TRUCK_BL_DETAIL_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  jobDiv: z.string(),
  bound: z.string(),
  shipmentType: z.string().nullable().optional().transform((v) => v ?? undefined),
  freightTerm: z.string().nullable().optional().transform((v) => v ?? undefined),
  shipperCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  shipperAddr: z.string().nullable().optional().transform((v) => v ?? undefined),
  consigneeCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  consigneeAddr: z.string().nullable().optional().transform((v) => v ?? undefined),
  notifyCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  notifyAddr: z.string().nullable().optional().transform((v) => v ?? undefined),
  settlePartnerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  docPartnerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  docPartnerAddress: z.string().nullable().optional().transform((v) => v ?? undefined),
  polCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  podCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  deliveryCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  etd: z.string().nullable().optional().transform((v) => v ?? undefined),
  eta: z.string().nullable().optional().transform((v) => v ?? undefined),
  pkgQty: z.number().nullable().optional().transform((v) => v ?? undefined),
  pkgUnit: z.string().nullable().optional().transform((v) => v ?? undefined),
  grossWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  cbm: z.number().nullable().optional().transform((v) => v ?? undefined),
  weightUnit: z.string().nullable().optional().transform((v) => v ?? undefined),
  actualCustomerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  operatorCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  teamCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  salesManCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  incoterms: z.string().nullable().optional().transform((v) => v ?? undefined),
  createdAt: z.string().nullable().optional().transform((v) => v ?? undefined),
  updatedAt: z.string().nullable().optional().transform((v) => v ?? undefined),
  truckerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  truckerPic: z.string().nullable().optional().transform((v) => v ?? undefined),
  chargeWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  pickupDate: z.string().nullable().optional().transform((v) => v ?? undefined),
  pickupTm: z.string().nullable().optional().transform((v) => v ?? undefined),
  etdTm: z.string().nullable().optional().transform((v) => v ?? undefined),
  etaTm: z.string().nullable().optional().transform((v) => v ?? undefined),
  loadType: z.string().nullable().optional().transform((v) => v ?? undefined),
  serviceTerm: z.string().nullable().optional().transform((v) => v ?? undefined),
  voyageNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselName: z.string().nullable().optional().transform((v) => v ?? undefined),
  remark: z.string().nullable().optional().transform((v) => v ?? undefined),
  truckOrders: z.array(TRUCK_ORDER_DETAIL_SCHEMA).nullable().optional().transform((v) => v ?? undefined),
  desc: DESC_DETAIL_SCHEMA,
});

export const API_TRUCK_BL_PORT: TruckBlPort = {
  // BE는 0-based page, FE는 1-based page — 어댑터에서 변환
  async list(filter: TruckBlFilter, page: number, size = 50): Promise<TruckBlPageResult> {
    // FE TruckBlFilter 키 → BE JSON body 키 명시 매핑 (이름 불일치 항목: truckBlNo→hblNo, dateFrom→etdFrom, dateTo→etdTo)
    const body: Record<string, unknown> = { page: page - 1, size };
    if (filter.bound)        body.bound        = filter.bound;
    if (filter.truckBlNo)    body.hblNo        = filter.truckBlNo;
    if (filter.dateFrom)     body.etdFrom      = filter.dateFrom;
    if (filter.dateTo)       body.etdTo        = filter.dateTo;
    if (filter.truckerCode)     body.truckerCode     = filter.truckerCode;
    if (filter.partyCode)       body.partyCode       = filter.partyCode;
    if (filter.portCode)        body.portCode        = filter.portCode;
    if (filter.docPartnerCode)  body.docPartnerCode  = filter.docPartnerCode;
    if (filter.operatorCode)    body.operatorCode    = filter.operatorCode;
    if (filter.teamCode)     body.teamCode     = filter.teamCode;
    if (filter.dateKind)     body.dateKind     = filter.dateKind;
    if (filter.partyKind)    body.partyKind    = filter.partyKind;
    if (filter.portKind)     body.portKind     = filter.portKind;
    const json = await fetchJson(`${TRUCK_BL_BASE}/search`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(TRUCK_BL_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid truck-bl list response: ${parsed.error.message}`);
    const { content, totalPages, totalElements, page: p, size: s } = parsed.data.data;
    return { content, totalPages, totalElements, page: p + 1, size: s };
  },

  async getById(id: number): Promise<TruckBlDetail> {
    const json = await fetchJson(`${TRUCK_BL_BASE}/${id}`);
    const parsed = apiResponse(TRUCK_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid truck-bl detail response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async create(payload: CreateTruckBlRequest): Promise<{ id: number }> {
    const json = await fetchJson(TRUCK_BL_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid truck-bl create response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async update(id: number, payload: UpdateTruckBlRequest): Promise<void> {
    // BE는 204 또는 200+data=null 반환 — 응답 본문 무시
    await fetchJson(`${TRUCK_BL_BASE}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
  },

  async delete(id: number): Promise<void> {
    await fetchJson(`${TRUCK_BL_BASE}/${id}`, { method: 'DELETE' });
  },

  async findByHblNo(hblNo: string): Promise<number[]> {
    const json = await fetchJson(`${TRUCK_BL_BASE}/find-by-hbl-no`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ hblNo }),
    });
    const parsed = apiResponse(z.array(z.number())).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid truck-bl find-by-hbl-no response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async changeHblNo(id: number, hblNo: string): Promise<void> {
    await fetchJson(`${TRUCK_BL_BASE}/${id}/hbl-no`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ hblNo }),
    });
  },
};
