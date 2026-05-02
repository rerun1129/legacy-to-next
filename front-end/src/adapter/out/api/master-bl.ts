import { z } from 'zod';
import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlFilter } from '@/domain/master-bl';
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

const CONSOLIDATED_HBL_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string().nullable(),
  shipperCode: z.string().nullable(),
  consigneeCode: z.string().nullable(),
});

const MASTER_BL_DETAIL_SCHEMA = MASTER_BL_ROW_SCHEMA.extend({
  freightTerm: z.enum(['PREPAID', 'COLLECT']).nullable(),
  pkgQty: z.number().nullable(),
  grossWeightKg: z.number().nullable(),
  cbm: z.number().nullable(),
  consolidatedHouseBls: z.array(CONSOLIDATED_HBL_SCHEMA),
  updatedAt: z.string().nullable(),
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
  async getById(id: number): Promise<MasterBlRow> {
    const json = await fetchJson(`${MASTER_BL_BASE}/${id}`);
    const parsed = apiResponse(MASTER_BL_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid detail response: ${parsed.error.message}`);
    return parsed.data.data as unknown as MasterBlRow;
  },
  async delete(id: number): Promise<void> {
    await fetchJson(`${MASTER_BL_BASE}/${id}`, { method: 'DELETE' });
  },
};
