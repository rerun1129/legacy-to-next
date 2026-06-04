"use client";

/**
 * 서류 발행 모달.
 * - 발행 대상 라인은 상위 그리드에서 선택 후 스냅샷으로 전달받음(selectedLines).
 * - customerCode / financialDocType 검증은 발행 버튼 onClick(freight-panels)에서 선행.
 * - 성공 시 모달 잔류(PRD §3). onClose 시 listByBl invalidate 보장.
 * - 에러 토스트는 전역 MutationCache onError SSOT에 위임(직접 catch 금지).
 */

import { useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { ModalShell } from "@/components/shared/modal-shell";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { DateBox } from "@/components/shared/inputs";
import { toast } from "@/lib/toast-store";
import { getSession } from "@/lib/admin-session";
import { authUseCases } from "@/application/auth/use-cases";
import {
  financialDocumentKeys,
  financialDocumentUseCases,
} from "@/application/bms/financial-document/use-cases";

// ── 오늘 날짜 yyyyMMdd ─────────────────────────────────────────
function todayYyyyMmDd(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}${m}${day}`;
}

// ── DocumentType FE 매핑 (BE 컴포넌트 의존 금지) ──────────────

const DOCUMENT_TYPE_MAP: Record<string, string> = {
  INVOICE: "Invoice",
  PAYMENT: "Payment",
  DEBIT:   "Debit",
  CREDIT:  "Credit",
};

function resolveDocType(code: string): string {
  return DOCUMENT_TYPE_MAP[code] ?? code;
}

// ── 선택 행 스냅샷 타입 ────────────────────────────────────────
// 상위 그리드에서 체크 후 발행 버튼 onClick 시 고정되는 스냅샷.
// 이후 그리드 선택 변경과 독립적으로 모달이 동작하도록 분리.

export interface SelectedFreightLine {
  freightLineId:    number;
  customerCode:     string;
  customerName:     string;
  financialDocType: string;
  currency:         string;
  settleAmount:     number | null;
  localAmount:      number | null;
  vat:              number | null;
  usdAmount:        number | null;
  performanceDt:    string;
}

// ── Props ──────────────────────────────────────────────────────

export interface FreightIssueModalProps {
  isOpen: boolean;
  onClose: () => void;
  blType: string;
  blId: string | number;
  freightType: "SELLING" | "BUYING";
  /** 상위 그리드에서 체크 후 전달되는 발행 대상 스냅샷 */
  selectedLines: SelectedFreightLine[];
  /** 발행 성공 후 상위 그리드 체크박스 선택 해제 콜백 */
  onIssueSuccess: () => void;
  /** B/L detail 쿼리 캐시 도메인 키. 기본값 "house-bl". */
  blDomainKey?: "house-bl" | "master-bl" | "truck-bl" | "non-bl";
}

// ── Modal Inner ────────────────────────────────────────────────

function FreightIssueModalInner({
  onClose,
  blType,
  blId,
  freightType,
  selectedLines,
  onIssueSuccess,
  blDomainKey = "house-bl",
}: Omit<FreightIssueModalProps, "isOpen">) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const ti = useTranslations("fms.houseBl.entry.freight.issue");
  const queryClient = useQueryClient();

  // 로그인 세션에서 팀 기본값
  const session = getSession();
  const defaultTeam = session?.attributes?.["team"]?.[0] ?? "";

  const blIdStr = String(blId);

  // me 조회 — staleTime 캐시 히트 시 모달 오픈과 동시에 동기적으로 사용 가능.
  // operator 기본값(PRD §3): 로그인 사용자의 username.
  const { data: meData } = useQuery({
    queryKey: ["auth", "me"],
    queryFn: () => authUseCases.me(),
    staleTime: 5 * 60 * 1000,
  });

  // ── 헤더 입력 상태 ─────────────────────────────────────────
  const [documentDt, setDocumentDt] = useState<string>(todayYyyyMmDd);
  const [performanceDt, setPerformanceDt] = useState<string>(todayYyyyMmDd);
  const [teamCode, setTeamCode] = useState<string>(defaultTeam);
  // operator는 빈 문자열로 초기화 — 표시·제출 시 meData.username 폴백(setState-in-effect 회피)
  const [operator, setOperator] = useState<string>("");

  // ── 선택 라인 합계 ─────────────────────────────────────────
  const totalSettle = useMemo(
    () => selectedLines.reduce((sum, l) => sum + (l.settleAmount ?? 0), 0),
    [selectedLines],
  );
  const totalLocal = useMemo(
    () => selectedLines.reduce((sum, l) => sum + (l.localAmount ?? 0), 0),
    [selectedLines],
  );
  const totalVat = useMemo(
    () => selectedLines.reduce((sum, l) => sum + (l.vat ?? 0), 0),
    [selectedLines],
  );
  const totalUsd = useMemo(
    () => selectedLines.reduce((sum, l) => sum + (l.usdAmount ?? 0), 0),
    [selectedLines],
  );

  // 단일 customer/docType 요약 (선행 검증 통과 전제, 방어적 표시용)
  const singleCustomer = selectedLines.length > 0 ? selectedLines[0].customerCode : "";
  const singleDocType  = selectedLines.length > 0 ? resolveDocType(selectedLines[0].financialDocType) : "";

  // ── 발행 Mutation ──────────────────────────────────────────
  const issueMutation = useMutation({
    mutationFn: () =>
      financialDocumentUseCases.issueDocument({
        blType,
        blId: blIdStr,
        freightType,
        lineIds: selectedLines.map((l) => l.freightLineId),
        documentDt,
        performanceDt,
        teamCode: teamCode || null,
        // 사용자가 직접 입력하지 않은 경우 me.username 폴백(PRD §3)
        operator: operator || meData?.username || null,
      }),
    onSuccess: (result) => {
      // 성공 토스트 명시(전역 onError는 에러만) — 발행된 document_no 포함
      toast.success(`${ti("success")} (${result.documentNo})`);
      // ① listByBl invalidate (Account Documents 갱신)
      queryClient.invalidateQueries({
        queryKey: financialDocumentKeys.listByBl(blType, blIdStr),
      });
      // ② B/L detail invalidate (freight 그리드 reload → financialDocumentNo 채워짐)
      invalidateBlDetail(queryClient, blDomainKey, blId);
      // ③ 상위 그리드 체크박스 선택 해제
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

  // ── 선택 라인 읽기전용 그리드 컬럼 ───────────────────────────
  const lineColumns: GridColumn<SelectedFreightLine>[] = [
    {
      key: "customerCode",
      label: ti("lineCols.customer"),
      width: 90,
      render: (_, row) => `${row.customerCode} ${row.customerName}`,
    },
    {
      key: "financialDocType",
      label: tf("cols.financialDocType"),
      width: 80,
      render: (_, row) => resolveDocType(row.financialDocType),
    },
    {
      key: "currency",
      label: ti("lineCols.currency"),
      width: 60,
    },
    {
      key: "settleAmount",
      label: ti("lineCols.settleAmount"),
      className: "is-num",
      width: 90,
      render: (_, row) => row.settleAmount?.toFixed(2) ?? "",
    },
    {
      key: "localAmount",
      label: ti("lineCols.localAmount"),
      className: "is-num",
      width: 90,
      render: (_, row) => row.localAmount?.toFixed(2) ?? "",
    },
    {
      key: "vat",
      label: ti("lineCols.vat"),
      className: "is-num",
      width: 80,
      render: (_, row) => row.vat?.toFixed(2) ?? "",
    },
    {
      key: "usdAmount",
      label: ti("lineCols.usdAmount"),
      className: "is-num",
      width: 80,
      render: (_, row) => row.usdAmount?.toFixed(2) ?? "",
    },
    {
      key: "performanceDt",
      label: ti("performanceDt"),
      width: 90,
    },
  ];

  // 발행 버튼: 발행 성공 후 재발행 방지(isSuccess 시 비활성)
  const canIssue = !issueMutation.isPending && !issueMutation.isSuccess;

  return (
    <div
      className="modal__body"
      onKeyDown={(e) => {
        // Enter 차단 (textarea 제외) — 부모 엔트리 폼으로 버블링돼 저장 제출되는 것을 막음
        if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
          e.preventDefault();
        }
      }}
    >
      {/* ── 헤더 입력 섹션 ─────────────────────────────────── */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 8, marginBottom: 12 }}>
        <div className="field">
          <div className="field__label">{ti("documentDt")}</div>
          <div className="field__input">
            <DateBox
              name="documentDt"
              value={documentDt}
              onChange={(e) => setDocumentDt((e.target as HTMLInputElement).value)}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{ti("performanceDt")}</div>
          <div className="field__input">
            <DateBox
              name="performanceDt"
              value={performanceDt}
              onChange={(e) => setPerformanceDt((e.target as HTMLInputElement).value)}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{ti("team")}</div>
          <div className="field__input">
            <input
              className="input"
              value={teamCode}
              onChange={(e) => setTeamCode(e.target.value)}
              placeholder={ti("team")}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{ti("operator")}</div>
          <div className="field__input">
            <input
              className="input"
              value={operator || (meData?.username ?? "")}
              onChange={(e) => setOperator(e.target.value)}
              placeholder={ti("operator")}
            />
          </div>
        </div>
      </div>

      {/* ── 선택 요약(고객/DocType/합계) ─────────────────────── */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr 1fr 1fr 1fr", gap: 8, marginBottom: 8 }}>
        <div className="field">
          <div className="field__label">{ti("customer")}</div>
          <div className="field__input">
            <input className="input" readOnly value={singleCustomer} />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{tf("cols.financialDocType")}</div>
          <div className="field__input">
            <input className="input" readOnly value={singleDocType} />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{ti("lineCols.settleAmount")}</div>
          <div className="field__input">
            <input className="input" readOnly value={totalSettle ? totalSettle.toFixed(2) : ""} style={{ textAlign: "right" }} />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{ti("lineCols.localAmount")}</div>
          <div className="field__input">
            <input className="input" readOnly value={totalLocal ? totalLocal.toFixed(2) : ""} style={{ textAlign: "right" }} />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{ti("lineCols.vat")}</div>
          <div className="field__input">
            <input className="input" readOnly value={totalVat ? totalVat.toFixed(2) : ""} style={{ textAlign: "right" }} />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{ti("lineCols.usdAmount")}</div>
          <div className="field__input">
            <input className="input" readOnly value={totalUsd ? totalUsd.toFixed(2) : ""} style={{ textAlign: "right" }} />
          </div>
        </div>
      </div>

      {/* ── 선택 라인 읽기전용 목록 ──────────────────────────── */}
      <div style={{ marginBottom: 4, fontSize: 12, color: "var(--color-text-secondary, #6b7280)" }}>
        {ti("desc")} ({selectedLines.length}건)
      </div>
      <div style={{ flex: 1, minHeight: 120 }}>
        <GridList<SelectedFreightLine>
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

// ── B/L detail 쿼리 invalidate 헬퍼 ───────────────────────────
function invalidateBlDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  domainKey: string,
  blId: string | number
) {
  const numId = Number(blId);
  if (isNaN(numId)) return;
  queryClient.invalidateQueries({ queryKey: [domainKey, "detail", numId] });
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
  blDomainKey,
}: FreightIssueModalProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const panelLabel =
    freightType === "SELLING" ? tf("panels.sellingDebit") : tf("panels.buyingCredit");

  return (
    <ModalShell
      isOpen={isOpen}
      title={panelLabel}
      size="lg"
    >
      <FreightIssueModalInner
        onClose={onClose}
        blType={blType}
        blId={blId}
        freightType={freightType}
        selectedLines={selectedLines}
        onIssueSuccess={onIssueSuccess}
        blDomainKey={blDomainKey}
      />
    </ModalShell>
  );
}
