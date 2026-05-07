import { z } from 'zod';
import type { SeaHousePort, SeaHousePageResult } from '@/application/sea-house/ports';
import type { SeaHouseRow, SeaHouseFilter } from '@/domain/sea-house';
import { ResponseParseError } from './errors';
import { fetchJson } from './utils';

const SEA_HOUSE_BASE = '/api/sea-house';

const SEA_HOUSE_ROW_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string().nullable().transform((v) => v ?? ''),
  bound: z.string(),
  mblNo: z.string().nullable().transform((v) => v ?? ''),
  masterRefNo: z.string().nullable().transform((v) => v ?? ''),
  shipmentType: z.string().nullable().transform((v) => v ?? ''),
  loadType: z.string().nullable().optional().default(null),
  etd: z.string().nullable().transform((v) => v ?? ''),
  eta: z.string().nullable().transform((v) => v ?? ''),
  polCode: z.string().nullable().transform((v) => v ?? ''),
  podCode: z.string().nullable().transform((v) => v ?? ''),
  deliveryCode: z.string().nullable().optional().default(null),
  vesselName: z.string().nullable().optional().default(null),
  voyageNo: z.string().nullable().optional().default(null),
  shipperCode: z.string().nullable().transform((v) => v ?? ''),
  consigneeCode: z.string().nullable().transform((v) => v ?? ''),
  notifyCode: z.string().nullable().transform((v) => v ?? ''),
  settlePartnerCode: z.string().nullable().transform((v) => v ?? ''),
  docPartnerCode: z.string().nullable().transform((v) => v ?? ''),
  linerCode: z.string().nullable().transform((v) => v ?? ''),
  freightTerm: z.string().nullable().transform((v) => v ?? ''),
  incoterms: z.string().nullable().transform((v) => v ?? ''),
  actualCustomerCode: z.string().nullable().transform((v) => v ?? ''),
  pkgQty: z.number().nullable().transform((v) => String(v ?? '')),
  pkgUnit: z.string().nullable().transform((v) => v ?? ''),
  grossWeightKg: z.number().nullable().transform((v) => String(v ?? '')),
  rton: z.number().nullable().transform((v) => String(v ?? '')),
  cbm: z.number().nullable().optional().default(null),
  cntr20Qty: z.number().nullable().optional().default(null),
  cntr40Qty: z.number().nullable().optional().default(null),
  teuQty: z.number().nullable().optional().default(null),
  salesManCode: z.string().nullable().transform((v) => v ?? ''),
  teamCode: z.string().nullable().transform((v) => v ?? ''),
}).transform((raw) => ({
  id: raw.id,
  hblNo: raw.hblNo,
  bound: raw.bound,
  mblNo: raw.mblNo,
  masterRefNo: raw.masterRefNo,
  shipmentType: raw.shipmentType,
  loadType: raw.loadType ?? null,
  etd: raw.etd,
  eta: raw.eta,
  polCode: raw.polCode,
  podCode: raw.podCode,
  deliveryCode: raw.deliveryCode ?? null,
  vesselName: raw.vesselName ?? null,
  voyageNo: raw.voyageNo ?? null,
  shipperCode: raw.shipperCode,
  shipperName: '',
  consigneeCode: raw.consigneeCode,
  consigneeName: '',
  notifyCode: raw.notifyCode,
  notifyName: '',
  settlePartnerCode: raw.settlePartnerCode,
  settlePartnerName: '',
  docPartnerCode: raw.docPartnerCode,
  docPartnerName: '',
  linerCode: raw.linerCode,
  linerName: '',
  freightTerm: raw.freightTerm,
  incoterms: raw.incoterms,
  actualCustomerCode: raw.actualCustomerCode,
  actualCustomerName: '',
  pkgQty: raw.pkgQty,
  pkgUnit: raw.pkgUnit,
  grossWeightKg: raw.grossWeightKg,
  rton: raw.rton,
  cbm: raw.cbm ?? null,
  cntr20Qty: raw.cntr20Qty ?? null,
  cntr40Qty: raw.cntr40Qty ?? null,
  teuQty: raw.teuQty ?? null,
  salesManCode: raw.salesManCode,
  teamCode: raw.teamCode,
} satisfies SeaHouseRow));

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

export const API_SEA_HOUSE_PORT: SeaHousePort = {
  async list(filter: SeaHouseFilter, page: number, size = 50): Promise<SeaHousePageResult> {
    const body: Record<string, unknown> = { page: page - 1, size };
    body.bound = filter.bound;
    body.dateKind = filter.dateKind;
    body.dateFrom = filter.dateFrom;
    body.dateTo = filter.dateTo;
    body.masterBlKind = filter.masterBlKind;
    if (filter.masterBlValue)          body.masterBlValue          = filter.masterBlValue;
    if (filter.hblNo)                  body.hblNo                  = filter.hblNo;
    body.partyKind = filter.partyKind;
    body.partyCode = filter.partyCode;
    if (filter.actualCustomerCode)     body.actualCustomerCode     = filter.actualCustomerCode;
    if (filter.partnerKind)            body.partnerKind            = filter.partnerKind;
    if (filter.partnerCode)            body.partnerCode            = filter.partnerCode;
    if (filter.linerCode)              body.linerCode              = filter.linerCode;
    body.portKind = filter.portKind;
    body.portCode = filter.portCode;
    if (filter.vesselName)             body.vesselName             = filter.vesselName;
    if (filter.voyageNo)               body.voyageNo               = filter.voyageNo;
    if (filter.shipmentType)           body.shipmentType           = filter.shipmentType;
    if (filter.teamCode)               body.teamCode               = filter.teamCode;
    if (filter.operatorCode)           body.operatorCode           = filter.operatorCode;
    if (filter.salesClass)             body.salesClass             = filter.salesClass;
    if (filter.salesManCode)           body.salesManCode           = filter.salesManCode;
    if (filter.incoterms)              body.incoterms              = filter.incoterms;
    if (filter.loadType)               body.loadType               = filter.loadType;
    const json = await fetchJson(`${SEA_HOUSE_BASE}/search`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(SEA_HOUSE_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid sea-house list response: ${parsed.error.message}`);
    const { content, totalPages, totalElements, page: p, size: s } = parsed.data.data;
    return { content, totalPages, totalElements, page: p + 1, size: s };
  },
};
