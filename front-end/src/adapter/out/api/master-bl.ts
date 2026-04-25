import { z } from 'zod';
import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlFilter } from '@/domain/master-bl';
import { ApiError, NotFoundError, ResponseParseError } from './errors';

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

function toSearchParams(filter: MasterBlFilter): URLSearchParams {
  return Object.entries(filter)
    .filter(([, v]) => v != null)
    .reduce((p, [k, v]) => { p.set(k, String(v)); return p; }, new URLSearchParams());
}

export const apiMasterBlPort: MasterBlPort = {
  async list(filter: MasterBlFilter) {
    const res = await fetch(`/api/v1/master-bl?${toSearchParams(filter)}`);
    if (!res.ok) throw new ApiError('Failed to fetch master B/L list', res.status);
    const json = await res.json();
    const parsed = z.array(MasterBlRowSchema).safeParse(json.data?.content);
    if (!parsed.success) throw new ResponseParseError(`Invalid master B/L list response: ${parsed.error.message}`);
    return parsed.data;
  },
  async getById(id: string) {
    const res = await fetch(`/api/v1/master-bl/${id}`);
    if (res.status === 404) throw new NotFoundError('MasterBl', id);
    if (!res.ok) throw new ApiError(`Failed to fetch master B/L: ${id}`, res.status);
    const json = await res.json();
    const parsed = MasterBlRowSchema.safeParse(json.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid master B/L response: ${parsed.error.message}`);
    return parsed.data;
  },
  async delete(id: string) {
    const res = await fetch(`/api/v1/master-bl/${id}`, { method: 'DELETE' });
    if (res.status === 404) throw new NotFoundError('MasterBl', id);
    if (!res.ok) throw new ApiError(`Failed to delete master B/L: ${id}`, res.status);
  },
};
