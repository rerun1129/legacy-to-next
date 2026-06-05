"use client";

/**
 * 금융서류 그룹화 모달.
 * - 좌측: 미그룹(rightKeys에 없는 행) / 우측: 그룹 대상(rightKeys에 있는 행)
 * - 부여→: selectedLeft를 rightKeys에 add / ←해제: selectedRight를 rightKeys에서 delete
 * - 확정: evaluateGroupConfirm → groupDocuments mutate → invalidate + onGroupSuccess
 * - 모달 루트는 <div>(중첩 form 금지), Enter 차단 onKeyDown
 */

import { useState, useMemo } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { ModalShell } from "@/components/shared/modal-shell";
import { GridList } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { financialDocumentKeys, financialDocumentUseCases } from "@/application/bms/financial-document/use-cases";
import type { FinancialDocumentSearchRow } from "@/application/bms/financial-document/ports";
import { buildGroupModalColumns } from "./financial-document-group-columns";
import { evaluateGroupConfirm } from "./financial-document-group-gate";
import type { FinancialDocumentGroupModalProps } from "./financial-document-group-types";

// ── Modal Inner ──────────────────────────────────────────────

function FinancialDocumentGroupModalInner({
  onClose,
  rows,
  searchFilter,
  page,
  pageSize,
  onGroupSuccess,
}: Omit<FinancialDocumentGroupModalProps, "isOpen">) {
  const tg = useTranslations("bms.list.group");
  const tCols = useTranslations("bms.list.masterCols");
  const queryClient = useQueryClient();

  // 우측 key Set이 단일 SoT — 초기값: 이미 그룹에 속한 행(groupFinancialNo가 있는 행)
  const [rightKeys, setRightKeys] = useState<Set<number>>(
    () => new Set(rows.filter((r) => r.groupFinancialNo != null).map((r) => r.financialDocumentId)),
  );
  const [selectedLeft, setSelectedLeft] = useState<Set<number>>(new Set());
  const [selectedRight, setSelectedRight] = useState<Set<number>>(new Set());

  // 파생 행 목록 — rightKeys 변경 시에만 재계산
  const leftRows = useMemo(() => rows.filter((r) => !rightKeys.has(r.financialDocumentId)), [rows, rightKeys]);
  const rightRows = useMemo(() => rows.filter((r) => rightKeys.has(r.financialDocumentId)), [rows, rightKeys]);

  // 컬럼 — 좌우 공용
  const columns = useMemo(() => buildGroupModalColumns((key) => tCols(key)), [tCols]);

  // 부여→ 버튼
  function handleAssign() {
    setRightKeys((prev) => {
      const next = new Set(prev);
      selectedLeft.forEach((id) => next.add(id));
      return next;
    });
    setSelectedLeft(new Set());
  }

  // ←해제 버튼
  function handleRelease() {
    setRightKeys((prev) => {
      const next = new Set(prev);
      selectedRight.forEach((id) => next.delete(id));
      return next;
    });
    setSelectedRight(new Set());
  }

  const groupMutation = useMutation({
    mutationFn: (req: { groupedDocumentIds: number[]; scopeDocumentIds: number[] }) =>
      financialDocumentUseCases.groupDocuments(req),
    onSuccess: (result) => {
      const suffix = result.groupFinancialNo ? ` (${result.groupFinancialNo})` : "";
      toast.success(`${tg("success")}${suffix}`);
      if (searchFilter !== null) {
        queryClient.invalidateQueries({
          queryKey: financialDocumentKeys.search(searchFilter, page - 1, pageSize),
        });
      }
      onGroupSuccess();
      onClose();
    },
    // 에러 토스트는 전역 MutationCache onError SSOT에 위임
  });

  function handleConfirm() {
    const gate = evaluateGroupConfirm(rightRows);
    if (gate.kind === "error") {
      toast.error(tg(gate.messageKey));
      return;
    }
    groupMutation.mutate({
      groupedDocumentIds: [...rightKeys],
      scopeDocumentIds: rows.map((r) => r.financialDocumentId),
    });
  }

  return (
    <div
      className="modal__body"
      style={{ padding: "12px 20px 16px" }}
      onKeyDown={(e) => {
        // Enter 차단 (textarea 제외)
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
    >
      {/* 좌우 2그리드 transfer 레이아웃 */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr auto 1fr",
          gridTemplateRows: "1fr",
          gap: 12,
          marginBottom: 12,
          minHeight: 460,
        }}
      >
        {/* 좌측: 미그룹 */}
        <div style={{ display: "flex", flexDirection: "column", minHeight: 0, minWidth: 0 }}>
          <div style={{ fontSize: 12, fontWeight: 600, marginBottom: 4, color: "var(--color-text-secondary, #6b7280)" }}>
            {tg("leftTitle")} ({leftRows.length})
          </div>
          <div className="list-wrap">
            <GridList<FinancialDocumentSearchRow>
              columns={columns}
              data={leftRows}
              rowKey={(r) => r.financialDocumentId}
              emptyMessage="—"
              selectable
              selectedKeys={selectedLeft}
              onSelectionChange={(next) => setSelectedLeft(next as Set<number>)}
            />
          </div>
        </div>

        {/* 중앙 버튼 */}
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            gap: 8,
            alignSelf: "center",
          }}
        >
          <Button
            type="button"
            variant="transaction"
            size="sm"
            disabled={selectedLeft.size === 0}
            onClick={handleAssign}
          >
            {tg("assign")}
          </Button>
          <Button
            type="button"
            variant="normal"
            size="sm"
            disabled={selectedRight.size === 0}
            onClick={handleRelease}
          >
            {tg("release")}
          </Button>
        </div>

        {/* 우측: 그룹 대상 */}
        <div style={{ display: "flex", flexDirection: "column", minHeight: 0, minWidth: 0 }}>
          <div style={{ fontSize: 12, fontWeight: 600, marginBottom: 4, color: "var(--color-text-secondary, #6b7280)" }}>
            {tg("rightTitle")} ({rightRows.length})
          </div>
          <div className="list-wrap">
            <GridList<FinancialDocumentSearchRow>
              columns={columns}
              data={rightRows}
              rowKey={(r) => r.financialDocumentId}
              emptyMessage="—"
              selectable
              selectedKeys={selectedRight}
              onSelectionChange={(next) => setSelectedRight(next as Set<number>)}
            />
          </div>
        </div>
      </div>

      {/* 액션 버튼 */}
      <div className="modal__actions">
        <Button
          type="button"
          variant="transaction"
          onClick={handleConfirm}
          disabled={groupMutation.isPending}
          loading={groupMutation.isPending}
        >
          {tg("confirm")}
        </Button>
        <Button type="button" variant="normal" onClick={onClose}>
          {tg("cancel")}
        </Button>
      </div>
    </div>
  );
}

// ── 공개 컴포넌트 ────────────────────────────────────────────

export function FinancialDocumentGroupModal({
  isOpen,
  onClose,
  rows,
  searchFilter,
  page,
  pageSize,
  onGroupSuccess,
}: FinancialDocumentGroupModalProps) {
  const tg = useTranslations("bms.list.group");

  return (
    <ModalShell isOpen={isOpen} title={tg("title")} size="lg" portal>
      <FinancialDocumentGroupModalInner
        onClose={onClose}
        rows={rows}
        searchFilter={searchFilter}
        page={page}
        pageSize={pageSize}
        onGroupSuccess={onGroupSuccess}
      />
    </ModalShell>
  );
}
