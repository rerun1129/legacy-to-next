"use client";

/**
 * 서류 발행/편집(amend) 모달.
 * - issue 모드: 미발행 운임 선택 → 새 financial_document INSERT + 라인 link.
 * - amend 모드: 발행된 서류의 라인 편집 → finalLineIds 선언적 PATCH, 모든 라인 제거 시 서류 자동 삭제.
 * - editableHeader(amend+CREATED): listByBl 캐시 self-resolve → 진입점 무관 동일 동작.
 * - customerCode / financialDocType 검증은 발행 버튼 onClick(freight-panels)에서 선행.
 * - 에러 토스트는 전역 MutationCache onError SSOT에 위임(직접 catch 금지).
 */

import { useState, useMemo } from "react";
import type { FinancialDocument } from "@/application/bms/financial-document/ports";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Minus } from "lucide-react";
import { ModalShell } from "@/components/shared/modal-shell";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { confirm } from "@/components/confirm";
import {
  financialDocumentKeys,
  financialDocumentUseCases,
} from "@/application/bms/financial-document/use-cases";
import { useEnumOptions } from "@/application/enums/use-enum";
import { type FreightIssueModalProps, type SelectedFreightLine } from "./freight-issue-types";
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

  // 모달 내 편집 가능한 라인 목록 — 행 제거 시 실시간 반영
  // ModalShell isOpen=false → return null(unmount)이므로 재오픈마다 초기값으로 재설정됨
  const [lines, setLines] = useState<SelectedFreightLine[]>(selectedLines);

  // amend 모드 판정 — 진입 시점 selectedLines 기준 1회 고정.
  // lines(가변) 기반으로 두면 행을 전부 제거했을 때 isAmend=false로 뒤집혀
  // amendMutation 대신 issueMutation이 호출되고 BE validateLines가 빈 lineIds를 거부하는 버그 발생.
  // ModalShell이 isOpen=false 시 unmount하므로 재오픈마다 selectedLines로 재초기화됨.
  const [amendDocId] = useState<number | null>(
    () => selectedLines.find((l) => l.financialDocumentId != null)?.financialDocumentId ?? null,
  );
  const isAmend = amendDocId != null;

  // amend 시 서류 번호 표시 — 행을 전부 지워도 헤더 표시 유지를 위해 진입 시점 고정
  const [displayDocumentNo] = useState(
    () => selectedLines.find((l) => l.financialDocumentNo)?.financialDocumentNo ?? "",
  );

  // amend 시 listByBl 캐시에서 해당 서류를 찾아 status·헤더값 self-resolve
  // → 진입점(운임그리드/Account Documents) 무관하게 동일 동작.
  const amendDoc = isAmend
    ? queryClient
        .getQueryData<FinancialDocument[]>(financialDocumentKeys.listByBl(blType, blId))
        ?.find((d) => d.financialDocumentId === amendDocId)
    : undefined;
  const editableHeader = isAmend && amendDoc?.status === "CREATED";
  const resolvedInitialHeader = amendDoc
    ? {
        documentDt: amendDoc.documentDt,
        performanceDt: amendDoc.performanceDt,
        teamCode: amendDoc.teamCode ?? "",
        operator: amendDoc.operator ?? "",
      }
    : undefined;

  const header = useFreightIssueHeader(editableHeader ? resolvedInitialHeader : undefined);

  const { options: taxTypeOptions } = useEnumOptions("TaxType");
  const taxTypeLabelMap = useMemo(
    () => new Map(taxTypeOptions.map((o) => [o.value, o.label])),
    [taxTypeOptions],
  );

  // 행 제거 버튼 컬럼 — amend/issue 모두 허용(최종셋에서 빠지면 BE diff가 unlink 처리)
  // setLines는 React setState 안정 레퍼런스. ti(removeLine) 키 변경 시에만 재생성.
  const removeColumn = useMemo<GridColumn<SelectedFreightLine>>(
    () => ({
      key: "_remove",
      label: "",
      width: 36,
      align: "center",
      render: (_, row) => (
        <button
          type="button"
          style={{
            background: "none",
            border: "none",
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: 2,
            color: "var(--color-danger, #ef4444)",
          }}
          title={ti("removeLine")}
          onClick={() =>
            setLines((prev) => prev.filter((l) => l.freightLineId !== row.freightLineId))
          }
        >
          <Minus size={14} />
        </button>
      ),
    }),
    [ti, setLines],
  );

  const baseLineColumns = useMemo(
    () => buildFreightLineColumns(tf, taxTypeLabelMap),
    [tf, taxTypeLabelMap],
  );

  // 행 제거 컬럼을 앞에 배치
  const lineColumns = useMemo(
    () => [removeColumn, ...baseLineColumns],
    [removeColumn, baseLineColumns],
  );

  // ── 발행 Mutation (issue 모드) ─────────────────────────────
  const issueMutation = useMutation({
    mutationFn: () =>
      financialDocumentUseCases.issueDocument({
        blType,
        blId,
        freightType,
        lineIds: lines.map((l) => l.freightLineId),
        documentDt: header.documentDt,
        performanceDt: header.performanceDt,
        teamCode: header.teamCode || null,
        operator: header.operatorForSubmit,
      }),
    onSuccess: (result) => {
      toast.success(`${ti("success")} (${result.documentNo})`);
      queryClient.invalidateQueries({
        queryKey: financialDocumentKeys.listByBl(blType, blId),
      });
      onIssueSuccess();
      // 모달은 닫지 않음(PRD: 잔류)
    },
    // 에러는 전역 MutationCache onError SSOT에 위임
  });

  // ── 서류 삭제 Mutation (amend 모드) ────────────────────────
  const deleteMutation = useMutation({
    mutationFn: () => financialDocumentUseCases.deleteDocument(amendDocId!),
    onSuccess: () => {
      toast.success(ti("deleteSuccess"));
      queryClient.invalidateQueries({
        queryKey: financialDocumentKeys.listByBl(blType, blId),
      });
      onIssueSuccess();
      handleClose();
    },
    // 에러는 전역 MutationCache onError SSOT에 위임
  });

  // ── 편집 Mutation (amend 모드) ─────────────────────────────
  const amendMutation = useMutation({
    mutationFn: () =>
      financialDocumentUseCases.amendDocument({
        documentId: amendDocId!,
        blType,
        blId,
        freightType,
        finalLineIds: lines.map((l) => l.freightLineId),
        // editableHeader=true(CREATED)일 때만 헤더 4필드 전달, 비CREATED는 null로 BE 무시
        documentDt: editableHeader ? header.documentDt : null,
        performanceDt: editableHeader ? header.performanceDt : null,
        teamCode: editableHeader ? (header.teamCode || null) : null,
        operator: editableHeader ? header.operatorForSubmit : null,
      }),
    onSuccess: (result) => {
      if (result.deleted) {
        toast.success(ti("deleteSuccess"));
      } else {
        toast.success(ti("amendSuccess"));
      }
      queryClient.invalidateQueries({
        queryKey: financialDocumentKeys.listByBl(blType, blId),
      });
      // detail 재조회(form.reset 경로) + 그리드 선택 해제
      onIssueSuccess();
      handleClose();
    },
    // 에러는 전역 MutationCache onError SSOT에 위임
  });

  // 모달 닫기 — listByBl invalidate 재보장
  function handleClose() {
    queryClient.invalidateQueries({
      queryKey: financialDocumentKeys.listByBl(blType, blId),
    });
    onClose();
  }

  // 서류 삭제 핸들러 — amend 모드 전용
  async function handleDeleteDocument() {
    const ok = await confirm({
      title: ti("deleteConfirmTitle"),
      description: ti("deleteConfirmDesc", { count: 1 }),
      variant: "destructive",
      confirmText: ti("deleteConfirmBtn"),
      cancelText: ti("cancel"),
    });
    if (!ok) return;
    deleteMutation.mutate();
  }

  // 발행 버튼: 발행/편집 성공 후 재실행 방지
  const canIssue = !issueMutation.isPending && !issueMutation.isSuccess;
  const canAmend = !amendMutation.isPending && !amendMutation.isSuccess;

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
      {/* ── 헤더 섹션 ─────────────────────────────────────────── */}
      {isAmend && !editableHeader ? (
        // amend 모드, 비CREATED: 헤더 표시 전용 (서류번호만 표시)
        <div style={{ display: "grid", gridTemplateColumns: "repeat(5, minmax(0,1fr))", gap: 8, marginBottom: 12 }}>
          <div className="field">
            <div className="field__label">{ti("documentNo")}</div>
            <div className="field__input">
              <input className="input" readOnly value={displayDocumentNo} />
            </div>
          </div>
        </div>
      ) : isAmend && editableHeader ? (
        // amend 모드, CREATED: 헤더 4필드 편집 가능 + 실서류번호 표시
        <FreightIssueHeader header={header} ti={ti} documentNo={displayDocumentNo} />
      ) : (
        // issue 모드: 기존 헤더 (documentNo 빈칸 placeholder)
        <FreightIssueHeader header={header} ti={ti} />
      )}

      {/* ── 선택 요약(고객/DocType/합계) ─────────────────────── */}
      <FreightIssueSummary selectedLines={lines} ti={ti} tf={tf} />

      {/* ── 선택 라인 목록 (행 제거 버튼 포함) ─────────────────── */}
      <div style={{ marginBottom: 4, fontSize: 12, color: "var(--color-text-secondary, #6b7280)" }}>
        {isAmend ? ti("amendDesc", { defaultValue: ti("desc") }) : ti("desc")} ({lines.length}건)
      </div>
      <div style={{ flex: 1, minHeight: 270 }}>
        <GridList
          columns={lineColumns}
          data={lines}
          rowKey={(r) => r.freightLineId}
          emptyMessage="—"
        />
      </div>

      {/* ── 액션 버튼 ────────────────────────────────────────── */}
      <div className="modal__actions">
        {isAmend && (
          <Button
            variant="danger"
            style={{ marginRight: "auto" }}
            onClick={handleDeleteDocument}
            disabled={deleteMutation.isPending}
            loading={deleteMutation.isPending}
          >
            {ti("deleteDocBtn")}
          </Button>
        )}
        {isAmend ? (
          <Button
            variant="transaction"
            onClick={() => amendMutation.mutate()}
            disabled={!canAmend}
            loading={amendMutation.isPending}
          >
            {ti("confirm")}
          </Button>
        ) : (
          <Button
            variant="transaction"
            onClick={() => issueMutation.mutate()}
            disabled={!canIssue || lines.length === 0}
            loading={issueMutation.isPending}
          >
            {ti("confirm")}
          </Button>
        )}
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
