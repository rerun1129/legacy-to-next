import { ApiError, NotFoundError, ResponseParseError } from './errors';
import { getAuthHeader } from '@/lib/admin-session';

// 공통 에러 처리 — !res.ok 응답을 적절한 에러 인스턴스로 변환 (호출측이 throw)
async function buildResponseError(res: Response, inputHint: string): Promise<ApiError> {
  const isProblem = res.headers.get('content-type')?.includes('application/problem+json');
  if (isProblem) {
    const pd = await res.json().catch(() => null) as {
      type?: string;
      title?: string;
      detail?: string;
      status?: number;
    } | null;
    if (res.status === 404) return new NotFoundError('resource', inputHint);
    return new ApiError(pd?.detail ?? pd?.title ?? `HTTP ${res.status}`, res.status, pd);
  }
  if (res.status === 404) return new NotFoundError('resource', inputHint);
  return new ApiError(`HTTP ${res.status}`, res.status);
}

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? '';

function resolveUrl(input: RequestInfo): RequestInfo {
  if (typeof input !== 'string') return input;
  if (/^https?:\/\//i.test(input)) return input;
  if (input.startsWith('/api/')) return `${API_BASE}${input}`;
  return input;
}

export function toSearchParams(filter: Record<string, unknown>): URLSearchParams {
  // null, undefined, 빈 문자열은 query string에서 제외
  return Object.entries(filter)
    .filter(([, v]) => v != null && v !== '')
    .reduce((p, [k, v]) => { p.set(k, String(v)); return p; }, new URLSearchParams());
}

export async function fetchJson(input: RequestInfo, init?: RequestInit): Promise<unknown> {
  const url = resolveUrl(input);
  const headers = new Headers(init?.headers);
  // FormData는 브라우저가 boundary를 포함한 Content-Type을 자동 세팅하므로 수동 지정 금지
  if (init?.body && !(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  for (const [k, v] of Object.entries(getAuthHeader())) headers.set(k, v);
  let res: Response;
  try {
    res = await fetch(url, { ...init, headers });
  } catch (e) {
    throw new ApiError('Network error', undefined, e);
  }

  if (!res.ok) throw await buildResponseError(res, String(input));
  if (res.status === 204 || res.headers.get('content-length') === '0') return null;
  try {
    return await res.json();
  } catch (e) {
    throw new ResponseParseError('Failed to parse response JSON', e);
  }
}

// Authorization 헤더 필요 다운로드용 — 성공 시 Blob 반환
export async function fetchBlob(input: RequestInfo, init?: RequestInit): Promise<Blob> {
  const url = resolveUrl(input);
  const headers = new Headers(init?.headers);
  for (const [k, v] of Object.entries(getAuthHeader())) headers.set(k, v);
  let res: Response;
  try {
    res = await fetch(url, { ...init, headers });
  } catch (e) {
    throw new ApiError('Network error', undefined, e);
  }
  if (!res.ok) throw await buildResponseError(res, String(input));
  return res.blob();
}
