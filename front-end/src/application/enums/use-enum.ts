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

export function useEnum(name: EnumName) {
  return useQuery({
    queryKey: enumKeys.one(name),
    queryFn: () => enumPort.fetchEnum(name),
    // ENUM 값은 배포 중 변경되지 않으므로 무기한 캐싱
    staleTime: Infinity,
    retry: false,
  })
}

export function useEnums(names: EnumName[]) {
  return useQuery({
    queryKey: enumKeys.many(names),
    queryFn: () => enumPort.fetchEnums(names),
    staleTime: Infinity,
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
