import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import React from 'react'
import { useEnumOptions, useEnums, enumKeys } from '../use-enum'
// useEnumOptions는 useLocale/useTranslations를 호출하므로
// NextIntlClientProvider + QueryClientProvider 를 함께 공급하는 Providers를 wrapper로 사용
import { Providers } from '@/test/render-with-providers'

vi.mock('../bindings', () => ({
  enumPort: {
    fetchEnum: vi.fn(),
    fetchEnums: vi.fn(),
  },
}))

import { enumPort } from '../bindings'
const mockFetchEnum = vi.mocked(enumPort.fetchEnum)
const mockFetchEnums = vi.mocked(enumPort.fetchEnums)

function makeWrapper() {
  return function Wrapper({ children }: { children: React.ReactNode }) {
    return <Providers>{children}</Providers>
  }
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('useEnumOptions', () => {
  it('초기 상태: isLoading=true, options=[], placeholder="로딩 중..."', () => {
    // 절대 resolve되지 않는 Promise로 로딩 상태 유지
    mockFetchEnum.mockReturnValue(new Promise(() => {}))

    const { result } = renderHook(() => useEnumOptions('Per'), {
      wrapper: makeWrapper(),
    })

    expect(result.current.isLoading).toBe(true)
    expect(result.current.options).toEqual([])
    expect(result.current.placeholder).toBe('불러오는 중…')
  })

  it('성공 후: isLoading=false, options에 value/label 매핑, placeholder=undefined', async () => {
    mockFetchEnum.mockResolvedValue([
      { code: 'SHP', label: 'Ship' },
      { code: 'AIR', label: 'Air' },
    ])

    const { result } = renderHook(() => useEnumOptions('Per'), {
      wrapper: makeWrapper(),
    })

    await waitFor(() => expect(result.current.isLoading).toBe(false))

    expect(result.current.options).toEqual([
      { value: 'SHP', label: 'Ship' },
      { value: 'AIR', label: 'Air' },
    ])
    expect(result.current.placeholder).toBeUndefined()
    expect(result.current.error).toBeNull()
  })

  it('에러 시: isLoading=false, options=[], error 설정', async () => {
    const fetchError = new Error('Network error')
    mockFetchEnum.mockRejectedValue(fetchError)

    const { result } = renderHook(() => useEnumOptions('Per'), {
      wrapper: makeWrapper(),
    })

    await waitFor(() => expect(result.current.isLoading).toBe(false))

    expect(result.current.options).toEqual([])
    expect(result.current.error).toBeTruthy()
  })
})

describe('useEnums — many 키 정렬', () => {
  it('names 순서가 달라도 동일한 캐시 키를 생성', () => {
    const keyAB = enumKeys.many(['Bound', 'Per'])
    const keyBA = enumKeys.many(['Per', 'Bound'])

    expect(keyAB).toEqual(keyBA)
  })

  it('성공: {enums, notFound} 구조 반환', async () => {
    mockFetchEnums.mockResolvedValue({
      enums: {
        Per: [{ code: 'SHP', label: 'Ship' }],
        Bound: [{ code: 'EXP', label: 'Export' }],
      },
      notFound: [],
    })

    const { result } = renderHook(() => useEnums(['Per', 'Bound']), {
      wrapper: makeWrapper(),
    })

    await waitFor(() => expect(result.current.isLoading).toBe(false))

    expect(result.current.data?.enums).toHaveProperty('Per')
    expect(result.current.data?.notFound).toEqual([])
  })
})
