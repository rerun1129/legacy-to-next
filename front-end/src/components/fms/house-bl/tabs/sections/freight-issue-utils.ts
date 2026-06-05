import { type useQueryClient } from "@tanstack/react-query";

// ── 오늘 날짜 yyyyMMdd ─────────────────────────────────────────
export function todayYyyyMmDd(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}${m}${day}`;
}

// ── DocumentType FE 매핑 (BE 컴포넌트 의존 금지) ──────────────
export const DOCUMENT_TYPE_MAP: Record<string, string> = {
  INVOICE: "Invoice",
  PAYMENT: "Payment",
  DEBIT:   "Debit",
  CREDIT:  "Credit",
};

export function resolveDocType(code: string): string {
  return DOCUMENT_TYPE_MAP[code] ?? code;
}

// ── B/L detail 쿼리 invalidate 헬퍼 ───────────────────────────
export function invalidateBlDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  domainKey: string,
  blId: number,
) {
  queryClient.invalidateQueries({ queryKey: [domainKey, "detail", blId] });
}
