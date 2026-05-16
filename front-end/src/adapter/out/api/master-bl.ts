import { z } from 'zod';
import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlDetail, MasterBlFilter, CreateMasterBlRequest, UpdateMasterBlRequest } from '@/domain/master-bl';
import { ResponseParseError } from './errors';
import { toSearchParams, fetchJson } from './utils';

const MASTER_BL_BASE = '/api/master-bl';

const MASTER_BL_ROW_SCHEMA = z.object({
  id: z.number(),
  mblNo: z.string().nullable(),
  masterRefNo: z.string().nullable(),
  jobDiv: z.enum(['SEA', 'AIR', 'TRUCK', 'NON_BL']),
  bound: z.enum(['EXP', 'IMP']),
  shipperCode: z.string().nullable(),
  consigneeCode: z.string().nullable(),
  polCode: z.string().nullable(),
  podCode: z.string().nullable(),
  etd: z.string().nullable(),
  eta: z.string().nullable(),
  operatorCode: z.string().nullable(),
  createdAt: z.string(),
});

// §BE-sync — ConsoledHouseBlSummaryView 17 필드 (houseBlId 키 정합, AIR 전용 chargeWeightKg optional)
// BigDecimal 필드(grossWeightKg/cbm/chargeWeightKg)는 BE Jackson 기본 직렬화 → JSON number
const CONSOLIDATED_HBL_SCHEMA = z.object({
  houseBlId:      z.number(),
  hblNo:          z.string().nullable(),
  shipperCode:    z.string().nullable(),
  consigneeCode:  z.string().nullable(),
  docPartnerCode: z.string().nullable(),
  pkgQty:         z.number().nullable(),
  pkgUnit:        z.string().nullable(),
  weightUnit:     z.string().nullable(),
  grossWeightKg:  z.number().nullable(),
  cbm:            z.number().nullable(),
  etd:            z.string().nullable(),
  eta:            z.string().nullable(),
  vesselName:     z.string().nullable(),
  voyageNo:       z.string().nullable(),
  polCode:        z.string().nullable(),
  podCode:        z.string().nullable(),
  chargeWeightKg: z.number().nullable().optional(),
});

// §BE-sync — ConsoledSeaContainerView 11 필드 (houseBlId + BigDecimal→number)
// BigDecimal 필드(grossWeightKg/cbm/vgmKg)는 BE Jackson 기본 직렬화 → JSON number
const CONSOLED_SEA_CONTAINER_API_SCHEMA = z.object({
  houseBlId:     z.number(),
  containerNo:   z.string().nullable(),
  containerType: z.string().nullable(),
  sealNo1:       z.string().nullable(),
  sealNo2:       z.string().nullable(),
  sealNo3:       z.string().nullable(),
  pkgQty:        z.number().nullable(),
  pkgUnit:       z.string().nullable(),
  grossWeightKg: z.number().nullable(),
  cbm:           z.number().nullable(),
  vgmKg:         z.number().nullable(),
});

// §BE 보강 — MasterBlDetailResponse.DescView (root desc nested, seaDetail.desc 제거)
const MASTER_BL_DESC_VIEW_SCHEMA = z.object({
  marks:        z.string().nullable().optional().transform((v) => v ?? undefined),
  description:  z.string().nullable().optional().transform((v) => v ?? undefined),
  descClause1:  z.string().nullable().optional().transform((v) => v ?? undefined),
  descClause2:  z.string().nullable().optional().transform((v) => v ?? undefined),
});

// §BE-sync — SeaDetailResponse 15 필드 (desc root 승격으로 제거됨)
// §6.49 ⑮ — 모든 nullable 필드 .nullable().optional().transform(v => v ?? undefined) 통일
const SEA_DETAIL_SCHEMA = z.object({
  loadType:          z.string().nullable().optional().transform((v) => v ?? undefined),
  linerCode:         z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselCode:        z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselName:        z.string().nullable().optional().transform((v) => v ?? undefined),
  voyageNo:          z.string().nullable().optional().transform((v) => v ?? undefined),
  onboardDate:       z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselNationality: z.string().nullable().optional().transform((v) => v ?? undefined),
  serviceTerm:       z.string().nullable().optional().transform((v) => v ?? undefined),
  blType:            z.string().nullable().optional().transform((v) => v ?? undefined),
  porCode:           z.string().nullable().optional().transform((v) => v ?? undefined),
  finalDestCode:     z.string().nullable().optional().transform((v) => v ?? undefined),
  rton:              z.number().nullable().optional().transform((v) => v ?? undefined),
  lineBkgNo:         z.string().nullable().optional().transform((v) => v ?? undefined),
  issueDate:         z.string().nullable().optional().transform((v) => v ?? undefined),
  remark:            z.string().nullable().optional().transform((v) => v ?? undefined),
});

const MASTER_BL_DETAIL_SCHEMA = MASTER_BL_ROW_SCHEMA.extend({
  shipmentType: z.string().nullable(),
  // §6.49 ⑰ — freightTerm enum literal → string nullable 완화
  freightTerm: z.string().nullable(),
  pkgQty: z.number().nullable(),
  pkgUnit: z.string().nullable().optional().transform((v) => v ?? ''),
  weightUnit: z.string().nullable().optional().transform((v) => v ?? ''),
  grossWeightKg: z.number().nullable(),
  cbm: z.number().nullable(),
  // §BE 보강 — root 승격 cargo 식별 필드
  mainItemName:      z.string().nullable().optional().transform((v) => v ?? null),
  hsCode:            z.string().nullable().optional().transform((v) => v ?? null),
  settlePartnerCode: z.string().nullable().optional().transform((v) => v ?? null),
  desc:              MASTER_BL_DESC_VIEW_SCHEMA.nullable().optional(),
  consolidatedHouseBls: z.array(CONSOLIDATED_HBL_SCHEMA),
  consoledSeaContainers: z.array(CONSOLED_SEA_CONTAINER_API_SCHEMA),
  updatedAt: z.string().nullable(),
  teamCode: z.string().nullable(),
  // §BE Phase 2 — party address 3 필드
  shipperAddress: z.string().nullable(),
  consigneeAddress: z.string().nullable(),
  notifyAddress: z.string().nullable(),
  notifyCode: z.string().nullable(),
  remark: z.string().nullable().optional().transform((v) => v ?? undefined),
  // §BE Phase 2 — seaDetail nested (desc root 승격)
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
  z.object({ data: schema, message: z.string().optional() });

export const API_MASTER_BL_PORT: MasterBlPort = {
  async list(filter: MasterBlFilter): Promise<MasterBlRow[]> {
    const json = await fetchJson(`${MASTER_BL_BASE}?${toSearchParams(filter as unknown as Record<string, unknown>)}`);
    const parsed = apiResponse(pagedResult(MASTER_BL_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid list response: ${parsed.error.message}`);
    return parsed.data.data.content as unknown as MasterBlRow[];
  },

  async getById(id: number): Promise<MasterBlDetail> {
    const json = await fetchJson(`${MASTER_BL_BASE}/${id}`);
    const parsed = apiResponse(MASTER_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid detail response: ${parsed.error.message}`);
    return parsed.data.data as unknown as MasterBlDetail;
  },

  // §6.54 — create는 ID-only 반환 (BE Phase 3 Map<"id", Long> 정합)
  async create(req: CreateMasterBlRequest): Promise<{ id: number }> {
    const json = await fetchJson(MASTER_BL_BASE, {
      method: 'POST',
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid create response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  // §BE Phase 4 — update는 void 응답 (House dd76d64 패턴)
  async update(id: number, req: UpdateMasterBlRequest): Promise<void> {
    const json = await fetchJson(`${MASTER_BL_BASE}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(req),
    });
    const j = (json ?? {}) as { data?: unknown };
    if (j.data == null) return;
    // data가 있는 경우도 void로 처리 (BE ApiResponse<Void> 변환 과도기 대응)
  },

  async delete(id: number): Promise<void> {
    await fetchJson(`${MASTER_BL_BASE}/${id}`, { method: 'DELETE' });
  },

  async findByMblNo(mblNo: string): Promise<number[]> {
    const json = await fetchJson(`${MASTER_BL_BASE}/find-by-mbl-no`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mblNo }),
    });
    const parsed = apiResponse(z.array(z.number())).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid find-by-mbl-no response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async changeMblNo(id: number, mblNo: string, masterRefNo: string): Promise<void> {
    await fetchJson(`${MASTER_BL_BASE}/${id}/mbl-no`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mblNo, masterRefNo }),
    });
  },
};
