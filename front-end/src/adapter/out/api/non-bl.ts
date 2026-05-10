import { z } from 'zod';
import type { NonBlPort, NonBlPageResult } from '@/application/non-bl/ports';
import type { NonBlRow, NonBlFilter, NonBlDetail, CreateNonBlRequest, UpdateNonBlRequest } from '@/domain/non-bl';
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

const NON_BL_CONTAINER_SCHEMA = z.object({
  id: z.number().optional(),
  seq: z.number().optional(),
  containerNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  containerType: z.string().nullable().optional().transform((v) => v ?? undefined),
  lengthFeet: z.number().nullable().optional().transform((v) => v ?? undefined),
  sealNo1: z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo2: z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo3: z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo4: z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo5: z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo6: z.string().nullable().optional().transform((v) => v ?? undefined),
  pkgQty: z.number().nullable().optional().transform((v) => v ?? undefined),
  pkgUnit: z.string().nullable().optional().transform((v) => v ?? undefined),
  grossWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  netWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  vgmKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  cbm: z.number().nullable().optional().transform((v) => v ?? undefined),
  isSoc: z.boolean().nullable().optional().transform((v) => v ?? undefined),
});

const NON_BL_DIM_SCHEMA = z.object({
  id: z.number().optional(),
  lengthCm: z.number().nullable().optional().transform((v) => v ?? undefined),
  widthCm: z.number().nullable().optional().transform((v) => v ?? undefined),
  heightCm: z.number().nullable().optional().transform((v) => v ?? undefined),
  quantity: z.number().nullable().optional().transform((v) => v ?? undefined),
  cbm: z.number().nullable().optional().transform((v) => v ?? undefined),
  volumeWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
});

const NON_BL_DETAIL_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  jobDiv: z.string(),
  bound: z.string(),
  shipmentType: z.string().nullable().optional().transform((v) => v ?? undefined),
  freightTerm: z.string().nullable().optional().transform((v) => v ?? undefined),
  shipperCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  consigneeCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  notifyCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  docPartnerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  settlePartnerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  actualCustomerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  polCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  podCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  etd: z.string().nullable().optional().transform((v) => v ?? undefined),
  eta: z.string().nullable().optional().transform((v) => v ?? undefined),
  pkgQty: z.number().nullable().optional().transform((v) => v ?? undefined),
  pkgUnit: z.string().nullable().optional().transform((v) => v ?? undefined),
  grossWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  cbm: z.number().nullable().optional().transform((v) => v ?? undefined),
  operatorCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  salesManCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  teamCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  mblNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  masterRefNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  masterBlId: z.number().nullable().optional().transform((v) => v ?? undefined),
  mainItemName: z.string().nullable().optional().transform((v) => v ?? undefined),
  hsCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  workDivision: z.string().nullable().optional().transform((v) => v ?? undefined),
  originalBlRef: z.string().nullable().optional().transform((v) => v ?? undefined),
  rton: z.number().nullable().optional().transform((v) => v ?? undefined),
  volumeWtKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  linerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  linerName: z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselName: z.string().nullable().optional().transform((v) => v ?? undefined),
  voyageNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  finalDestCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  finalDestName: z.string().nullable().optional().transform((v) => v ?? undefined),
  finalEta: z.string().nullable().optional().transform((v) => v ?? undefined),
  volumeDivisor: z.string().nullable().optional(),
  salesClass: z.string().nullable().optional(),
  createdAt: z.string().nullable().optional().transform((v) => v ?? undefined),
  updatedAt: z.string().nullable().optional().transform((v) => v ?? undefined),
  remark: z.string().nullable().optional().transform((v) => v ?? undefined),
  containers: z.array(NON_BL_CONTAINER_SCHEMA).default([]),
  dims: z.array(NON_BL_DIM_SCHEMA).default([]),
});

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

/** FE CreateNonBlRequest → BE CreateHouseBlRequest 형식 변환. jobDiv는 항상 NON_BL 강제. */
function toBeRequest(req: CreateNonBlRequest) {
  return {
    jobDiv: 'NON_BL',
    hblNo: req.hblNo,
    bound: req.bound,
    workDivision: req.workDivision,
    shipmentType: req.shipmentType,
    freightTerm: req.freightTerm,
    shipperCode: req.shipperCode,
    consigneeCode: req.consigneeCode,
    notifyCode: req.notifyCode,
    docPartnerCode: req.docPartnerCode,
    settlePartnerCode: req.settlePartnerCode,
    actualCustomerCode: req.actualCustomerCode,
    polCode: req.polCode,
    podCode: req.podCode,
    etd: req.etd,
    eta: req.eta,
    pkgQty: req.pkgQty,
    pkgUnit: req.pkgUnit,
    grossWeightKg: req.grossWeightKg,
    cbm: req.cbm,
    operatorCode: req.operatorCode,
    salesManCode: req.salesManCode,
    teamCode: req.teamCode,
    mblNo: req.mblNo,
    masterRefNo: req.masterRefNo,
    masterBlId: req.masterBlId,
    mainItemName: req.mainItemName,
    hsCode: req.hsCode,
    rton: req.rton,
    volumeWeightKg: req.volumeWtKg,
    linerCode: req.linerCode,
    linerName: req.linerName,
    vesselName: req.vesselName,
    voyageNo: req.voyageNo,
    finalDestCode: req.finalDestCode,
    finalDestName: req.finalDestName,
    finalEta: req.finalEta,
    salesClass: req.salesClass,
    volumeDivisor: req.volumeDivisor,
    originalBlRef: req.originalBlRef,
    containers: req.containers,
    dims: req.dims,
    remark: req.remark,
  };
}

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

  async getById(id: number): Promise<NonBlDetail> {
    const json = await fetchJson(`${NON_BL_BASE}/${id}`);
    const parsed = apiResponse(NON_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid non-bl detail response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async create(req: CreateNonBlRequest): Promise<{ id: number }> {
    const json = await fetchJson(NON_BL_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(toBeRequest(req)),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid non-bl create response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async update(id: number, req: UpdateNonBlRequest): Promise<NonBlDetail> {
    const json = await fetchJson(`${NON_BL_BASE}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(toBeRequest(req as CreateNonBlRequest)),
    });
    const parsed = apiResponse(NON_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid non-bl update response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async delete(id: number): Promise<void> {
    await fetchJson(`${NON_BL_BASE}/${id}`, { method: 'DELETE' });
  },

  async changeHblNo(id: number, hblNo: string): Promise<void> {
    await fetchJson(`${NON_BL_BASE}/${id}/hbl-no`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ hblNo }),
    });
  },

  async findByHblNo(hblNo: string): Promise<number[]> {
    const json = await fetchJson(`${NON_BL_BASE}/find-by-hbl-no`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ hblNo }),
    });
    const parsed = apiResponse(z.array(z.number())).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid non-bl find-by-hbl-no response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },
};
