import { z } from 'zod';
import type { AirHousePort, AirHousePageResult } from '@/application/air-house/ports';
import type { AirHouseRow, AirHouseFilter } from '@/domain/air-house';
import { ResponseParseError } from './errors';
import { fetchJson } from './utils';
import { DEFAULT_PAGE_SIZE } from '@/lib/grid-pagination';

const AIR_HOUSE_BASE = '/api/air-house';

const AIR_HOUSE_ROW_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string().nullable().transform((v) => v ?? ''),
  bound: z.string(),
  mblNo: z.string().nullable().transform((v) => v ?? ''),
  shipmentType: z.string().nullable().transform((v) => v ?? ''),
  etd: z.string().nullable().transform((v) => v ?? ''),
  eta: z.string().nullable().transform((v) => v ?? ''),
  grossWeightKg: z.number().nullable().transform((v) => String(v ?? '')),
  chargeWeightKg: z.number().nullable().transform((v) => String(v ?? '')),
  pkgQty: z.number().nullable().transform((v) => String(v ?? '')),
  pkgUnit: z.string().nullable().transform((v) => v ?? ''),
  polCode: z.string().nullable().transform((v) => v ?? ''),
  podCode: z.string().nullable().transform((v) => v ?? ''),
  shipperCode: z.string().nullable().transform((v) => v ?? ''),
  consigneeCode: z.string().nullable().transform((v) => v ?? ''),
  notifyCode: z.string().nullable().transform((v) => v ?? ''),
  settlePartnerCode: z.string().nullable().transform((v) => v ?? ''),
  docPartnerCode: z.string().nullable().transform((v) => v ?? ''),
  airlineCode: z.string().nullable().transform((v) => v ?? ''),
  masterRefNo: z.string().nullable().transform((v) => v ?? ''),
  freightTerm: z.string().nullable().transform((v) => v ?? ''),
  incoterms: z.string().nullable().transform((v) => v ?? ''),
  actualCustomerCode: z.string().nullable().transform((v) => v ?? ''),
  salesManCode: z.string().nullable().transform((v) => v ?? ''),
  teamCode: z.string().nullable().transform((v) => v ?? ''),
  teamName: z.string().nullable().transform((v) => v ?? ''),
}).transform((raw) => ({
  id: raw.id,
  hblNo: raw.hblNo,
  bound: raw.bound,
  mblNo: raw.mblNo,
  shipmentType: raw.shipmentType,
  etd: raw.etd,
  eta: raw.eta,
  grossWeightKg: raw.grossWeightKg,
  chargeWeightKg: raw.chargeWeightKg,
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
  settlePartnerCode: raw.settlePartnerCode,
  settlePartnerName: '',
  docPartnerCode: raw.docPartnerCode,
  docPartnerName: '',
  airlineCode: raw.airlineCode,
  airlineName: '',
  masterRefNo: raw.masterRefNo,
  freightTerm: raw.freightTerm,
  incoterms: raw.incoterms,
  actualCustomerCode: raw.actualCustomerCode,
  actualCustomerName: '',
  salesManCode: raw.salesManCode,
  teamCode: raw.teamCode,
  teamName: raw.teamName,
} satisfies AirHouseRow));

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

export const API_AIR_HOUSE_PORT: AirHousePort = {
  async list(filter: AirHouseFilter, page: number, size = DEFAULT_PAGE_SIZE): Promise<AirHousePageResult> {
    const body: Record<string, unknown> = { page: page - 1, size };
    body.bound = filter.bound;
    body.dateKind = filter.dateKind;
    body.dateFrom = filter.dateFrom;
    body.dateTo = filter.dateTo;
    body.masterAwbKind = filter.masterAwbKind;
    if (filter.masterAwbValue)        body.masterAwbValue        = filter.masterAwbValue;
    if (filter.hblNo)                 body.hblNo                 = filter.hblNo;
    body.partyKind = filter.partyKind;
    body.partyCode = filter.partyCode;
    if (filter.actualCustomerCode)    body.actualCustomerCode    = filter.actualCustomerCode;
    if (filter.settlePartnerCode)     body.settlePartnerCode     = filter.settlePartnerCode;
    if (filter.airlineCode)           body.airlineCode           = filter.airlineCode;
    body.portKind = filter.portKind;
    body.portCode = filter.portCode;
    if (filter.shipmentType)          body.shipmentType          = filter.shipmentType;
    if (filter.teamCode)              body.teamCode              = filter.teamCode;
    if (filter.operatorCode)          body.operatorCode          = filter.operatorCode;
    if (filter.salesClass)            body.salesClass            = filter.salesClass;
    if (filter.salesManCode)          body.salesManCode          = filter.salesManCode;
    if (filter.incoterms)             body.incoterms             = filter.incoterms;
    const json = await fetchJson(`${AIR_HOUSE_BASE}/search`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(AIR_HOUSE_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid air-house list response: ${parsed.error.message}`);
    const { content, totalPages, totalElements, page: p, size: s } = parsed.data.data;
    return { content, totalPages, totalElements, page: p + 1, size: s };
  },
};
