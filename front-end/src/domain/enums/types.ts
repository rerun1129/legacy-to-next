export interface EnumOption {
  code: string
  label: string
  description?: string
}

export type EnumMap = Record<string, EnumOption[]>
export type EnumName = string
