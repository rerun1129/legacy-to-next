import { z } from "zod";
import type { SubscriptionPort } from "@/application/subscription/ports";
import type {
  SubscriptionItem,
  SaveSubscriptionChangesRequestDto,
  SaveSubscriptionChangesResultDto,
} from "@/domain/subscription";
import { adminFetchJson } from "./admin-fetch";
import { ResponseParseError } from "./errors";

const SUBSCRIPTION_ITEM_SCHEMA = z.object({
  id: z.number(),
  subscriberId: z.number(),
  moduleCode: z.string(),
  startDate: z.string(),
  endDate: z.string(),
  active: z.boolean(),
  createdAt: z.string(),
  updatedAt: z.string(),
}) satisfies z.ZodType<SubscriptionItem>;

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

const SAVE_CHANGES_RESULT_SCHEMA = z.object({
  createdCount: z.number(),
  updatedCount: z.number(),
  deletedCount: z.number(),
});

export const API_SUBSCRIPTION_PORT: SubscriptionPort = {
  async listBySubscriber(subscriberId: number): Promise<SubscriptionItem[]> {
    const json = await adminFetchJson(
      `/api/admin/subscriber/${subscriberId}/subscription`,
    );
    const parsed = apiResponse(z.array(SUBSCRIPTION_ITEM_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid subscription list response: ${parsed.error.message}`,
      );
    }
    return parsed.data.data as SubscriptionItem[];
  },

  async saveChanges(
    subscriberId: number,
    req: SaveSubscriptionChangesRequestDto,
  ): Promise<SaveSubscriptionChangesResultDto> {
    const json = await adminFetchJson(
      `/api/admin/subscriber/${subscriberId}/subscription/save-changes`,
      { method: "POST", body: JSON.stringify(req) },
    );
    const parsed = apiResponse(SAVE_CHANGES_RESULT_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid subscription save-changes response: ${parsed.error.message}`,
      );
    }
    return parsed.data.data;
  },
};
