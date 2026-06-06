"use client";

/**
 * 발급취소 확정 모달.
 * - 선택 행 건수 요약 (날짜 입력 없음)
 * - 확정 시 cancel API(TAX or SLIP) → 성공 시 invalidate + 성공 토스트
 * - 모달 루트는 <div>(중첩 form 금지), Enter 차단, portal 사용
 */

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { freightLineIssueKeys } from "@/application/bms/freight-line-issue/use-cases";
import { freightLineIssuePort } from "@/lib/ports";
import type { FreightLineIssueRow } from "@/application/bms/freight-line-issue/ports";
import type { IssueType } from "./freight-line-issue-list-config";

interface Props {
  isOpen: boolean;
  onClose: () => void;
  issueType: IssueType;
  rows: FreightLineIssueRow[];
  onCancelSuccess: () => void;
}

function FreightLineIssueCancelModalInner({
  onClose,
  issueType,
  rows,
  onCancelSuccess,
}: Omit<Props, "isOpen">) {
  const t = useTranslations("bms.issue.cancelModal");
  const queryClient = useQueryClient();

  const lineCount = rows.length;

  const cancelMutation = useMutation({
    mutationFn: () => {
      const req = { lineIds: rows.map((r) => r.freightLineId) };
      return issueType === "TAX"
        ? freightLineIssuePort.cancelTax(req)
        : freightLineIssuePort.cancelSlip(req);
    },
    onSuccess: () => {
      toast.success(t("successToast"));
      // TAX·SLIP 두 화면 모두 stale 마킹
      queryClient.invalidateQueries({ queryKey: freightLineIssueKeys.all });
      onCancelSuccess();
      onClose();
    },
    // 에러 토스트는 전역 MutationCache onError SSOT에 위임
  });

  return (
    <div
      className="modal__body"
      style={{ padding: "16px 20px 16px" }}
      onKeyDown={(e) => {
        // Enter 차단 (textarea 제외)
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
    >
      {/* 취소 대상 요약 */}
      <div style={{ marginBottom: 20, fontSize: 13, lineHeight: 1.6 }}>
        <span>
          {issueType === "TAX" ? t("summaryTax", { lineCount }) : t("summarySlip", { lineCount })}
        </span>
      </div>

      {/* 액션 버튼 */}
      <div className="modal__actions">
        <Button
          type="button"
          variant="transaction"
          onClick={() => cancelMutation.mutate()}
          disabled={cancelMutation.isPending}
          loading={cancelMutation.isPending}
        >
          {t("confirm")}
        </Button>
        <Button type="button" variant="normal" onClick={onClose}>
          {t("close")}
        </Button>
      </div>
    </div>
  );
}

export function FreightLineIssueCancelModal({
  isOpen,
  onClose,
  issueType,
  rows,
  onCancelSuccess,
}: Props) {
  const t = useTranslations("bms.issue.cancelModal");

  return (
    <ModalShell isOpen={isOpen} title={t("title")} size="md" portal>
      <FreightLineIssueCancelModalInner
        onClose={onClose}
        issueType={issueType}
        rows={rows}
        onCancelSuccess={onCancelSuccess}
      />
    </ModalShell>
  );
}
