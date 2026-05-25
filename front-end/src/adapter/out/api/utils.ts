import { ApiError, NotFoundError, ResponseParseError } from './errors';
import { getAuthHeader } from '@/lib/admin-session';

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
  if (init?.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  for (const [k, v] of Object.entries(getAuthHeader())) headers.set(k, v);
  let res: Response;
  try {
    res = await fetch(url, { ...init, headers });
  } catch (e) {
    throw new ApiError('Network error', undefined, e);
  }

  const isProblem = res.headers.get('content-type')?.includes('application/problem+json');
  if (!res.ok) {
    if (isProblem) {
      const pd = await res.json().catch(() => null) as {
        type?: string;
        title?: string;
        detail?: string;
        status?: number;
      } | null;
      if (res.status === 404) throw new NotFoundError('resource', String(input));
      throw new ApiError(pd?.detail ?? pd?.title ?? `HTTP ${res.status}`, res.status, pd);
    }
    if (res.status === 404) throw new NotFoundError('resource', String(input));
    throw new ApiError(`HTTP ${res.status}`, res.status);
  }
  if (res.status === 204 || res.headers.get('content-length') === '0') return null;
  try {
    return await res.json();
  } catch (e) {
    throw new ResponseParseError('Failed to parse response JSON', e);
  }
}
