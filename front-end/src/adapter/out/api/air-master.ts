import { z } from 'zod';
import type { AirMasterPort, AirMasterPageResult } from '@/application/air-master/ports';
import type { AirMasterRow, AirMasterFilter } from '@/domain/air-master';
import { ResponseParseError } from './errors';
import { fetchJson } from './utils';

const AIR_MASTER_BASE = '/api/air-master';

const AIR_MASTER_ROW_SCHEMA = z.object({
  id: z.number(),
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
  airlineCode: z.string().nullable().transform((v) => v ?? ''),
  houseBlCount: z.number().nullable().transform((v) => v ?? 0),
  operatorCode: z.string().nullable().transform((v) => v ?? ''),
  masterRefNo: z.string().nullable().transform((v) => v ?? ''),
  freightTerm: z.string().nullable().transform((v) => v ?? ''),
  teamCode: z.string().nullable().transform((v) => v ?? ''),
  teamName: z.string().nullable().transform((v) => v ?? ''),
}).transform((raw) => ({
  id: raw.id,
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
  airlineCode: raw.airlineCode,
  airlineName: '',
  houseBlCount: raw.houseBlCount,
  operatorCode: raw.operatorCode,
  masterRefNo: raw.masterRefNo,
  freightTerm: raw.freightTerm,
  teamCode: raw.teamCode,
  teamName: raw.teamName,
} satisfies AirMasterRow));

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

export const API_AIR_MASTER_PORT: AirMasterPort = {
  async list(filter: AirMasterFilter, page: number, size = 50): Promise<AirMasterPageResult> {
    const body: Record<string, unknown> = { page: page - 1, size };
    body.bound = filter.bound;
    body.dateKind = filter.dateKind;
    body.dateFrom = filter.dateFrom;
    body.dateTo = filter.dateTo;
    body.masterAwbKind = filter.masterAwbKind;
    if (filter.masterAwbValue)        body.masterAwbValue        = filter.masterAwbValue;
    body.partyKind = filter.partyKind;
    body.partyCode = filter.partyCode;
    if (filter.airlineCode)           body.airlineCode           = filter.airlineCode;
    body.portKind = filter.portKind;
    body.portCode = filter.portCode;
    if (filter.shipmentType)          body.shipmentType          = filter.shipmentType;
    if (filter.teamCode)              body.teamCode              = filter.teamCode;
    const json = await fetchJson(`${AIR_MASTER_BASE}/search`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(AIR_MASTER_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid air-master list response: ${parsed.error.message}`);
    const { content, totalPages, totalElements, page: p, size: s } = parsed.data.data;
    return { content, totalPages, totalElements, page: p + 1, size: s };
  },
};
