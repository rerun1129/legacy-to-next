export interface EnumOption {
  code: string
  label: string
  labelKo?: string | null
  description?: string
}

export type EnumMap = Record<string, EnumOption[]>
export type EnumName = string
