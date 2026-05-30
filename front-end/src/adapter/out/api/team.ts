import { z } from "zod";
import type { TeamPort } from "@/application/team/ports";
import type { TeamRow } from "@/domain/team";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/teams";

const TEAM_ROW_SCHEMA = z.object({
  id: z.number(),
  teamCode: z.string(),
  name: z.string(),
  sortOrder: z.number().nullable().optional().transform((v) => v ?? null),
  active: z.boolean(),
}) satisfies z.ZodType<TeamRow>;

const TEAM_AUTOCOMPLETE_SCHEMA = z.object({
  id: z.number(),
  code: z.string(),
  name: z.string(),
}) satisfies z.ZodType<Required<Pick<CodeBoxSuggestion, "id" | "code" | "name">>>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_TEAM_PORT: TeamPort = {
  async listAll(): Promise<TeamRow[]> {
    const json = await adminFetchJson(BASE);
    const parsed = apiResponse(z.array(TEAM_ROW_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid team list response: ${parsed.error.message}`);
    }
    return parsed.data.data as TeamRow[];
  },

  async autocomplete(q: string, limit = 20): Promise<CodeBoxSuggestion[]> {
    const params = new URLSearchParams({ q, limit: String(limit) });
    const json = await adminFetchJson(`${BASE}/autocomplete?${params}`);
    const parsed = apiResponse(z.array(TEAM_AUTOCOMPLETE_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid team autocomplete response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },
};
