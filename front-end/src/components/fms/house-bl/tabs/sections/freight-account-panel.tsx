"use client";

/**
 * Account Documents 패널.
 * blId/blType 수신 → listByBl 쿼리로 실데이터 표시.
 * selectable GridList + 삭제 버튼 → deleteDocument mutation.
 * 삭제 성공 시 listByBl + B/L detail invalidate(라인 해제·readOnly 해제).
 * 에러 토스트는 전역 MutationCache onError SSOT에 위임.
 */

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Trash2 } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import {
  financialDocumentKeys,
  financialDocumentUseCases,
} from "@/application/bms/financial-document/use-cases";
import type { FinancialDocument } from "@/application/bms/financial-document/ports";

// ── DocumentType/Status FE 매핑 (BE 컴포넌트 의존 금지) ────────

const DOCUMENT_TYPE_MAP: Record<string, string> = {
  INVOICE: "Invoice",
  PAYMENT: "Payment",
  DEBIT:   "Debit",
  CREDIT:  "Credit",
};

const DOCUMENT_STATUS_MAP: Record<string, string> = {
  CREATED: "Created",
  GROUPED: "Grouped",
  TAX:     "Tax Invoice",
  SLIP:    "Slip",
  CLEAR:   "Cleared",
};

function resolveDocType(code: string): string {
  return DOCUMENT_TYPE_MAP[code] ?? code;
}

function resolveDocStatus(code: string): string {
  return DOCUMENT_STATUS_MAP[code] ?? code;
}

// ── Account Documents Panel ────────────────────────────────────

interface FreightAccountPanelProps {
  blType?: "HOUSE" | "MASTER";
  blId?: string | number | null;
  blDomainKey?: "house-bl" | "master-bl" | "truck-bl" | "non-bl";
}

export function FreightAccountPanel({ blType, blId, blDomainKey = "house-bl" }: FreightAccountPanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const ti = useTranslations("fms.houseBl.entry.freight.issue");
  const queryClient = useQueryClient();
  const [selectedKeys, setSelectedKeys] = useState<Set<string | number>>(new Set());

  const hasBlId = Boolean(blId);
  const blIdStr = String(blId ?? "");
  const resolvedBlType = blType ?? "HOUSE";

  // ── 서류 목록 조회 ─────────────────────────────────────────
  const { data, isLoading } = useQuery<FinancialDocument[]>({
    queryKey: financialDocumentKeys.listByBl(resolvedBlType, blIdStr),
    queryFn: () => financialDocumentUseCases.listByBl(resolvedBlType, blIdStr),
    enabled: hasBlId,
  });
  const docs = data ?? [];

  // ── 삭제 Mutation ──────────────────────────────────────────
  // A5: 순서 보장 필요 없으므로 병렬 실행 가능하나 각 id별 개별 mutation으로 처리
  const deleteMutation = useMutation({
    mutationFn: async (ids: number[]) => {
      // 병렬 실행: rate-limit 이슈가 없는 내부 API이므로 Promise.all 적용
      await Promise.all(ids.map((id) => financialDocumentUseCases.deleteDocument(id)));
    },
    onSuccess: () => {
      toast.success(ti("deleteSuccess"));
      // listByBl invalidate(패널 갱신)
      queryClient.invalidateQueries({
        queryKey: financialDocumentKeys.listByBl(resolvedBlType, blIdStr),
      });
      // B/L detail invalidate(라인 해제 · readOnly 해제)
      const numId = Number(blId);
      if (!isNaN(numId)) {
        queryClient.invalidateQueries({ queryKey: [blDomainKey, "detail", numId] });
      }
      setSelectedKeys(new Set());
    },
    // 에러는 전역 MutationCache onError SSOT에 위임
  });

  function handleDelete() {
    const ids = [...selectedKeys].map(Number).filter((n) => !isNaN(n));
    if (ids.length === 0) return;
    deleteMutation.mutate(ids);
  }

  // ── 컬럼 정의 ─────────────────────────────────────────────
  const accountCols: GridColumn<FinancialDocument>[] = [
    {
      key: "documentType",
      label: tf("cols.docType"),
      width: 90,
      render: (_, row) => resolveDocType(row.documentType),
    },
    {
      key: "documentNo",
      label: tf("cols.docNo"),
      width: 120,
    },
    {
      key: "documentDt",
      label: tf("cols.issueDate"),
      width: 90,
    },
    {
      key: "localTotalAmount",
      label: tf("cols.amount"),
      className: "is-num",
      width: 110,
      render: (_, row) => row.localTotalAmount?.toFixed(2) ?? "",
    },
    {
      key: "customerCode",
      label: tf("cols.customer"),
      width: 90,
      render: (_, row) => `${row.customerCode} ${row.customerName}`,
    },
    {
      key: "status",
      label: tf("cols.status"),
      width: 90,
      render: (_, row) => resolveDocStatus(row.status),
    },
  ];

  return (
    <div
      className="panel"
      style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}
    >
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tf("panels.accountDocuments")}</span>
        <span className="panel__rowcount">{docs.length}</span>
        <div className="panel__actions">
          {/* 다중 삭제 버튼 — 선택 없으면 disabled */}
          <Button
            variant="danger"
            size="sm"
            iconOnly
            disabled={selectedKeys.size === 0 || deleteMutation.isPending}
            loading={deleteMutation.isPending}
            onClick={handleDelete}
          >
            <Trash2 size={12} />
          </Button>
        </div>
      </div>
      <div className="panel__body--flush" style={{ flex: 1, minHeight: 0 }}>
        <GridList<FinancialDocument>
          columns={accountCols}
          data={docs}
          rowKey={(row) => row.financialDocumentId}
          isLoading={isLoading}
          selectable
          selectedKeys={selectedKeys}
          onSelectionChange={setSelectedKeys}
        />
      </div>
    </div>
  );
}
