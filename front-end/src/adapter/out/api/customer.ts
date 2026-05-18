import { z } from "zod";
import type { CustomerPort, CustomerPageResult } from "@/application/customer/ports";
import type {
  CustomerRow,
  CustomerDetail,
  CustomerFilter,
  CustomerType,
  CustomerScope,
  CreateCustomerRequestDto,
  UpdateCustomerRequestDto,
} from "@/domain/customer";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/customer";

const CUSTOMER_TYPE_SCHEMA = z.enum([
  "FORWARDER",
  "SHIPPER",
  "CONSIGNEE",
  "CARRIER",
  "AGENT",
  "CUSTOMS_BROKER",
]) satisfies z.ZodType<CustomerType>;

const CUSTOMER_ROW_SCHEMA = z.object({
  id: z.number(),
  customerCode: z.string(),
  name: z.string(),
  customerType: CUSTOMER_TYPE_SCHEMA,
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<CustomerRow>;

const CUSTOMER_DETAIL_SCHEMA = z.object({
  id: z.number(),
  customerCode: z.string(),
  customerType: CUSTOMER_TYPE_SCHEMA,
  name: z.string(),
  nameEn: z.string().nullable().optional().transform((v) => v ?? null),
  businessNo: z.string().nullable().optional().transform((v) => v ?? null),
  representative: z.string().nullable().optional().transform((v) => v ?? null),
  phone: z.string().nullable().optional().transform((v) => v ?? null),
  email: z.string().nullable().optional().transform((v) => v ?? null),
  address: z.string().nullable().optional().transform((v) => v ?? null),
  addressEn: z.string().nullable().optional().transform((v) => v ?? null),
  memo: z.string().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
}) satisfies z.ZodType<CustomerDetail>;

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

function scopeForBackend(scope: CustomerScope): CustomerScope {
  return scope;
}

export const API_CUSTOMER_PORT: CustomerPort = {
  async search(filter: CustomerFilter, page: number, size = 20): Promise<CustomerPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.customerCode) body.customerCode = filter.customerCode;
    if (filter.name) body.name = filter.name;
    if (filter.customerType !== "ALL") body.customerType = filter.customerType;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(CUSTOMER_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid customer search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as CustomerRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id: number) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(CUSTOMER_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid customer detail response: ${parsed.error.message}`);
    return parsed.data.data as CustomerDetail;
  },

  async create(req: CreateCustomerRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid customer create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id: number, req: UpdateCustomerRequestDto) {
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(req),
    });
  },

  async delete(id: number) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },

  async deleteMany(ids: number[]) {
    await adminFetchJson(`${BASE}/bulk`, {
      method: "DELETE",
      body: JSON.stringify({ ids }),
    });
  },
};
