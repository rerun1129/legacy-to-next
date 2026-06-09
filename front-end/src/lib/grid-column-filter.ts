/**
 * 순수·결정적 클라이언트 컬럼 필터 엔진.
 * React·도메인 무의존. 동일 입력 → 동일 출력.
 * 빈 문자열을 sentinel로 맨 끝에 배치하는 bucketComparator 방식 사용(grid-aggregate.ts와 동일).
 */

/** columnKey → 선택 표시값 집합. 키 부재 = 해당 컬럼 필터 없음(모두 통과). */
export type ColumnFilterState = Record<string, ReadonlySet<string>>;

/** 빈 버킷("")은 맨 끝 sentinel, 그 외 오름차순. grid-aggregate의 bucketComparator와 동일. */
function distinctComparator(a: string, b: string): number {
  if (a === "" && b !== "") return 1;
  if (a !== "" && b === "") return -1;
  return a < b ? -1 : a > b ? 1 : 0;
}

/**
 * rows에서 accessor가 반환하는 표시값의 distinct 배열을 결정적 순서로 반환.
 * 빈 문자열은 맨 끝. 나머지는 오름차순.
 */
export function distinctDisplayValues<T>(
  rows: readonly T[],
  accessor: (r: T) => string,
): string[] {
  const set = new Set<string>();
  for (const row of rows) {
    set.add(accessor(row));
  }
  return [...set].sort(distinctComparator);
}

/**
 * ColumnFilterState를 기준으로 rows를 필터링한다(AND 조합).
 * - state에 키가 없는 컬럼은 해당 컬럼 필터 없음 = 모두 통과.
 * - 선택 Set이 비어있는 컬럼이 있으면 그 컬럼에서 아무 행도 통과하지 못함(빈 Set = 전체 차단).
 * - accessors에 정의되지 않은 state 키는 무시.
 */
export function applyColumnFilters<T>(
  rows: readonly T[],
  accessors: Record<string, (r: T) => string>,
  state: ColumnFilterState,
): T[] {
  // 활성 필터(키가 state에 있는 것)만 추출
  const activeKeys = Object.keys(state).filter((k) => k in accessors);
  if (activeKeys.length === 0) return rows as T[];

  return (rows as T[]).filter((row) => {
    for (const key of activeKeys) {
      const selected = state[key];
      if (!selected.has(accessors[key](row))) return false;
    }
    return true;
  });
}
