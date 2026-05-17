import { z } from "zod";
import type { PartnerPort, PartnerPageResult } from "@/application/partner/ports";
import type {
  PartnerRow,
  PartnerDetail,
  PartnerFilter,
  PartnerType,
  PartnerScope,
  CreatePartnerRequestDto,
  UpdatePartnerRequestDto,
} from "@/domain/partner";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/partner";

const PARTNER_TYPE_SCHEMA = z.enum([
  "FORWARDER",
  "SHIPPER",
  "CONSIGNEE",
  "CARRIER",
  "AGENT",
  "CUSTOMS_BROKER",
]) satisfies z.ZodType<PartnerType>;

const PARTNER_ROW_SCHEMA = z.object({
  id: z.number(),
  partnerCode: z.string(),
  name: z.string(),
  partnerType: PARTNER_TYPE_SCHEMA,
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<PartnerRow>;

const PARTNER_DETAIL_SCHEMA = z.object({
  id: z.number(),
  partnerCode: z.string(),
  partnerType: PARTNER_TYPE_SCHEMA,
  name: z.string(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  businessNo: z.string().nullable().optional().transform((v) => v ?? null),
  representative: z.string().nullable().optional().transform((v) => v ?? null),
  phone: z.string().nullable().optional().transform((v) => v ?? null),
  email: z.string().nullable().optional().transform((v) => v ?? null),
  address: z.string().nullable().optional().transform((v) => v ?? null),
  memo: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<PartnerDetail>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

const pagedResult = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({
    content: z.array(schema),
    totalElements: z.number(),
    totalPages: z.number(),
    page: z.number(),
    size: z.number(),
  });

function scopeForBackend(scope: PartnerScope): PartnerScope {
  return scope;
}

export const API_PARTNER_PORT: PartnerPort = {
  async search(filter: PartnerFilter, page: number, size = 20): Promise<PartnerPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.partnerCode) body.partnerCode = filter.partnerCode;
    if (filter.name) body.name = filter.name;
    if (filter.partnerType !== "ALL") body.partnerType = filter.partnerType;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(PARTNER_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid partner search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as PartnerRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(PARTNER_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid partner detail response: ${parsed.error.message}`);
    return parsed.data.data as PartnerDetail;
  },

  async create(req: CreatePartnerRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid partner create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdatePartnerRequestDto) {
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(req),
    });
  },

  async delete(id: number) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },
};
