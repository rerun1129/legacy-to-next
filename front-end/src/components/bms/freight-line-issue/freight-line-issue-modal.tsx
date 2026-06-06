"use client";

/**
 * 발급 확정 모달.
 * - 발급일 입력 + 선택 행 요약 (고객명·건수·발급 종류)
 * - 확정 시 issue API(TAX or SLIP) → 성공 시 invalidate + 성공 토스트
 * - 모달 루트는 <div>(엔트리 폼 밖이지만 중첩 form 가능, Enter 차단으로 안전 확보)
 */

import { useState } from "react";
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
  onIssueSuccess: () => void;
}

function FreightLineIssueModalInner({
  onClose,
  issueType,
  rows,
  onIssueSuccess,
}: Omit<Props, "isOpen">) {
  const t = useTranslations("bms.issue.modal");
  const queryClient = useQueryClient();

  // 오늘 날짜 기본값 (yyyyMMdd)
  const today = (() => {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}${m}${day}`;
  })();

  const [issueDt, setIssueDt] = useState(today);

  // 선택 행 요약
  const customerName = rows[0]?.customerName ?? "";
  const lineCount = rows.length;
  const docType = rows[0]?.financialDocType ?? "";

  const issueMutation = useMutation({
    mutationFn: () => {
      const req = { issueDt, lineIds: rows.map((r) => r.freightLineId) };
      return issueType === "TAX"
        ? freightLineIssuePort.issueTax(req)
        : freightLineIssuePort.issueSlip(req);
    },
    onSuccess: (result) => {
      toast.success(t("successToast", { issueNo: result.issueNo }));
      // TAX·SLIP 두 화면 모두 stale 마킹 — refetchOnMount:true(기본)로 진입 시 최신 보장
      queryClient.invalidateQueries({ queryKey: freightLineIssueKeys.all });
      onIssueSuccess();
      onClose();
    },
    // 에러 토스트는 전역 MutationCache onError SSOT에 위임
  });

  function handleConfirm() {
    if (!issueDt || issueDt.length !== 8) {
      toast.error(t("issueDtRequired"));
      return;
    }
    issueMutation.mutate();
  }

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
      {/* 발급 요약 정보 */}
      <div style={{ marginBottom: 16, fontSize: 13, lineHeight: 1.6 }}>
        <div>
          <span style={{ color: "var(--color-text-secondary, #6b7280)", marginRight: 8 }}>
            {t("customer")}
          </span>
          <strong>{customerName}</strong>
        </div>
        <div>
          <span style={{ color: "var(--color-text-secondary, #6b7280)", marginRight: 8 }}>
            {t("docType")}
          </span>
          <strong>{docType}</strong>
        </div>
        <div>
          <span style={{ color: "var(--color-text-secondary, #6b7280)", marginRight: 8 }}>
            {t("lineCount")}
          </span>
          <strong>{lineCount}{t("lineUnit")}</strong>
        </div>
      </div>

      {/* 발급일 입력 */}
      <div className="lcn" style={{ marginBottom: 20 }}>
        <span className="lcn__label">{t("issueDt")}</span>
        <input
          type="text"
          className="lcn__name"
          value={issueDt}
          onChange={(e) => setIssueDt(e.target.value)}
          placeholder="YYYYMMDD"
          maxLength={8}
          style={{ gridColumn: "2 / span 2" }}
        />
      </div>

      {/* 액션 버튼 */}
      <div className="modal__actions">
        <Button
          type="button"
          variant="transaction"
          onClick={handleConfirm}
          disabled={issueMutation.isPending}
          loading={issueMutation.isPending}
        >
          {t("confirm")}
        </Button>
        <Button type="button" variant="normal" onClick={onClose}>
          {t("cancel")}
        </Button>
      </div>
    </div>
  );
}

export function FreightLineIssueModal({
  isOpen,
  onClose,
  issueType,
  rows,
  onIssueSuccess,
}: Props) {
  const t = useTranslations("bms.issue.modal");

  return (
    <ModalShell isOpen={isOpen} title={t("title")} size="md" portal>
      <FreightLineIssueModalInner
        onClose={onClose}
        issueType={issueType}
        rows={rows}
        onIssueSuccess={onIssueSuccess}
      />
    </ModalShell>
  );
}
