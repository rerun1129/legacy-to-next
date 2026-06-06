// 발급 확정 게이트 — 순수 함수. 토스트·모달 호출 없이 결과만 반환.
// 컴포넌트(freight-line-issue-list-client)에서 toast·mutate로 사후 처리.

import type { IssueType } from "./freight-line-issue-list-config";
import type { FreightLineIssueRow } from "@/application/bms/freight-line-issue/ports";

export type IssueGateResult =
  | { kind: "error"; messageKey: string }
  | { kind: "ok"; lineIds: number[] };

/**
 * 발급 버튼 클릭 시 선택 행 검증.
 * - 선택 0개: 차단
 * - 단일 고객 코드만 허용 (혼재 차단)
 * - 단일 financialDocType만 허용 (혼재 차단)
 * - 이미 발급된 행 차단 (issueType 기준)
 */
export function evaluateIssueGate(
  rows: FreightLineIssueRow[],
  issueType: IssueType,
): IssueGateResult {
  if (rows.length === 0) {
    return { kind: "error", messageKey: "selectRequired" };
  }

  const customerCodes = new Set(rows.map((r) => r.customerCode));
  if (customerCodes.size > 1) {
    return { kind: "error", messageKey: "customerMixed" };
  }

  const docTypes = new Set(rows.map((r) => r.financialDocType));
  if (docTypes.size > 1) {
    return { kind: "error", messageKey: "docTypeMixed" };
  }

  // 이미 발급된 행 포함 여부 확인 (BE도 거부하지만 FE에서 선제 차단)
  const alreadyIssued = issueType === "TAX"
    ? rows.some((r) => r.taxNo !== null)
    : rows.some((r) => r.slipNo !== null);

  if (alreadyIssued) {
    return { kind: "error", messageKey: "alreadyIssued" };
  }

  return { kind: "ok", lineIds: rows.map((r) => r.freightLineId) };
}
