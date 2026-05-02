import { z } from 'zod';
import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlFilter } from '@/domain/house-bl';
import { ResponseParseError } from './errors';
import { toSearchParams, fetchJson } from './utils';
import { formatDateDisplay } from '@/lib/date';

const HOUSE_BL_BASE = '/api/house-bl';

const HOUSE_BL_ROW_SCHEMA = z.object({
  id: z.number().optional(),
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

const applyDateDisplay = (raw: z.infer<typeof HOUSE_BL_ROW_SCHEMA>) => ({
  ...raw,
  etd: formatDateDisplay(raw.etd),
  eta: formatDateDisplay(raw.eta),
  regDate: formatDateDisplay(raw.regDate),
});

export const API_HOUSE_BL_PORT: HouseBlPort = {
  async list(filter: HouseBlFilter) {
    const json = await fetchJson(`${HOUSE_BL_BASE}?${toSearchParams(filter as Record<string, unknown>)}`);
    const content = (json as { data?: { content?: unknown } })?.data?.content;
    const parsed = z.array(HOUSE_BL_ROW_SCHEMA).safeParse(content);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L list response: ${parsed.error.message}`);
    return parsed.data.map(applyDateDisplay);
  },
  async getById(id: number) {
    const json = await fetchJson(`${HOUSE_BL_BASE}/${id}`);
    const parsed = HOUSE_BL_ROW_SCHEMA.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid house B/L response: ${parsed.error.message}`);
    return applyDateDisplay(parsed.data);
  },
  async save(data) {
    const json = await fetchJson(HOUSE_BL_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (json === null) throw new ResponseParseError('Expected response body from POST /house-bl');
    const parsed = HOUSE_BL_ROW_SCHEMA.safeParse((json as { data?: unknown })?.data);
    if (!parsed.success) throw new ResponseParseError(`Invalid save response: ${parsed.error.message}`);
    return applyDateDisplay(parsed.data);
  },
  async delete(id: number) {
    await fetchJson(`${HOUSE_BL_BASE}/${id}`, { method: 'DELETE' });
  },
};
