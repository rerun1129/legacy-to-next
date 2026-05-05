import { z } from 'zod'
import type { EnumMap, EnumOption } from '@/domain/enums/types'
import { ResponseParseError } from './errors'
import { fetchJson } from './utils'

const ENUMS_BASE = '/api/enums'

const ENUM_OPTION_SCHEMA = z.object({
  code: z.string(),
  label: z.string(),
  description: z.string().optional(),
})

const ENUM_MAP_RESPONSE_SCHEMA = z.object({
  enums: z.record(z.string(), z.array(ENUM_OPTION_SCHEMA)),
  notFound: z.array(z.string()),
})

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({
    data: schema,
    message: z.string().optional(),
  })

export async function fetchEnum(name: string): Promise<EnumOption[]> {
  const json = await fetchJson(`${ENUMS_BASE}/${name}`)
  const parsed = apiResponse(z.array(ENUM_OPTION_SCHEMA)).safeParse(json)
  if (!parsed.success) throw new ResponseParseError(`Invalid enum response for "${name}": ${parsed.error.message}`)
  return parsed.data.data
}

export async function fetchEnums(names: string[]): Promise<{ enums: EnumMap; notFound: string[] }> {
  const query = names.join(',')
  const json = await fetchJson(`${ENUMS_BASE}?names=${encodeURIComponent(query)}`)
  const parsed = apiResponse(ENUM_MAP_RESPONSE_SCHEMA).safeParse(json)
  if (!parsed.success) throw new ResponseParseError(`Invalid enums response for "${query}": ${parsed.error.message}`)
  return parsed.data.data
}
