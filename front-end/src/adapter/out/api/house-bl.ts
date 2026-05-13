import { z } from 'zod';
import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlDetail, HouseBlFilter, CreateHouseBlRequest, UpdateHouseBlRequest } from '@/domain/house-bl';
import { ResponseParseError } from './errors';
import { fetchJson } from './utils';


const HOUSE_BL_BASE = '/api/house-bl';

const HOUSE_BL_ROW_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string().nullable(),
  jobDiv: z.enum(['SEA', 'AIR', 'TRUCK', 'NON_BL']),
  bound: z.enum(['EXP', 'IMP']),
  polCode: z.string().nullable(),
  podCode: z.string().nullable(),
  etd: z.string().nullable(),
  eta: z.string().nullable(),
  shipperCode: z.string().nullable(),
  consigneeCode: z.string().nullable(),
  pkgQty: z.number().nullable(),
  pkgUnit: z.string().nullable(),
  weightUnit: z.string().nullable().optional().transform((v) => v ?? ''),
  createdAt: z.string(),
});

const HOUSE_BL_DETAIL_SCHEMA = HOUSE_BL_ROW_SCHEMA.extend({
  shipmentType: z.enum(['HOUSE', 'DIRECT']).nullable(),
  blType: z.enum(['OBL', 'SWB', 'SURRENDER']).nullable(),
  freightTerm: z.enum(['PREPAID', 'COLLECT']).nullable(),
  notifyCode: z.string().nullable(),
  deliveryCode: z.string().nullable(),
  grossWeightKg: z.number().nullable(),
  cbm: z.number().nullable(),
  operatorCode: z.string().nullable(),
  teamCode: z.string().nullable(),
  salesManCode: z.string().nullable(),
  masterBlId: z.number().nullable(),
  updatedAt: z.string().nullable(),
  docPartnerCode: z.string().nullable(),
  actualCustomerCode: z.string().nullable(),
  // §6.48 ⑧ / §6.29 — BE 860abfb에서 노출된 party address 4필드
  shipperAddress: z.string().nullable(),
  consigneeAddress: z.string().nullable(),
  notifyAddress: z.string().nullable(),
  docPartnerAddress: z.string().nullable(),
  linerCode: z.string().optional(),
  linerName: z.string().optional(),
  vesselName: z.string().optional(),
  voyageNo: z.string().optional(),
  finalDestCode: z.string().optional(),
  finalDestName: z.string().optional(),
  finalEta: z.string().optional(),
  volumeWeightKg: z.number().optional(),
  rton: z.number().optional(),
  remark: z.string().nullable().optional().transform((v) => v ?? undefined),
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
  z.object({
    data: schema,
    message: z.string().optional(),
  });

export const API_HOUSE_BL_PORT: HouseBlPort = {
  async list(filter: HouseBlFilter): Promise<HouseBlRow[]> {
    // GET → POST /search 전환
    const json = await fetchJson(`${HOUSE_BL_BASE}/search`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(filter),
    });
    const parsed = apiResponse(pagedResult(HOUSE_BL_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid list response: ${parsed.error.message}`);
    return parsed.data.data.content;
  },

  async getById(id: number): Promise<HouseBlDetail> {
    const json = await fetchJson(`${HOUSE_BL_BASE}/${id}`);
    const parsed = apiResponse(HOUSE_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid detail response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async save(req: unknown): Promise<HouseBlDetail> {
    const json = await fetchJson(HOUSE_BL_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(HOUSE_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid save response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async create(req: CreateHouseBlRequest): Promise<HouseBlDetail> {
    const json = await fetchJson(HOUSE_BL_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(HOUSE_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid create response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async update(id: number, req: UpdateHouseBlRequest): Promise<HouseBlDetail | null> {
    const json = await fetchJson(`${HOUSE_BL_BASE}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });
    // §6.29 — SEA jobDiv 분기에서 BE가 ApiResponse<Void> 반환 (data=null).
    // data가 null이면 void 응답으로 간주하고 null 반환. 그 외 jobDiv는 HouseBlDetail 파싱.
    const maybeVoid = z.object({ data: z.null() }).safeParse(json);
    if (maybeVoid.success) return null;
    const parsed = apiResponse(HOUSE_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid update response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async delete(id: number): Promise<void> {
    await fetchJson(`${HOUSE_BL_BASE}/${id}`, { method: 'DELETE' });
  },
};
