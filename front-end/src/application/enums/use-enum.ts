import { useQuery } from '@tanstack/react-query'
import type { DropBoxOption } from '@/components/shared/inputs/_types'
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
  options: DropBoxOption[]
  isLoading: boolean
  error: unknown
  placeholder: string | undefined
} {
  const { data, isLoading, error } = useEnum(name)
  return {
    options: data?.map((o) => ({ value: o.code, label: o.label })) ?? [],
    isLoading,
    error,
    placeholder: isLoading ? '로딩 중...' : undefined,
  }
}
