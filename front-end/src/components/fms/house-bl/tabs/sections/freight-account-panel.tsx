"use client";

/**
 * Account Documents 패널.
 * blId/blType 수신 → listByBl 쿼리로 실데이터 표시.
 * selectable GridList + 삭제 버튼 → deleteDocument mutation.
 * 삭제 성공 시 listByBl + B/L detail invalidate(라인 해제·readOnly 해제).
 * Doc No 셀 더블클릭 → 발행/편집(amend) 모달 진입.
 *   - editableHeader·헤더 prefill은 모달 내부에서 listByBl 캐시 self-resolve.
 * 에러 토스트는 전역 MutationCache onError SSOT에 위임.
 */

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { useFormContext } from "react-hook-form";
import { Trash2 } from "lucide-react";
import { confirm } from "@/components/confirm";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { formatDateDisplay } from "@/lib/date";
import {
  financialDocumentKeys,
  financialDocumentUseCases,
} from "@/application/bms/financial-document/use-cases";
import type { FinancialDocument } from "@/application/bms/financial-document/ports";
import type { HouseBlFormValues, FreightRow } from "@/components/fms/house-bl/house-bl-schema";
import { FreightIssueModal, type SelectedFreightLine } from "./freight-issue-modal";
import { toSelectedFreightLine } from "./freight-issue-gate";

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

// ── 모달 상태 타입 ─────────────────────────────────────────────

interface AmendModalState {
  open: boolean;
  lines: SelectedFreightLine[];
  freightType: "SELLING" | "BUYING";
}

// ── Account Documents Panel ────────────────────────────────────

interface FreightAccountPanelProps {
  blType?: "HOUSE" | "MASTER";
  blId?: number | null;
  onFreightMutated?: () => void;
}

export function FreightAccountPanel({ blType, blId, onFreightMutated }: FreightAccountPanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const ti = useTranslations("fms.houseBl.entry.freight.issue");
  const queryClient = useQueryClient();
  const { getValues } = useFormContext<HouseBlFormValues>();
  const [selectedKeys, setSelectedKeys] = useState<Set<string | number>>(new Set());
  const [amendModal, setAmendModal] = useState<AmendModalState | null>(null);

  const hasBlId = blId != null;
  const resolvedBlType = blType ?? "HOUSE";

  // ── 서류 목록 조회 ─────────────────────────────────────────
  const { data, isLoading } = useQuery<FinancialDocument[]>({
    // blId가 null이면 enabled:false로 쿼리 비활성 — 더미 키는 실행되지 않음
    queryKey: financialDocumentKeys.listByBl(resolvedBlType, blId ?? 0),
    queryFn: () => financialDocumentUseCases.listByBl(resolvedBlType, blId!),
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
        queryKey: financialDocumentKeys.listByBl(resolvedBlType, blId ?? 0),
      });
      // B/L detail invalidate는 entry의 onFreightMutated 콜백으로 이관.
      // useFieldArray fields는 setValue로 갱신 불가하므로 detailLoadedRef 풀기+invalidate 패턴 필수.
      onFreightMutated?.();
      setSelectedKeys(new Set());
    },
    // 에러는 전역 MutationCache onError SSOT에 위임
  });

  async function handleDelete() {
    const ids = [...selectedKeys].map(Number).filter((n) => !isNaN(n));
    if (ids.length === 0) return;
    const ok = await confirm({
      title: ti("deleteConfirmTitle"),
      description: ti("deleteConfirmDesc", { count: ids.length }),
      variant: "destructive",
      confirmText: ti("deleteConfirmBtn"),
      cancelText: ti("cancel"),
    });
    if (!ok) return;
    deleteMutation.mutate(ids);
  }

  // ── Doc No 더블클릭 → amend 모달 진입 ─────────────────────
  function handleDocNoDblClick(doc: FinancialDocument) {
    const selling = (getValues("freightSelling") ?? []).filter(
      (r) => (r as FreightRow).financialDocumentId === doc.financialDocumentId,
    ) as FreightRow[];
    const buying = (getValues("freightBuying") ?? []).filter(
      (r) => (r as FreightRow).financialDocumentId === doc.financialDocumentId,
    ) as FreightRow[];
    const freightType: "SELLING" | "BUYING" = selling.length > 0 ? "SELLING" : "BUYING";
    const rows = selling.length > 0 ? selling : buying;
    // 해당 서류 라인을 폼에서 찾지 못하면 무시
    if (rows.length === 0) return;
    const lines = rows.map((r) => toSelectedFreightLine(r, freightType));
    // editableHeader·헤더 prefill은 모달 내부에서 listByBl 캐시 self-resolve
    setAmendModal({ open: true, lines, freightType });
  }

  // ── 컬럼 정의 ─────────────────────────────────────────────
  const accountCols: GridColumn<FinancialDocument>[] = [
    { key: "documentType", label: tf("cols.docType"), width: 90, align: "center",
      render: (_, row) => resolveDocType(row.documentType) },
    { key: "documentNo", label: tf("cols.docNo"), width: 120,
      render: (_, row) => (
        <span
          style={{ cursor: "pointer", textDecoration: "underline dotted" }}
          title={tf("cols.docNo")}
          onDoubleClick={() => handleDocNoDblClick(row)}
        >
          {row.documentNo}
        </span>
      ),
    },
    { key: "teamCode", label: tf("cols.team"), width: 80, align: "center",
      render: (_, row) => row.teamCode ?? "" },
    { key: "customerName", label: tf("cols.customerName"), width: 240,
      render: (_, row) => row.customerName },
    { key: "documentDt", label: tf("cols.issueDate"), width: 100, align: "center",
      render: (_, row) => formatDateDisplay(row.documentDt) },
    { key: "settleTotalAmount", label: tf("cols.settleTotalAmt"), className: "is-num", width: 110,
      render: (_, row) => row.settleTotalAmount?.toFixed(2) ?? "" },
    { key: "localTotalAmount", label: tf("cols.localTotalAmt"), className: "is-num", width: 110,
      render: (_, row) => row.localTotalAmount?.toFixed(2) ?? "" },
    { key: "localTotalVat", label: tf("cols.taxTotalAmt"), className: "is-num", width: 110,
      render: (_, row) => row.localTotalVat?.toFixed(2) ?? "" },
    { key: "status", label: tf("cols.status"), width: 90, align: "center",
      render: (_, row) => resolveDocStatus(row.status) },
    { key: "groupFinancialNo", label: tf("cols.groupDocNo"), width: 120,
      render: (_, row) => row.groupFinancialNo ?? "" },
  ];

  return (
    <>
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
      {amendModal && (
        <FreightIssueModal
          isOpen={amendModal.open}
          onClose={() => setAmendModal(null)}
          blType={resolvedBlType}
          blId={blId!}
          freightType={amendModal.freightType}
          selectedLines={amendModal.lines}
          onIssueSuccess={() => {
            onFreightMutated?.();
            setAmendModal(null);
          }}
        />
      )}
    </>
  );
}
