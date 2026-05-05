import type { EnumMap, EnumName, EnumOption } from '@/domain/enums/types'

export interface EnumPort {
  fetchEnum(name: EnumName): Promise<EnumOption[]>
  fetchEnums(names: EnumName[]): Promise<{ enums: EnumMap; notFound: string[] }>
}
