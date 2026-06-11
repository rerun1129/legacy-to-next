import { z } from "zod";
import type { EnumOption } from "@/domain/enums/types";
import { pmsFetchJson } from "../pms-fetch";
import { ResponseParseError } from "../errors";

const ENUM_OPTION_SCHEMA = z.object({
  code: z.string(),
  label: z.string(),
  labelKo: z.string().nullish(),
  description: z.string().optional(),
});

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

// PMS EnumController 경로: /api/pms/enums/{name} (FMS /api/enums 와 분리된 모듈 prefix 규약)
export async function fetchPmsEnum(name: string): Promise<EnumOption[]> {
  const json = await pmsFetchJson(`/api/pms/enums/${name}`);
  const parsed = apiResponse(z.array(ENUM_OPTION_SCHEMA)).safeParse(json);
  if (!parsed.success) {
    throw new ResponseParseError(
      `Invalid PMS enum response for "${name}": ${parsed.error.message}`
    );
  }
  return parsed.data.data;
}
