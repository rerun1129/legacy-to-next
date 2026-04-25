import { ApiError, ResponseParseError } from './errors';

export function toSearchParams(filter: Record<string, unknown>): URLSearchParams {
  return Object.entries(filter)
    .filter(([, v]) => v != null)
    .reduce((p, [k, v]) => { p.set(k, String(v)); return p; }, new URLSearchParams());
}

export async function fetchJson(input: RequestInfo, init?: RequestInit): Promise<unknown> {
  let res: Response;
  try {
    res = await fetch(input, init);
  } catch (e) {
    throw new ApiError('Network error', undefined, e);
  }
  if (!res.ok) throw new ApiError(`HTTP ${res.status}`, res.status);
  if (res.status === 204 || res.headers.get('content-length') === '0') return null;
  try {
    return await res.json();
  } catch (e) {
    throw new ResponseParseError('Failed to parse response JSON', e);
  }
}
