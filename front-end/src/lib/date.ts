/**
 * YYYYMMDD(8자리 숫자 문자열)를 YYYY-MM-DD 표시 형식으로 변환.
 * 이미 하이픈 포함이거나 빈 값이면 그대로 반환.
 */
export function formatDateDisplay(raw: string | undefined | null): string {
  if (!raw) return '';
  const d = raw.replace(/\D/g, '');
  if (d.length !== 8) return raw;
  return `${d.slice(0, 4)}-${d.slice(4, 6)}-${d.slice(6, 8)}`;
}

/**
 * YYYY-MM-DD 또는 YYYYMMDD를 백엔드 저장 형식(YYYYMMDD)으로 변환.
 */
export function formatDateStorage(display: string | undefined | null): string {
  if (!display) return '';
  return display.replace(/\D/g, '').slice(0, 8);
}
