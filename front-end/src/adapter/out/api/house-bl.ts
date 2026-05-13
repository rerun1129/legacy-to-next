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

// §BE-sync — SeaContainerView / SeaDescView (BE SeaDetailResponse nested 컬렉션)
const SEA_CONTAINER_VIEW_SCHEMA = z.object({
  id:            z.number(),
  containerNo:   z.string().nullable().optional().transform((v) => v ?? undefined),
  containerType: z.string().nullable().optional().transform((v) => v ?? undefined),
  lengthFeet:    z.number().nullable().optional().transform((v) => v ?? undefined),
  sealNo1:       z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo2:       z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo3:       z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo4:       z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo5:       z.string().nullable().optional().transform((v) => v ?? undefined),
  sealNo6:       z.string().nullable().optional().transform((v) => v ?? undefined),
  pkgQty:        z.number().nullable().optional().transform((v) => v ?? undefined),
  pkgUnit:       z.string().nullable().optional().transform((v) => v ?? undefined),
  grossWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  netWeightKg:   z.number().nullable().optional().transform((v) => v ?? undefined),
  cbm:           z.number().nullable().optional().transform((v) => v ?? undefined),
  vgmKg:         z.number().nullable().optional().transform((v) => v ?? undefined),
  soc:           z.boolean().nullable().optional().transform((v) => v ?? undefined),
  seq:           z.number().nullable().optional().transform((v) => v ?? undefined),
});

const SEA_DESC_VIEW_SCHEMA = z.object({
  marks:        z.string().nullable().optional().transform((v) => v ?? undefined),
  description:  z.string().nullable().optional().transform((v) => v ?? undefined),
  descClause1:  z.string().nullable().optional().transform((v) => v ?? undefined),
  descClause2:  z.string().nullable().optional().transform((v) => v ?? undefined),
});

const SEA_DETAIL_SCHEMA = z.object({
  linerCode:                z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselCode:               z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselName:               z.string().nullable().optional().transform((v) => v ?? undefined),
  voyageNo:                 z.string().nullable().optional().transform((v) => v ?? undefined),
  onboardDate:              z.string().nullable().optional().transform((v) => v ?? undefined),
  porCode:                  z.string().nullable().optional().transform((v) => v ?? undefined),
  finalDestCode:            z.string().nullable().optional().transform((v) => v ?? undefined),
  issueDate:                z.string().nullable().optional().transform((v) => v ?? undefined),
  noOfBl:                   z.string().nullable().optional().transform((v) => v ?? undefined),
  issuePlace:               z.string().nullable().optional().transform((v) => v ?? undefined),
  doDate:                   z.string().nullable().optional().transform((v) => v ?? undefined),
  payableAt:                z.string().nullable().optional().transform((v) => v ?? undefined),
  triangle:                 z.boolean().nullable().optional().transform((v) => v ?? false),
  serviceTerm:              z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselNationality:        z.string().nullable().optional().transform((v) => v ?? undefined),
  rton:                     z.number().nullable().optional().transform((v) => v ?? undefined),
  sayInformation:           z.string().nullable().optional().transform((v) => v ?? undefined),
  noOfContainerOrPackages:  z.string().nullable().optional().transform((v) => v ?? undefined),
  // §BE-sync — BE SeaDetailResponse.containers / .desc (seaDetail nested)
  containers: z.array(SEA_CONTAINER_VIEW_SCHEMA).optional(),
  desc: SEA_DESC_VIEW_SCHEMA.optional(),
});

const HOUSE_BL_DETAIL_SCHEMA = HOUSE_BL_ROW_SCHEMA.extend({
  shipmentType: z.enum(['HOUSE', 'DIRECT']).nullable(),
  blType: z.string().nullable(),
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
  loadType: z.string().nullable().optional().transform((v) => v ?? undefined),
  linerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  linerName: z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselName: z.string().nullable().optional().transform((v) => v ?? undefined),
  voyageNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  finalDestCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  finalDestName: z.string().nullable().optional().transform((v) => v ?? undefined),
  finalEta: z.string().nullable().optional().transform((v) => v ?? undefined),
  volumeWeightKg: z.number().nullable().optional().transform((v) => v ?? undefined),
  rton: z.number().nullable().optional().transform((v) => v ?? undefined),
  remark: z.string().nullable().optional().transform((v) => v ?? undefined),
  incoterms: z.string().nullable().optional().transform((v) => v ?? undefined),
  salesClass: z.string().nullable().optional().transform((v) => v ?? undefined),
  mblNo: z.string().nullable().optional().transform((v) => v ?? undefined),
  settlePartnerCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  seaDetail: SEA_DETAIL_SCHEMA.nullable().optional(),
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

  async changeHblNo(id: number, hblNo: string): Promise<void> {
    await fetchJson(`${HOUSE_BL_BASE}/${id}/hbl-no`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ hblNo }),
    });
  },

  async findByHblNo(hblNo: string, jobDiv: 'SEA' | 'AIR' | 'TRUCK' | 'NON_BL'): Promise<number[]> {
    const json = await fetchJson(`${HOUSE_BL_BASE}/find-by-hbl-no`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ hblNo, jobDiv }),
    });
    const parsed = apiResponse(z.array(z.number())).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid find-by-hbl-no response: ${parsed.error.message}`);
    return parsed.data.data;
  },
};
