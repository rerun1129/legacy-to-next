import { z } from "zod";
import type { MenuPolicyPort } from "@/application/access/menu-policy/ports";
import type { MenuPolicyRow, CreateMenuPolicyDto } from "@/domain/access/policy";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/access/menu-policy";

const MENU_POLICY_ROW_SCHEMA = z.object({
  id: z.number(),
  menuId: z.number(),
  attributeKey: z.string(),
  requiredValue: z.string(),
  updatedAt: z.string(),
}) satisfies z.ZodType<MenuPolicyRow>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_MENU_POLICY_PORT: MenuPolicyPort = {
  async listByMenu(menuId) {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ menuId, page: 0, size: 200 }),
    });
    const parsed = apiResponse(
      z.object({ content: z.array(MENU_POLICY_ROW_SCHEMA) })
    ).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid menu-policy list response: ${parsed.error.message}`);
    return parsed.data.data.content as MenuPolicyRow[];
  },

  async create(req: CreateMenuPolicyDto) {
    const json = await adminFetchJson(BASE, { method: "POST", body: JSON.stringify(req) });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid menu-policy create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async delete(id) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },
};
