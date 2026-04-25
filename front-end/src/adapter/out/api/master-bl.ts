import { z } from 'zod';
import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlFilter } from '@/domain/master-bl';
import { NotFoundError, ResponseParseError } from './errors';
import { toSearchParams, fetchJson } from './utils';

const MASTER_BL_ROW_SCHEMA = z.object({
  id: z.string().optional(),
  mblNo: z.string(),
  bound: z.enum(['EXP', 'IMP']),
  pol: z.string(),
  pod: z.string(),
  etd: z.string(),
  eta: z.string(),
  shipperCode: z.string(),
  consigneeCode: z.string(),
});

export const API_MASTER_BL_PORT: MasterBlPort = {
  async list(filter: MasterBlFilter) {
    const json = await fetchJson(`/api/v1/master-bl?${toSearchParams(filter as Record<string, unknown>)}`);
    const content = (json as { data?: { content?: unknown } })?.data?.content;
    const parsed = z.array(MASTER_BL_ROW_SCHEMA).safeParse(content);
    if (!parsed.success) throw new ResponseParseError(`Invalid master B/L list response: ${parsed.error.message}`);
    return parsed.data;
  },
  async getById(id: string) {
    const json = await fetchJson(`/api/v1/master-bl/${id}`);
    if (json === null) throw new NotFoundError('MasterBl', id);
    const parsed = MASTER_BL_ROW_SCHEMA.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid master B/L response: ${parsed.error.message}`);
    return parsed.data;
  },
  async delete(id: string) {
    const json = await fetchJson(`/api/v1/master-bl/${id}`, { method: 'DELETE' });
    if (json === null) throw new NotFoundError('MasterBl', id);
  },
};
