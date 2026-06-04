// 발행 게이트 — 순수 함수. 토스트·모달 호출 없이 결과만 반환.
// 컴포넌트(freight-panels.tsx)에서 ti·toast·setState로 사후 처리.

import type { FreightRow } from "@/components/fms/house-bl/house-bl-schema";
import type { SelectedFreightLine } from "./freight-issue-types";

// ── 반환 타입 ────────────────────────────────────────────────

export type IssueGateResult =
  | { kind: "error"; messageKey: string }
  | { kind: "open"; lines: SelectedFreightLine[] };

// ── 인자 타입 ────────────────────────────────────────────────

export interface EvaluateIssueSelectionArgs {
  rows: FreightRow[];
  /** fields.map(f => f.id) — 인덱스 정렬 기준 */
  fieldIds: string[];
  issueSelectedKeys: Set<string | number>;
  /** dirtyFields[prefix] */
  prefixDirtyFields: Record<number, object> | undefined;
  freightType: "SELLING" | "BUYING";
}

// ── FreightRow → SelectedFreightLine 변환 헬퍼 ───────────────
// amend(330~351)와 issue(379~400) 두 곳의 중복 스냅샷 빌드를 통합.
// row의 실제 값을 그대로 매핑하므로:
//   - amend 모드: financialDocumentId·financialDocumentNo 실제 값 보존
//   - issue 모드: financialDocumentNo 없는 행이 candidateRows이므로 "" 그대로 — 동작 동일

export function toSelectedFreightLine(
  r: FreightRow,
  freightType: "SELLING" | "BUYING",
): SelectedFreightLine {
  return {
    freightLineId:       r.freightLineId!,
    customerCode:        r.customerCode        ?? "",
    customerName:        r.customerName        ?? "",
    financialDocType:    r.financialDocType     ?? "",
    currency:            r.currency            ?? "",
    settleAmount:        r.settleAmount        ? Number(r.settleAmount)  : null,
    localAmount:         r.localAmount         ? Number(r.localAmount)   : null,
    vat:                 r.vat                 ? Number(r.vat)           : null,
    usdAmount:           r.usdAmount           ? Number(r.usdAmount)     : null,
    performanceDt:       r.performanceDt       ?? "",
    freightCode:         r.freightCode         ?? "",
    freightName:         r.freightName         ?? "",
    exchangeRate:        r.exchangeRate        ? Number(r.exchangeRate)  : null,
    per:                 r.per                 ?? "",
    qty:                 r.qty                 ? Number(r.qty)           : null,
    price:               r.price               ? Number(r.price)         : null,
    taxType:             r.taxType             ?? "",
    financialDocumentId: r.financialDocumentId ?? null,
    financialDocumentNo: r.financialDocumentNo ?? "",
    freightType,
  };
}

// ── 발행 게이트 평가 — 순수 함수 ────────────────────────────
// handleIssueClick(freight-panels.tsx, 라인 232~402)의 검증·분기 로직을 그대로 추출.
// 검증 순서·메시지 키·스냅샷 필드·분기 조건 변경 없음.

export function evaluateIssueSelection(args: EvaluateIssueSelectionArgs): IssueGateResult {
  const { rows, fieldIds, issueSelectedKeys, prefixDirtyFields, freightType } = args;

  // 1. 선택 0개 → 차단
  if (issueSelectedKeys.size === 0) {
    return { kind: "error", messageKey: "selectRequired" };
  }

  // 2. 선택된 행 인덱스 및 row 수집 (발행/미발행 모두 포함)
  const selectedRows: FreightRow[] = [];
  const selectedIndices: number[] = [];
  for (let i = 0; i < fieldIds.length; i++) {
    const fieldId = fieldIds[i];
    if (!issueSelectedKeys.has(fieldId)) continue;
    const row = rows[i];
    if (!row) continue;
    selectedRows.push(row);
    selectedIndices.push(i);
  }

  // 3. dirty 검사 — 선택된 행 중 하나라도 변경(미저장)되면 전체 차단
  const hasDirtySelected = selectedIndices.some((idx) => {
    const rowDirty = prefixDirtyFields?.[idx];
    return rowDirty !== undefined && Object.keys(rowDirty).length > 0;
  });
  if (hasDirtySelected) {
    return { kind: "error", messageKey: "dirtyRowsSaveFirst" };
  }

  // 4. 선택된 행에서 발행 행(financialDocumentId 있는 행)의 서류 ID 수집
  const issuedDocIds = new Set(
    selectedRows
      .map((r) => r.financialDocumentId)
      .filter((id): id is number => id != null),
  );

  // 5. 서로 다른 서류의 발행 행이 섞이면 차단
  if (issuedDocIds.size > 1) {
    return { kind: "error", messageKey: "multiDocBlocked" };
  }

  // ── amend 모드 (발행 행 ≥1, 단일 서류) ──────────────────────
  if (issuedDocIds.size === 1) {
    const targetDocId = [...issuedDocIds][0];

    // 해당 서류의 기준 customer/docType (발행 행 첫 번째 기준)
    const baseIssuedRow = selectedRows.find((r) => r.financialDocumentId === targetDocId)!;
    const baseCustomer = baseIssuedRow.customerCode ?? "";
    const baseDocType  = baseIssuedRow.financialDocType ?? "";

    // 패널 전체 rows에서 같은 서류에 연결된 행 전부 수집 (체크 여부 무관)
    const sameDocRows = rows.filter((r) => r.financialDocumentId === targetDocId);

    // 체크된 미발행 행 (추가 라인)
    const newUnissuedRows = selectedRows.filter((r) => r.financialDocumentId == null);

    // 추가 라인 customer/docType 불일치 선차단
    for (const r of newUnissuedRows) {
      if ((r.customerCode ?? "") !== baseCustomer) {
        return { kind: "error", messageKey: "customerMixed" };
      }
      if ((r.financialDocType ?? "") !== baseDocType) {
        return { kind: "error", messageKey: "docTypeMixed" };
      }
    }

    // dirty 검사: sameDocRows는 체크 안 해도 포함되므로 별도 dirty 체크
    const allPanelDirtyCheck = sameDocRows
      .map((r) => rows.findIndex((ar) => ar === r))
      .some((idx) => {
        if (idx === -1) return false;
        const rowDirty = prefixDirtyFields?.[idx];
        return rowDirty !== undefined && Object.keys(rowDirty).length > 0;
      });
    if (allPanelDirtyCheck) {
      return { kind: "error", messageKey: "dirtyRowsSaveFirst" };
    }

    // 최종 라인 셋 — sameDocRows + 체크된 미발행 행 합집합 (freightLineId 기준 중복 제거)
    const seenIds = new Set<number>();
    const finalRows: FreightRow[] = [];
    for (const r of [...sameDocRows, ...newUnissuedRows]) {
      const lid = r.freightLineId;
      if (lid == null || seenIds.has(lid)) continue;
      seenIds.add(lid);
      finalRows.push(r);
    }

    const snapshot = finalRows.map((r) => toSelectedFreightLine(r, freightType));
    return { kind: "open", lines: snapshot };
  }

  // ── issue 모드 (발행 행 0개) — 기존 로직 유지 ────────────────
  const candidateRows = selectedRows.filter((r) => !r.financialDocumentNo);

  if (candidateRows.length === 0) {
    return { kind: "error", messageKey: "selectRequired" };
  }

  // customerCode distinct 검증
  const customerCodes = new Set(candidateRows.map((r) => r.customerCode ?? ""));
  if (customerCodes.size > 1) {
    return { kind: "error", messageKey: "customerMixed" };
  }

  // financialDocType distinct 검증
  const docTypes = new Set(candidateRows.map((r) => r.financialDocType ?? ""));
  if (docTypes.size > 1) {
    return { kind: "error", messageKey: "docTypeMixed" };
  }

  const snapshot = candidateRows.map((r) => toSelectedFreightLine(r, freightType));
  return { kind: "open", lines: snapshot };
}
