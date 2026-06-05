// 그룹화 확정 게이트 — 순수 함수. 토스트·모달 호출 없이 결과만 반환.
// 컴포넌트(financial-document-group-modal.tsx)에서 toast·mutate로 사후 처리.

import type { FinancialDocumentSearchRow } from "@/application/bms/financial-document/ports";

export type GroupGateResult =
  | { kind: "error"; messageKey: string }
  | { kind: "ok"; ids: number[] };

/**
 * 그룹화 확정 전 우측 그리드(최종 그룹 대상) 검증.
 * 빈 우측(전원 해제)은 허용. 거래처 혼용만 차단.
 */
export function evaluateGroupConfirm(rightRows: FinancialDocumentSearchRow[]): GroupGateResult {
  if (rightRows.length === 0) {
    // 전원 해제 = 그룹 해제 요청 — 허용
    return { kind: "ok", ids: [] };
  }

  const customerCodes = new Set(rightRows.map((r) => r.customerCode));
  if (customerCodes.size > 1) {
    return { kind: "error", messageKey: "customerMixed" };
  }

  return {
    kind: "ok",
    ids: rightRows.map((r) => r.financialDocumentId),
  };
}
