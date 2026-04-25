import { z } from 'zod';
import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlFilter } from '@/domain/house-bl';
import { NotFoundError, ResponseParseError } from './errors';
import { toSearchParams, fetchJson } from './utils';

const HOUSE_BL_ROW_SCHEMA = z.object({
  no: z.number().optional(),
  hbl: z.string(),
  expImp: z.enum(['EXP', 'IMP']),
  docStatus: z.enum(['ok', 'inprog', 'draft']),
  mbl: z.string(),
  sType: z.string(),
  lType: z.string(),
  etd: z.string(),
  eta: z.string(),
  regDate: z.string(),
  pol: z.string(),
  pod: z.string(),
  vessel: z.string(),
  voyage: z.string(),
  shipper: z.string(),
  consignee: z.string(),
});

export const API_HOUSE_BL_PORT: HouseBlPort = {
  async list(filter: HouseBlFilter) {
    const json = await fetchJson(`/api/v1/house-bl?${toSearchParams(filter as Record<string, unknown>)}`);
    const content = (json as { data?: { content?: unknown } })?.data?.content;
    const parsed = z.array(HOUSE_BL_ROW_SCHEMA).safeParse(content);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L list response: ${parsed.error.message}`);
    return parsed.data;
  },
  async getById(id: string) {
    const json = await fetchJson(`/api/v1/house-bl/${id}`);
    if (json === null) throw new NotFoundError('HouseBl', id);
    const parsed = HOUSE_BL_ROW_SCHEMA.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L response: ${parsed.error.message}`);
    return parsed.data;
  },
  async save(data) {
    const json = await fetchJson('/api/v1/house-bl', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (json === null) throw new NotFoundError('HouseBl', '(unknown)');
    const parsed = HOUSE_BL_ROW_SCHEMA.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid save response: ${parsed.error.message}`);
    return parsed.data;
  },
  async delete(id: string) {
    const json = await fetchJson(`/api/v1/house-bl/${id}`, { method: 'DELETE' });
    if (json === null) throw new NotFoundError('HouseBl', id);
  },
};
