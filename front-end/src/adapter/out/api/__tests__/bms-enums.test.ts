import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchBmsEnum } from '../bms/enums'
import { ResponseParseError } from '../errors'
import { NotFoundError } from '../errors'

vi.mock('../bms-fetch', () => ({
  bmsFetchJson: vi.fn(),
}))

import { bmsFetchJson } from '../bms-fetch'
const mockBmsFetchJson = vi.mocked(bmsFetchJson)

beforeEach(() => {
  vi.clearAllMocks()
})

describe('fetchBmsEnum', () => {
  it('성공: ApiResponse 래핑(.data.data) 구조를 언래핑해 EnumOption[] 반환', async () => {
    // BMS는 ApiResponse<T> 구조 — data.data 이중 언래핑 필수
    mockBmsFetchJson.mockResolvedValue({
      data: [
        { code: 'CREATED', label: 'Created', labelKo: '생성됨' },
        { code: 'GROUPED', label: 'Grouped', labelKo: '그룹됨' },
      ],
      message: 'OK',
    })

    const result = await fetchBmsEnum('DocumentStatus')

    expect(mockBmsFetchJson).toHaveBeenCalledWith('/api/bms/enums/DocumentStatus')
    expect(result).toEqual([
      { code: 'CREATED', label: 'Created', labelKo: '생성됨' },
      { code: 'GROUPED', label: 'Grouped', labelKo: '그룹됨' },
    ])
  })

  it('성공: labelKo 없는 항목도 허용 (nullish)', async () => {
    mockBmsFetchJson.mockResolvedValue({
      data: [
        { code: 'TAX', label: 'Tax Invoice' },
      ],
    })

    const result = await fetchBmsEnum('DocumentStatus')

    expect(result).toEqual([{ code: 'TAX', label: 'Tax Invoice' }])
  })

  it('404: bmsFetchJson이 NotFoundError를 throw하면 그대로 전파', async () => {
    mockBmsFetchJson.mockRejectedValue(new NotFoundError('resource', '/api/bms/enums/Unknown'))

    await expect(fetchBmsEnum('Unknown')).rejects.toThrow(NotFoundError)
  })

  it('파싱 실패: 응답 구조가 올바르지 않으면 ResponseParseError throw', async () => {
    // data 필드가 배열이 아닌 잘못된 구조
    mockBmsFetchJson.mockResolvedValue({ data: { unexpected: true } })

    await expect(fetchBmsEnum('DocumentStatus')).rejects.toThrow(ResponseParseError)
  })

  it('파싱 실패: 배열 항목 code 필드 누락 시 ResponseParseError throw', async () => {
    mockBmsFetchJson.mockResolvedValue({
      data: [{ label: 'No Code Field' }],
    })

    await expect(fetchBmsEnum('DocumentStatus')).rejects.toThrow(ResponseParseError)
  })
})
