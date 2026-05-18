import { z } from "zod";
import type { ButtonPolicyPort } from "@/application/access/button-policy/ports";
import type { ButtonPolicyRow, CreateButtonPolicyDto } from "@/domain/access/policy";
import { adminFetchJson } from "../admin-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/admin/access/button-policy";

const BUTTON_POLICY_ROW_SCHEMA = z.object({
  id: z.number(),
  buttonId: z.number(),
  attributeKey: z.string(),
  requiredValue: z.string(),
  updatedAt: z.string(),
}) satisfies z.ZodType<ButtonPolicyRow>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_BUTTON_POLICY_PORT: ButtonPolicyPort = {
  async listByButton(buttonId) {
    const json = await adminFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({ buttonId, page: 0, size: 200 }),
    });
    const parsed = apiResponse(
      z.object({ content: z.array(BUTTON_POLICY_ROW_SCHEMA) })
    ).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid button-policy list response: ${parsed.error.message}`);
    return parsed.data.data.content as ButtonPolicyRow[];
  },

  async create(req: CreateButtonPolicyDto) {
    const json = await adminFetchJson(BASE, { method: "POST", body: JSON.stringify(req) });
    const parsed = apiResponse(z.object({ id: z.number() })).safeParse(json);
    if (!parsed.success) throw new ResponseParseError(`Invalid button-policy create response: ${parsed.error.message}`);
    return parsed.data.data.id;
  },

  async delete(id) {
    await adminFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },

  async deleteMany(ids) {
    await adminFetchJson(`${BASE}/bulk`, {
      method: "DELETE",
      body: JSON.stringify({ ids }),
    });
  },
};
