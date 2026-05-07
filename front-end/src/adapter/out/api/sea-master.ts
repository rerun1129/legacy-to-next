import { z } from 'zod';
import type { SeaMasterPort, SeaMasterPageResult } from '@/application/sea-master/ports';
import type { SeaMasterRow, SeaMasterFilter } from '@/domain/sea-master';
import { ResponseParseError } from './errors';
import { fetchJson } from './utils';

const SEA_MASTER_BASE = '/api/sea-master';

const SEA_MASTER_ROW_SCHEMA = z.object({
  id: z.number(),
  bound: z.string(),
  mblNo: z.string().nullable().transform((v) => v ?? ''),
  shipmentType: z.string().nullable().transform((v) => v ?? ''),
  etd: z.string().nullable().transform((v) => v ?? ''),
  eta: z.string().nullable().transform((v) => v ?? ''),
  grossWeightKg: z.number().nullable().transform((v) => String(v ?? '')),
  rton: z.number().nullable().transform((v) => String(v ?? '')),
  pkgQty: z.number().nullable().transform((v) => String(v ?? '')),
  pkgUnit: z.string().nullable().transform((v) => v ?? ''),
  polCode: z.string().nullable().transform((v) => v ?? ''),
  podCode: z.string().nullable().transform((v) => v ?? ''),
  shipperCode: z.string().nullable().transform((v) => v ?? ''),
  consigneeCode: z.string().nullable().transform((v) => v ?? ''),
  notifyCode: z.string().nullable().transform((v) => v ?? ''),
  linerCode: z.string().nullable().transform((v) => v ?? ''),
  houseBlCount: z.number().nullable().transform((v) => v ?? 0),
  operatorCode: z.string().nullable().transform((v) => v ?? ''),
  masterRefNo: z.string().nullable().transform((v) => v ?? ''),
  freightTerm: z.string().nullable().transform((v) => v ?? ''),
  teamCode: z.string().nullable().transform((v) => v ?? ''),
  vesselName: z.string().nullable().optional().default(null),
  voyageNo: z.string().nullable().optional().default(null),
  loadType: z.string().nullable().optional().default(null),
  cbm: z.number().nullable().optional().default(null),
}).transform((raw) => ({
  id: raw.id,
  bound: raw.bound,
  mblNo: raw.mblNo,
  shipmentType: raw.shipmentType,
  etd: raw.etd,
  eta: raw.eta,
  grossWeightKg: raw.grossWeightKg,
  rton: raw.rton,
  pkgQty: raw.pkgQty,
  pkgUnit: raw.pkgUnit,
  polCode: raw.polCode,
  podCode: raw.podCode,
  shipperCode: raw.shipperCode,
  shipperName: '',
  consigneeCode: raw.consigneeCode,
  consigneeName: '',
  notifyCode: raw.notifyCode,
  notifyName: '',
  linerCode: raw.linerCode,
  linerName: '',
  houseBlCount: raw.houseBlCount,
  operatorCode: raw.operatorCode,
  masterRefNo: raw.masterRefNo,
  freightTerm: raw.freightTerm,
  teamCode: raw.teamCode,
  vesselName: raw.vesselName,
  voyageNo: raw.voyageNo,
  loadType: raw.loadType,
  cbm: raw.cbm,
} satisfies SeaMasterRow));

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

export const API_SEA_MASTER_PORT: SeaMasterPort = {
  async list(filter: SeaMasterFilter, page: number, size = 50): Promise<SeaMasterPageResult> {
    const body: Record<string, unknown> = { page: page - 1, size };
    body.bound = filter.bound;
    body.dateKind = filter.dateKind;
    body.dateFrom = filter.dateFrom;
    body.dateTo = filter.dateTo;
    body.masterBlKind = filter.masterBlKind;
    if (filter.masterBlValue)   body.masterBlValue   = filter.masterBlValue;
    body.partyKind = filter.partyKind;
    body.partyCode = filter.partyCode;
    if (filter.linerCode)       body.linerCode       = filter.linerCode;
    body.portKind = filter.portKind;
    body.portCode = filter.portCode;
    if (filter.vesselName)       body.vesselName      = filter.vesselName;
    if (filter.voyageNo)         body.voyageNo        = filter.voyageNo;
    if (filter.shipmentType)     body.shipmentType    = filter.shipmentType;
    if (filter.loadType)         body.loadType        = filter.loadType;
    const json = await fetchJson(`${SEA_MASTER_BASE}/search`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(SEA_MASTER_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid sea-master list response: ${parsed.error.message}`);
    const { content, totalPages, totalElements, page: p, size: s } = parsed.data.data;
    return { content, totalPages, totalElements, page: p + 1, size: s };
  },
};
