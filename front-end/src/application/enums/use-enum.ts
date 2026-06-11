import { useLocale, useTranslations } from 'next-intl'
import { useQuery } from '@tanstack/react-query'
import type { ComboBoxOption } from '@/components/shared/inputs/_types'
import type { EnumName } from '@/domain/enums/types'
import { enumPort } from './bindings'

export const enumKeys = {
  all: ['enums'] as const,
  one: (name: string) => [...enumKeys.all, 'one', name] as const,
  many: (names: string[]) => [...enumKeys.all, 'many', [...names].sort().join(',')] as const,
}

// 공통코드 동적 관리 도입으로 무기한 캐시 가정 폐기 — 5분 TTL로 전환
const ENUM_STALE_TIME = 300_000;

export function useEnum(name: EnumName) {
  return useQuery({
    queryKey: enumKeys.one(name),
    queryFn: () => enumPort.fetchEnum(name),
    staleTime: ENUM_STALE_TIME,
    retry: false,
  })
}

export function useEnums(names: EnumName[]) {
  return useQuery({
    queryKey: enumKeys.many(names),
    queryFn: () => enumPort.fetchEnums(names),
    staleTime: ENUM_STALE_TIME,
    retry: false,
  })
}

export function useEnumOptions(name: EnumName): {
  options: ComboBoxOption[]
  isLoading: boolean
  error: unknown
  placeholder: string | undefined
} {
  const locale = useLocale()
  const t = useTranslations('common')
  const { data, isLoading, error } = useEnum(name)
  return {
    options: data?.map((o) => ({ value: o.code, label: locale === 'ko' && o.labelKo ? o.labelKo : o.label })) ?? [],
    isLoading,
    error,
    placeholder: isLoading ? t('loading') : undefined,
  }
}
