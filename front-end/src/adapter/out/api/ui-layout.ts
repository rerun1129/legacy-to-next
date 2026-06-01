import { z } from "zod";
import type { UiLayoutPort } from "@/application/ui-layout/ports";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const BASE = "/api/admin/ui-layout";

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

export const API_UI_LAYOUT_PORT: UiLayoutPort = {
  async load(storageKey: string): Promise<unknown | null> {
    const json = await adminFetchJson(`${BASE}/${encodeURIComponent(storageKey)}`);
    const parsed = apiResponse(z.unknown().nullable()).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid ui-layout load response for key "${storageKey}": ${parsed.error.message}`);
    }
    return parsed.data.data ?? null;
  },

  async save(storageKey: string, payload: unknown): Promise<void> {
    await adminFetchJson(`${BASE}/${encodeURIComponent(storageKey)}`, {
      method: "PUT",
      body: JSON.stringify({ payload }),
    });
  },

  async remove(storageKey: string): Promise<void> {
    await adminFetchJson(`${BASE}/${encodeURIComponent(storageKey)}`, {
      method: "DELETE",
    });
  },
};
