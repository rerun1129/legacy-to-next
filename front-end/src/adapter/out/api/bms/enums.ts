import { z } from "zod";
import type { EnumOption } from "@/domain/enums/types";
import { bmsFetchJson } from "../bms-fetch";
import { ResponseParseError } from "../errors";

const ENUM_OPTION_SCHEMA = z.object({
  code: z.string(),
  label: z.string(),
  labelKo: z.string().nullish(),
  description: z.string().optional(),
});

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

// BMS EnumController 경로: /api/bms/enums/{name}
// BMS는 ApiResponse<T> 래핑 구조이므로 parsed.data.data 로 언래핑
export async function fetchBmsEnum(name: string): Promise<EnumOption[]> {
  const json = await bmsFetchJson(`/api/bms/enums/${name}`);
  const parsed = apiResponse(z.array(ENUM_OPTION_SCHEMA)).safeParse(json);
  if (!parsed.success) {
    throw new ResponseParseError(
      `Invalid BMS enum response for "${name}": ${parsed.error.message}`
    );
  }
  return parsed.data.data;
}
