/**
 * YYYYMMDD → YYYY-MM-DD. 8자리가 아니면 원문 그대로.
 */
export function fmtDate(v: unknown): string {
  const s = String(v ?? '');
  return s.length === 8 ? `${s.slice(0, 4)}-${s.slice(4, 6)}-${s.slice(6, 8)}` : s;
}

/**
 * 코드값을 options 배열에서 검색해 레이블로 변환. 미매칭 시 원래 코드 반환.
 */
export function fmtEnum(v: unknown, options: ReadonlyArray<{ value: string; label: string }>): string {
  const code = String(v ?? '');
  return options.find(o => o.value === code)?.label ?? code;
}

/**
 * 숫자/문자열을 소수점 자리수 지정 형식으로 변환. 빈값은 '' 반환.
 */
export function fmtNumber(v: unknown, decimals = 0): string {
  const n = Number(v);
  if (v == null || v === '' || isNaN(n)) return '';
  return n.toFixed(decimals);
}

/**
 * 중량 값을 소수점 3자리 형식으로 변환. 빈값은 '' 반환.
 */
export function fmtWeight(v: unknown): string {
  return fmtNumber(v, 3);
}
