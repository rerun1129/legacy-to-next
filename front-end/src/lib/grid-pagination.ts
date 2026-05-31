/** 모든 그리드 기본 페이지 크기 SSOT. */
export const DEFAULT_PAGE_SIZE = 100;

/** FMS 조회 그리드 페이지 크기 순환 옵션. */
export const PAGE_SIZE_OPTIONS = [100, 1000, 10000] as const;

/**
 * 현재 page size의 다음 순환값을 반환.
 * 마지막(10000)이면 첫 번째(100)로 순환.
 * 현재값이 옵션에 없으면 첫 번째(100) 반환.
 */
export function cyclePageSize(current: number): number {
  const idx = PAGE_SIZE_OPTIONS.indexOf(current as (typeof PAGE_SIZE_OPTIONS)[number]);
  if (idx === -1) return PAGE_SIZE_OPTIONS[0];
  return PAGE_SIZE_OPTIONS[(idx + 1) % PAGE_SIZE_OPTIONS.length];
}
