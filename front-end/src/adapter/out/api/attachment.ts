import { z } from 'zod';
import type { BlAttachmentPort } from '@/application/attachment/ports';
import type { BlAttachment, AttachmentBlKind } from '@/domain/attachment';
import { ResponseParseError } from './errors';
import { fetchJson, fetchBlob } from './utils';

const BL_ATTACHMENT_BASE = '/api/bl-attachment';

const BL_ATTACHMENT_SCHEMA = z.object({
  id: z.number(),
  blKind: z.enum(['HOUSE', 'MASTER', 'TRUCK', 'NON_BL']),
  blId: z.number(),
  originalFilename: z.string(),
  // BE가 null 또는 미포함으로 내려올 수 있어 null-safe 변환 적용
  contentType: z.string().nullable().optional().transform((v) => v ?? null),
  fileSize: z.number(),
  uploadedBy: z.string(),
  createdAt: z.string(),
});

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({
    data: schema,
    message: z.string().nullable().optional(),
  });

export const API_BL_ATTACHMENT_PORT: BlAttachmentPort = {
  async list(blKind: AttachmentBlKind, blId: number): Promise<BlAttachment[]> {
    const json = await fetchJson(`${BL_ATTACHMENT_BASE}?blKind=${blKind}&blId=${blId}`);
    const parsed = apiResponse(z.array(BL_ATTACHMENT_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid list response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async upload(blKind: AttachmentBlKind, blId: number, file: File): Promise<number> {
    const form = new FormData();
    form.append('file', file);
    form.append('blKind', blKind);
    form.append('blId', String(blId));
    // Content-Type 수동 지정 금지 — FormData boundary는 브라우저가 자동 세팅
    const json = await fetchJson(BL_ATTACHMENT_BASE, { method: 'POST', body: form });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid upload response: ${parsed.error.message}`);
    }
    return parsed.data.data.id;
  },

  async download(id: number): Promise<Blob> {
    return fetchBlob(`${BL_ATTACHMENT_BASE}/${id}/download`);
  },

  async remove(id: number): Promise<void> {
    await fetchJson(`${BL_ATTACHMENT_BASE}/${id}`, { method: 'DELETE' });
  },
};
