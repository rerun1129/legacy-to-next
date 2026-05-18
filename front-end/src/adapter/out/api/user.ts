import { z } from "zod";
import type { UserPort, UserPageResult } from "@/application/user/ports";
import type {
  UserRow,
  UserDetail,
  UserFilter,
  CreateUserRequestDto,
  UpdateUserRequestDto,
  UserRole,
  UserScope,
} from "@/domain/user";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/user";

const USER_ROLE_SCHEMA = z.enum(["ADMIN", "USER"]) satisfies z.ZodType<UserRole>;

const PERMISSION_SCHEMA = z.enum(["CODE_MANAGE", "USER_MANAGE", "CUSTOMER_MANAGE", "CMS_MANAGE"]);

const USER_ROW_SCHEMA = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string().nullable().optional().transform((v) => v ?? null),
  role: USER_ROLE_SCHEMA,
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  updatedAt: z.string(),
}) satisfies z.ZodType<UserRow>;

const USER_DETAIL_SCHEMA = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string().nullable().optional().transform((v) => v ?? null),
  role: USER_ROLE_SCHEMA,
  active: z.boolean(),
  deletedAt: z.string().nullable().optional().transform((v) => v ?? null),
  createdAt: z.string(),
  updatedAt: z.string(),
  createdBy: z.string().nullable().optional().transform((v) => v ?? null),
  updatedBy: z.string().nullable().optional().transform((v) => v ?? null),
  permissions: z.array(PERMISSION_SCHEMA).default([]),
}) satisfies z.ZodType<UserDetail>;

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

function roleForBackend(role: UserFilter["role"]): UserRole | null {
  return role === "ALL" ? null : role;
}

function scopeForBackend(scope: UserScope): UserScope {
  // ALL을 포함해 항상 명시 전송 — BE가 null→ALL 처리하지만 명시 권장
  return scope;
}

export const API_USER_PORT: UserPort = {
  async search(filter, page, size = 20): Promise<UserPageResult> {
    const body: Record<string, unknown> = {
      page: page - 1,
      size,
      scope: scopeForBackend(filter.scope),
    };
    if (filter.username) body.username = filter.username;
    const r = roleForBackend(filter.role);
    if (r !== null) body.role = r;

    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    const parsed = apiResponse(pagedResult(USER_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid user search response: ${parsed.error.message}`);
    const d = parsed.data.data;
    return {
      content: d.content as UserRow[],
      totalPages: d.totalPages,
      totalElements: d.totalElements,
      page: d.page + 1,
      size: d.size,
    };
  },

  async getById(id) {
    const json = await adminFetchJson(`${BASE}/${id}`);
    const parsed = apiResponse(USER_DETAIL_SCHEMA).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid user detail response: ${parsed.error.message}`);
    return parsed.data.data as UserDetail;
  },

  async create(req: CreateUserRequestDto) {
    const json = await adminFetchJson(BASE, {
      method: "POST",
      body: JSON.stringify(req),
    });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid user create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async update(id, req: UpdateUserRequestDto) {
    // password가 빈 문자열이면 null로 정규화 — BE는 null/빈 모두 '미갱신'으로 처리
    const normalized: UpdateUserRequestDto = {
      ...req,
      password: req.password && req.password.length > 0 ? req.password : null,
      email: req.email && req.email.length > 0 ? req.email : null,
    };
    await adminFetchJson(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(normalized),
    });
  },

  async delete(id) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },
};
