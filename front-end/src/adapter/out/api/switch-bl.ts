import { z } from 'zod';
import type { SwitchBlPort } from '@/application/switch-bl/ports';
import type { SwitchBl, CreateSwitchBlRequest, UpdateSwitchBlRequest } from '@/domain/switch-bl';
import { ResponseParseError } from './errors';
import { fetchJson } from './utils';

const SWITCH_BL_BASE = '/api/switch-bl';

const SWITCH_BL_SCHEMA = z.object({
  id: z.number(),
  houseBlId: z.number(),
  switchBlNo: z.string().nullable().optional(),
  shipperCode: z.string(),
  shipperAddress: z.string().nullable().optional(),
  consigneeCode: z.string().nullable().optional(),
  consigneeAddress: z.string().nullable().optional(),
  notifyCode: z.string().nullable().optional(),
  notifyAddress: z.string().nullable().optional(),
  description: z.object({
    marks: z.string().nullable().optional(),
    natureQuantity: z.string().nullable().optional(),
  }).nullable().optional(),
});

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({
    data: schema,
    message: z.string().optional(),
  });

export const API_SWITCH_BL_PORT: SwitchBlPort = {
  async getByHouseBlId(houseBlId: number): Promise<SwitchBl | null> {
    const json = await fetchJson(`${SWITCH_BL_BASE}/by-house-bl/${houseBlId}`);
    if (json === null) return null;
    const schema = z.object({
      data: SWITCH_BL_SCHEMA.nullable().optional(),
      message: z.string().optional(),
    });
    const parsed = schema.safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid getByHouseBlId response: ${parsed.error.message}`);
    return parsed.data.data ?? null;
  },

  async create(req: CreateSwitchBlRequest): Promise<SwitchBl> {
    const json = await fetchJson(SWITCH_BL_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SWITCH_BL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid create response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async update(id: number, req: UpdateSwitchBlRequest): Promise<SwitchBl> {
    const json = await fetchJson(`${SWITCH_BL_BASE}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(SWITCH_BL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid update response: ${parsed.error.message}`);
    return parsed.data.data;
  },

  async delete(id: number): Promise<void> {
    await fetchJson(`${SWITCH_BL_BASE}/${id}`, { method: 'DELETE' });
  },
};
