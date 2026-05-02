import { z } from 'zod';
import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlFilter } from '@/domain/house-bl';
import { ResponseParseError } from './errors';
import { toSearchParams, fetchJson } from './utils';

const HOUSE_BL_ROW_SCHEMA = z.object({
  id: z.number(),
  hblNo: z.string(),
  bound: z.enum(['EXP', 'IMP']),
  docStatus: z.string().optional(),
  masterBlId: z.number().nullable().optional(),
  polCode: z.string().optional(),
  podCode: z.string().optional(),
  shipperCode: z.string().optional(),
  consigneeCode: z.string().optional(),
  createdAt: z.string().optional(),
  etd: z.string().optional(),
  eta: z.string().optional(),
});

export const API_HOUSE_BL_PORT: HouseBlPort = {
  async list(filter: HouseBlFilter) {
    const json = await fetchJson(`/api/v1/house-bl?${toSearchParams(filter as Record<string, unknown>)}`);
    const content = (json as { data?: { content?: unknown } })?.data?.content;
    const parsed = z.array(HOUSE_BL_ROW_SCHEMA).safeParse(content);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L list response: ${parsed.error.message}`);
    return parsed.data;
  },
  async getById(id: number) {
    const json = await fetchJson(`/api/v1/house-bl/${id}`);
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
    if (json === null) throw new ResponseParseError('Expected response body from POST /house-bl');
    const parsed = HOUSE_BL_ROW_SCHEMA.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid save response: ${parsed.error.message}`);
    return parsed.data;
  },
  async delete(id: number) {
    await fetchJson(`/api/v1/house-bl/${id}`, { method: 'DELETE' });
  },
};
