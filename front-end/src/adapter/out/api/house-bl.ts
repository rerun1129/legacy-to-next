import { z } from 'zod';
import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlFilter } from '@/domain/house-bl';
import { ApiError, NotFoundError, ResponseParseError } from './errors';

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

function toSearchParams(filter: HouseBlFilter): URLSearchParams {
  return Object.entries(filter)
    .filter(([, v]) => v != null)
    .reduce((p, [k, v]) => { p.set(k, String(v)); return p; }, new URLSearchParams());
}

export const apiHouseBlPort: HouseBlPort = {
  async list(filter: HouseBlFilter) {
    const res = await fetch(`/api/v1/house-bl?${toSearchParams(filter)}`);
    if (!res.ok) throw new ApiError('Failed to fetch house B/L list', res.status);
    const json = await res.json();
    const parsed = z.array(HouseBlRowSchema).safeParse(json.data?.content);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L list response: ${parsed.error.message}`);
    return parsed.data;
  },
  async getById(id: string) {
    const res = await fetch(`/api/v1/house-bl/${id}`);
    if (res.status === 404) throw new NotFoundError('HouseBl', id);
    if (!res.ok) throw new ApiError(`Failed to fetch house B/L: ${id}`, res.status);
    const json = await res.json();
    const parsed = HouseBlRowSchema.safeParse(json.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L response: ${parsed.error.message}`);
    return parsed.data;
  },
  async save(data) {
    const res = await fetch('/api/v1/house-bl', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) throw new ApiError('Failed to save house B/L', res.status);
    const json = await res.json();
    const parsed = HouseBlRowSchema.safeParse(json.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid save response: ${parsed.error.message}`);
    return parsed.data;
  },
  async delete(id: string) {
    const res = await fetch(`/api/v1/house-bl/${id}`, { method: 'DELETE' });
    if (res.status === 404) throw new NotFoundError('HouseBl', id);
    if (!res.ok) throw new ApiError(`Failed to delete house B/L: ${id}`, res.status);
  },
};
