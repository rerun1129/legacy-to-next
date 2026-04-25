import { z } from 'zod';
import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlFilter } from '@/domain/master-bl';
import { ApiError, NotFoundError, ResponseParseError } from './errors';
import { toSearchParams } from './utils';

const MasterBlRowSchema = z.object({
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

export const API_MASTER_BL_PORT: MasterBlPort = {
  async list(filter: MasterBlFilter) {
    const json = await fetchJson(`/api/v1/master-bl?${toSearchParams(filter as Record<string, unknown>)}`);
    const content = (json as { data?: { content?: unknown } })?.data?.content;
    const parsed = z.array(MasterBlRowSchema).safeParse(content);
    if (!parsed.success) throw new ResponseParseError(`Invalid master B/L list response: ${parsed.error.message}`);
    return parsed.data;
  },
  async getById(id: string) {
    const json = await fetchJson(`/api/v1/master-bl/${id}`);
    if (json === null) throw new NotFoundError('MasterBl', id);
    const parsed = MasterBlRowSchema.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid master B/L response: ${parsed.error.message}`);
    return parsed.data;
  },
  async delete(id: string) {
    const json = await fetchJson(`/api/v1/master-bl/${id}`, { method: 'DELETE' });
    if (json === null) throw new NotFoundError('MasterBl', id);
  },
};
