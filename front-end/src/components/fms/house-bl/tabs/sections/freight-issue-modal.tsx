"use client";

/**
 * 서류 발행 모달.
 * - 오픈 시 BE에서 issuableLines를 조회(폼 스냅샷 아님).
 * - 미발행 라인: 체크박스 선택 가능.
 * - 발행 라인: 목록 표시만(선택 불가).
 * - 단일 고객 프리체크(§6.14): 혼재 시 발행 버튼 비활성.
 * - 성공 시 모달 잔류(PRD). onClose 시에도 listByBl invalidate.
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
import type { IssuableLine } from "@/application/bms/financial-document/ports";

// ── 오늘 날짜 yyyyMMdd ─────────────────────────────────────────
function todayYyyyMmDd(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}${m}${day}`;
}

// ── DocumentType/Status FE 매핑 (BE 컴포넌트 의존 금지) ────────

const DOCUMENT_TYPE_MAP: Record<string, string> = {
  INVOICE: "Invoice",
  PAYMENT: "Payment",
  DEBIT: "Debit",
  CREDIT: "Credit",
};

function resolveDocType(code: string): string {
  return DOCUMENT_TYPE_MAP[code] ?? code;
}

// ── Props ──────────────────────────────────────────────────────

export interface FreightIssueModalProps {
  isOpen: boolean;
  onClose: () => void;
  blType: string;
  blId: string | number;
  freightType: "SELLING" | "BUYING";
  /** B/L detail 쿼리 캐시 도메인 키. 기본값 "house-bl". */
  blDomainKey?: "house-bl" | "master-bl" | "truck-bl" | "non-bl";
}

// ── Modal Inner ────────────────────────────────────────────────

function FreightIssueModalInner({
  onClose,
  blType,
  blId,
  freightType,
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

  // ── 선택 상태 ──────────────────────────────────────────────
  const [selectedKeys, setSelectedKeys] = useState<Set<string | number>>(new Set());

  // ── 발행 가능 라인 조회 ────────────────────────────────────
  const { data: lines, isLoading } = useQuery<IssuableLine[]>({
    queryKey: financialDocumentKeys.issuableLines(blType, blIdStr, freightType),
    queryFn: () => financialDocumentUseCases.findIssuableLines(blType, blIdStr, freightType),
  });
  const safeLines = useMemo(() => lines ?? [], [lines]);

  // 미발행 라인 / 발행 라인 분리
  const issuableLines = useMemo(
    () => safeLines.filter((l) => l.documentNo == null),
    [safeLines]
  );
  const issuedLines = useMemo(
    () => safeLines.filter((l) => l.documentNo != null),
    [safeLines]
  );

  // 선택된 라인 객체
  const selectedLines = useMemo(
    () => issuableLines.filter((l) => selectedKeys.has(l.freightLineId)),
    [issuableLines, selectedKeys]
  );

  // ── 선택 라인 자동 계산 ────────────────────────────────────
  // 단일 고객 체크(§6.14): 선택 라인의 customerCode가 2종 이상이면 혼재
  const selectedCustomerCodes = useMemo(
    () => new Set(selectedLines.map((l) => l.customerCode)),
    [selectedLines]
  );
  const isCustomerMixed = selectedCustomerCodes.size > 1;
  const singleCustomer =
    selectedCustomerCodes.size === 1
      ? [...selectedCustomerCodes][0]
      : "";

  const totalSettle = selectedLines.reduce(
    (sum, l) => sum + (l.settleAmount ?? 0),
    0
  );
  const totalLocal = selectedLines.reduce(
    (sum, l) => sum + (l.localAmount ?? 0),
    0
  );
  const totalVat = selectedLines.reduce(
    (sum, l) => sum + (l.settleTaxAmount ?? 0),
    0
  );
  const totalUsd = selectedLines.reduce(
    (sum, l) => sum + (l.usdAmount ?? 0),
    0
  );

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
    onSuccess: () => {
      // 성공 토스트 명시(전역 onError는 에러만)
      toast.success(ti("success"));
      // ① issuableLines invalidate(모달 갱신 — 발행 라인 documentNo 채워짐)
      queryClient.invalidateQueries({
        queryKey: financialDocumentKeys.issuableLines(blType, blIdStr, freightType),
      });
      // ② Account Documents invalidate
      queryClient.invalidateQueries({
        queryKey: financialDocumentKeys.listByBl(blType, blIdStr),
      });
      // ③ B/L detail invalidate(freight 그리드 reload → financialDocumentNo 채워짐)
      invalidateBlDetail(queryClient, blDomainKey, blId);
      // 모달은 닫지 않음(PRD: 잔류). 선택만 초기화.
      setSelectedKeys(new Set());
    },
    // 에러는 전역 MutationCache onError SSOT에 위임 — 여기서 catch 금지
  });

  // ── 발행 버튼 활성 조건 ────────────────────────────────────
  const canIssue =
    selectedKeys.size > 0 && !isCustomerMixed && !issueMutation.isPending;

  // ── 모달 닫기 — listByBl invalidate 보장 ──────────────────
  function handleClose() {
    queryClient.invalidateQueries({
      queryKey: financialDocumentKeys.listByBl(blType, blIdStr),
    });
    onClose();
  }

  // ── 그리드 컬럼 ───────────────────────────────────────────
  const lineColumns: GridColumn<IssuableLine>[] = [
    {
      key: "customerCode",
      label: ti("lineCols.customer"),
      width: 90,
      render: (_, row) => `${row.customerCode} ${row.customerName}`,
    },
    {
      key: "freightCode",
      label: ti("lineCols.freightCode"),
      width: 80,
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
      key: "settleTaxAmount",
      label: ti("lineCols.vat"),
      className: "is-num",
      width: 80,
      render: (_, row) => row.settleTaxAmount?.toFixed(2) ?? "",
    },
    {
      key: "usdAmount",
      label: ti("lineCols.usdAmount"),
      className: "is-num",
      width: 80,
      render: (_, row) => row.usdAmount?.toFixed(2) ?? "",
    },
    {
      key: "documentNo",
      label: ti("lineCols.docNo"),
      width: 110,
      render: (_, row) => row.documentNo ?? "",
    },
  ];

  return (
    <form
      noValidate
      className="modal__body"
      onKeyDown={(e) => {
        // Enter 차단 (textarea 제외) — 폼 의도치 않은 제출 방지
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

      {/* ── 선택 요약(고객/합계) ──────────────────────────── */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr 1fr 1fr", gap: 8, marginBottom: 8 }}>
        <div className="field">
          <div className="field__label">{ti("customer")}</div>
          <div className="field__input">
            <input className="input" readOnly value={singleCustomer} />
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

      {/* ── 고객 혼재 안내 ────────────────────────────────── */}
      {isCustomerMixed && (
        <div style={{ color: "var(--color-danger, #dc2626)", fontSize: 12, marginBottom: 6 }}>
          {ti("customerMixed")}
        </div>
      )}

      {/* ── 미발행 라인 그리드 ────────────────────────────── */}
      <div style={{ marginBottom: 4, fontSize: 12, color: "var(--color-text-secondary, #6b7280)" }}>
        {ti("desc")}
      </div>
      <div style={{ flex: 1, minHeight: 160 }}>
        <GridList<IssuableLine>
          columns={lineColumns}
          data={issuableLines}
          rowKey={(r) => r.freightLineId}
          selectable
          selectedKeys={selectedKeys}
          onSelectionChange={setSelectedKeys}
          isLoading={isLoading}
          emptyMessage="—"
        />
      </div>

      {/* ── 이미 발행된 라인 표시(있을 때만) ──────────────── */}
      {issuedLines.length > 0 && (
        <>
          <div style={{ marginTop: 8, marginBottom: 4, fontSize: 12, color: "var(--color-text-secondary, #6b7280)" }}>
            {tf("cols.financialDocumentNo")} — {issuedLines.length}건 발행됨
          </div>
          <div style={{ minHeight: 60 }}>
            <GridList<IssuableLine>
              columns={lineColumns}
              data={issuedLines}
              rowKey={(r) => r.freightLineId}
            />
          </div>
        </>
      )}

      {/* ── 액션 버튼 ────────────────────────────────────── */}
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
    </form>
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
        blDomainKey={blDomainKey}
      />
    </ModalShell>
  );
}
