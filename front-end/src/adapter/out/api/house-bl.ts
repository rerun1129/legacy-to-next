import { z } from 'zod';
import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlFilter } from '@/domain/house-bl';
import { ApiError, NotFoundError, ResponseParseError } from './errors';
import { toSearchParams } from './utils';

const HouseBlRowSchema = z.object({
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

async function fetchJson(input: RequestInfo, init?: RequestInit): Promise<unknown> {
  let res: Response;
  try {
    res = await fetch(input, init);
  } catch (e) {
    throw new ApiError('Network error', undefined, e);
  }
  if (res.status === 404) return null;
  if (!res.ok) throw new ApiError(`HTTP ${res.status}`, res.status);
  try {
    return await res.json();
  } catch (e) {
    throw new ResponseParseError('Failed to parse response JSON', e);
  }
}

export const API_HOUSE_BL_PORT: HouseBlPort = {
  async list(filter: HouseBlFilter) {
    const json = await fetchJson(`/api/v1/house-bl?${toSearchParams(filter as Record<string, unknown>)}`);
    const content = (json as { data?: { content?: unknown } })?.data?.content;
    const parsed = z.array(HouseBlRowSchema).safeParse(content);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L list response: ${parsed.error.message}`);
    return parsed.data;
  },
  async getById(id: string) {
    const json = await fetchJson(`/api/v1/house-bl/${id}`);
    if (json === null) throw new NotFoundError('HouseBl', id);
    const parsed = HouseBlRowSchema.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L response: ${parsed.error.message}`);
    return parsed.data;
  },
  async save(data) {
    const json = await fetchJson('/api/v1/house-bl', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    const parsed = HouseBlRowSchema.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid save response: ${parsed.error.message}`);
    return parsed.data;
  },
  async delete(id: string) {
    const json = await fetchJson(`/api/v1/house-bl/${id}`, { method: 'DELETE' });
    if (json === null) throw new NotFoundError('HouseBl', id);
  },
};
