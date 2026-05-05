import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchEnum, fetchEnums } from '../enums'
import { ResponseParseError } from '../errors'
import { NotFoundError } from '../errors'

vi.mock('../utils', () => ({
  fetchJson: vi.fn(),
}))

import { fetchJson } from '../utils'
const mockFetchJson = vi.mocked(fetchJson)

beforeEach(() => {
  vi.clearAllMocks()
})

describe('fetchEnum', () => {
  it('성공: 올바른 JSON 응답 시 EnumOption[] 반환', async () => {
    mockFetchJson.mockResolvedValue({
      data: [
        { code: 'SHP', label: 'Ship' },
        { code: 'AIR', label: 'Air', description: '항공' },
      ],
    })

    const result = await fetchEnum('Per')

    expect(mockFetchJson).toHaveBeenCalledWith('/api/enums/Per')
    expect(result).toEqual([
      { code: 'SHP', label: 'Ship' },
      { code: 'AIR', label: 'Air', description: '항공' },
    ])
  })

  it('404: fetchJson이 NotFoundError를 throw하면 그대로 전파', async () => {
    mockFetchJson.mockRejectedValue(new NotFoundError('resource', '/api/enums/Unknown'))

    await expect(fetchEnum('Unknown')).rejects.toThrow(NotFoundError)
  })

  it('파싱 실패: 응답 구조가 올바르지 않으면 ResponseParseError throw', async () => {
    // data 필드가 객체가 아닌 잘못된 구조
    mockFetchJson.mockResolvedValue({ data: { unexpected: true } })

    await expect(fetchEnum('Per')).rejects.toThrow(ResponseParseError)
  })
})

describe('fetchEnums', () => {
  it('성공: names를 콤마 join해 요청하고 {enums, notFound} 반환', async () => {
    mockFetchJson.mockResolvedValue({
      data: {
        enums: {
          Per: [{ code: 'SHP', label: 'Ship' }],
          Bound: [{ code: 'EXP', label: 'Export' }],
        },
        notFound: [],
      },
    })

    const result = await fetchEnums(['Per', 'Bound'])

    expect(mockFetchJson).toHaveBeenCalledWith('/api/enums?names=Per%2CBound')
    expect(result.enums).toEqual({
      Per: [{ code: 'SHP', label: 'Ship' }],
      Bound: [{ code: 'EXP', label: 'Export' }],
    })
    expect(result.notFound).toEqual([])
  })

  it('notFound 포함: 일부 ENUM을 찾지 못한 경우 notFound 배열에 포함', async () => {
    mockFetchJson.mockResolvedValue({
      data: {
        enums: { Per: [{ code: 'SHP', label: 'Ship' }] },
        notFound: ['Missing'],
      },
    })

    const result = await fetchEnums(['Per', 'Missing'])

    expect(result.notFound).toEqual(['Missing'])
  })
})
