import { describe, it, expect, vi, beforeEach } from 'vitest';
import { API_BL_ATTACHMENT_PORT } from '../attachment';
import { ResponseParseError } from '../errors';

vi.mock('../utils', () => ({
  fetchJson: vi.fn(),
  fetchBlob: vi.fn(),
}));

import { fetchJson, fetchBlob } from '../utils';
const mockFetchJson = vi.mocked(fetchJson);
const mockFetchBlob = vi.mocked(fetchBlob);

beforeEach(() => {
  vi.clearAllMocks();
});

const FIXTURE_ATTACHMENT = {
  id: 1,
  blKind: 'HOUSE',
  blId: 123,
  originalFilename: 'test.pdf',
  contentType: 'application/pdf',
  fileSize: 12345,
  uploadedBy: 'fms',
  createdAt: '2026-06-12T10:00:00',
};

describe('API_BL_ATTACHMENT_PORT.list', () => {
  it('성공: data 배열을 언래핑해 BlAttachment[] 반환', async () => {
    mockFetchJson.mockResolvedValue({ data: [FIXTURE_ATTACHMENT], message: null });

    const result = await API_BL_ATTACHMENT_PORT.list('HOUSE', 123);

    expect(mockFetchJson).toHaveBeenCalledWith('/api/bl-attachment?blKind=HOUSE&blId=123');
    expect(result).toHaveLength(1);
    expect(result[0].id).toBe(1);
    expect(result[0].originalFilename).toBe('test.pdf');
  });

  it('contentType null: null-safe transform 적용 — null 그대로 반환', async () => {
    mockFetchJson.mockResolvedValue({
      data: [{ ...FIXTURE_ATTACHMENT, contentType: null }],
      message: null,
    });

    const result = await API_BL_ATTACHMENT_PORT.list('HOUSE', 123);

    expect(result[0].contentType).toBeNull();
  });

  it('contentType 미포함: null-safe transform — null 반환', async () => {
    const { contentType: _, ...withoutContentType } = FIXTURE_ATTACHMENT;
    mockFetchJson.mockResolvedValue({
      data: [withoutContentType],
      message: null,
    });

    const result = await API_BL_ATTACHMENT_PORT.list('HOUSE', 123);

    expect(result[0].contentType).toBeNull();
  });

  it('파싱 실패: 구조 불일치 시 ResponseParseError throw', async () => {
    mockFetchJson.mockResolvedValue({ data: { unexpected: true } });

    await expect(API_BL_ATTACHMENT_PORT.list('HOUSE', 123)).rejects.toThrow(ResponseParseError);
  });
});

describe('API_BL_ATTACHMENT_PORT.upload', () => {
  it('성공: 201 응답에서 id 반환', async () => {
    mockFetchJson.mockResolvedValue({ data: { id: 7 }, message: 'uploaded' });

    const file = new File(['content'], 'a.pdf', { type: 'application/pdf' });
    const id = await API_BL_ATTACHMENT_PORT.upload('HOUSE', 123, file);

    expect(id).toBe(7);
    // FormData body로 호출되었는지 확인 — Content-Type 수동 지정 없음
    const [, init] = mockFetchJson.mock.calls[0];
    expect(init?.method).toBe('POST');
    expect(init?.body).toBeInstanceOf(FormData);
  });

  it('파싱 실패: 응답 구조 불일치 시 ResponseParseError throw', async () => {
    mockFetchJson.mockResolvedValue({ data: null });

    const file = new File(['x'], 'b.txt');
    await expect(API_BL_ATTACHMENT_PORT.upload('HOUSE', 123, file)).rejects.toThrow(ResponseParseError);
  });
});

describe('API_BL_ATTACHMENT_PORT.download', () => {
  it('성공: fetchBlob 결과 그대로 반환', async () => {
    const blob = new Blob(['data'], { type: 'application/pdf' });
    mockFetchBlob.mockResolvedValue(blob);

    const result = await API_BL_ATTACHMENT_PORT.download(1);

    expect(mockFetchBlob).toHaveBeenCalledWith('/api/bl-attachment/1/download');
    expect(result).toBe(blob);
  });
});

describe('API_BL_ATTACHMENT_PORT.remove', () => {
  it('성공: DELETE 호출', async () => {
    mockFetchJson.mockResolvedValue({ data: null, message: 'deleted' });

    await API_BL_ATTACHMENT_PORT.remove(3);

    expect(mockFetchJson).toHaveBeenCalledWith('/api/bl-attachment/3', { method: 'DELETE' });
  });
});
