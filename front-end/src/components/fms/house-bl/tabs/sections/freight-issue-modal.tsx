"use client";

/**
 * 서류 발행 모달.
 * - 발행 대상 라인은 상위 그리드에서 선택 후 스냅샷으로 전달받음(selectedLines).
 * - customerCode / financialDocType 검증은 발행 버튼 onClick(freight-panels)에서 선행.
 * - 성공 시 모달 잔류(PRD §3). onClose 시 listByBl invalidate 보장.
 * - 에러 토스트는 전역 MutationCache onError SSOT에 위임(직접 catch 금지).
 */

import { useMemo } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { ModalShell } from "@/components/shared/modal-shell";
import { GridList } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import {
  financialDocumentKeys,
  financialDocumentUseCases,
} from "@/application/bms/financial-document/use-cases";
import { useEnumOptions } from "@/application/enums/use-enum";
import { type FreightIssueModalProps } from "./freight-issue-types";
import { buildFreightLineColumns } from "./freight-issue-columns";
import { useFreightIssueHeader, FreightIssueHeader } from "./freight-issue-header";
import { FreightIssueSummary } from "./freight-issue-summary";

// 타입은 외부 사용자를 위해 re-export
export type { SelectedFreightLine, FreightIssueModalProps } from "./freight-issue-types";

// ── Modal Inner ────────────────────────────────────────────────

function FreightIssueModalInner({
  onClose,
  blType,
  blId,
  freightType,
  selectedLines,
  onIssueSuccess,
}: Omit<FreightIssueModalProps, "isOpen">) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const ti = useTranslations("fms.houseBl.entry.freight.issue");
  const queryClient = useQueryClient();

  const header = useFreightIssueHeader();

  const { options: taxTypeOptions } = useEnumOptions("TaxType");
  // value→label 역방향 조회 맵 (코드→표시명)
  const taxTypeLabelMap = useMemo(
    () => new Map(taxTypeOptions.map((o) => [o.value, o.label])),
    [taxTypeOptions],
  );

  const lineColumns = useMemo(
    () => buildFreightLineColumns(tf, taxTypeLabelMap),
    [tf, taxTypeLabelMap],
  );

  const blIdStr = String(blId);

  // ── 발행 Mutation ──────────────────────────────────────────
  const issueMutation = useMutation({
    mutationFn: () =>
      financialDocumentUseCases.issueDocument({
        blType,
        blId: blIdStr,
        freightType,
        lineIds: selectedLines.map((l) => l.freightLineId),
        documentDt: header.documentDt,
        performanceDt: header.performanceDt,
        teamCode: header.teamCode || null,
        operator: header.operatorForSubmit,
      }),
    onSuccess: (result) => {
      // 성공 토스트 명시(전역 onError는 에러만) — 발행된 document_no 포함
      toast.success(`${ti("success")} (${result.documentNo})`);
      // ① listByBl invalidate (Account Documents 갱신)
      queryClient.invalidateQueries({
        queryKey: financialDocumentKeys.listByBl(blType, blIdStr),
      });
      // ② 상위 그리드 체크박스 선택 해제 + entry detail 재조회 트리거(onIssueSuccess 경로로 전파)
      // B/L detail invalidate는 entry의 onFreightMutated → handleIssueSuccess → 여기로 이관.
      // useFieldArray fields는 setValue로 갱신 불가하므로 detailLoadedRef 풀기+invalidate 패턴 필수.
      onIssueSuccess();
      // 모달은 닫지 않음(PRD: 잔류). 발행 완료 후 버튼 비활성(isPending/isSuccess 판정).
    },
    // 에러는 전역 MutationCache onError SSOT에 위임 — 여기서 catch 금지
  });

  // 모달 닫기 — listByBl invalidate 재보장(Account Documents 최신)
  function handleClose() {
    queryClient.invalidateQueries({
      queryKey: financialDocumentKeys.listByBl(blType, blIdStr),
    });
    onClose();
  }

  // 발행 버튼: 발행 성공 후 재발행 방지(isSuccess 시 비활성)
  const canIssue = !issueMutation.isPending && !issueMutation.isSuccess;

  return (
    <div
      className="modal__body"
      style={{ padding: "12px 20px 16px" }}
      onKeyDown={(e) => {
        // Enter 차단 (textarea 제외) — 부모 엔트리 폼으로 버블링돼 저장 제출되는 것을 막음
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
    >
      {/* ── 헤더 입력 섹션 ─────────────────────────────────── */}
      <FreightIssueHeader header={header} ti={ti} />

      {/* ── 선택 요약(고객/DocType/합계) ─────────────────────── */}
      <FreightIssueSummary selectedLines={selectedLines} ti={ti} tf={tf} />

      {/* ── 선택 라인 읽기전용 목록 ──────────────────────────── */}
      <div style={{ marginBottom: 4, fontSize: 12, color: "var(--color-text-secondary, #6b7280)" }}>
        {ti("desc")} ({selectedLines.length}건)
      </div>
      <div style={{ flex: 1, minHeight: 270 }}>
        <GridList
          columns={lineColumns}
          data={selectedLines}
          rowKey={(r) => r.freightLineId}
          emptyMessage="—"
        />
      </div>

      {/* ── 액션 버튼 ────────────────────────────────────────── */}
      <div className="modal__actions">
        <Button
          variant="transaction"
          onClick={() => issueMutation.mutate()}
          disabled={!canIssue}
          loading={issueMutation.isPending}
        >
          {ti("confirm")}
        </Button>
        <Button variant="normal" onClick={handleClose}>
          {ti("cancel")}
        </Button>
      </div>
    </div>
  );
}

// ── 공개 컴포넌트 ─────────────────────────────────────────────

export function FreightIssueModal({
  isOpen,
  onClose,
  blType,
  blId,
  freightType,
  selectedLines,
  onIssueSuccess,
}: FreightIssueModalProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const panelLabel =
    freightType === "SELLING" ? tf("panels.sellingDebit") : tf("panels.buyingCredit");

  return (
    <ModalShell
      isOpen={isOpen}
      title={panelLabel}
      size="lg"
      portal
      style={{ maxWidth: 1400 }}
    >
      <FreightIssueModalInner
        onClose={onClose}
        blType={blType}
        blId={blId}
        freightType={freightType}
        selectedLines={selectedLines}
        onIssueSuccess={onIssueSuccess}
      />
    </ModalShell>
  );
}
