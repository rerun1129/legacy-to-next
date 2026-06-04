"use client";

/**
 * 서류 발행 모달.
 * - 발행 대상 라인은 상위 그리드에서 선택 후 스냅샷으로 전달받음(selectedLines).
 * - customerCode / financialDocType 검증은 발행 버튼 onClick(freight-panels)에서 선행.
 * - 성공 시 모달 잔류(PRD §3). onClose 시 listByBl invalidate 보장.
 * - 에러 토스트는 전역 MutationCache onError SSOT에 위임(직접 catch 금지).
 */

import { useState, useMemo, useRef } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { ModalShell } from "@/components/shared/modal-shell";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { DateBox, CodeBox } from "@/components/shared/inputs";
import { toast } from "@/lib/toast-store";
import { getSession } from "@/lib/admin-session";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import { authUseCases } from "@/application/auth/use-cases";
import {
  financialDocumentKeys,
  financialDocumentUseCases,
} from "@/application/bms/financial-document/use-cases";
import { useEnumOptions } from "@/application/enums/use-enum";
import { resolvePerLabel } from "@/components/fms/house-bl/freight-per";

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
  // 외부 Freight 탭 그리드와 동일 컬럼 구성을 위한 추가 필드
  freightCode:      string;
  freightName:      string;
  exchangeRate:     number | null;
  per:              string;
  qty:              number | null;
  price:            number | null;
  taxType:          string;
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

  const { options: taxTypeOptions } = useEnumOptions("TaxType");
  // value→label 역방향 조회 맵 (코드→표시명)
  const taxTypeLabelMap = useMemo(
    () => new Map(taxTypeOptions.map((o) => [o.value, o.label])),
    [taxTypeOptions],
  );

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

  // ── Team / Operator 자동완성 ───────────────────────────────
  const teamAc = useCodeAutocomplete(CODE_SOURCES.team);
  const operatorAc = useCodeAutocomplete(CODE_SOURCES.user);

  // 직전 유효값 ref — invalid blur 시 선택 전 입력을 버리고 복원
  // team 초기 유효값: 세션에서 받은 defaultTeam
  const teamValidRef = useRef<string>(defaultTeam);
  // operator 초기 유효값: me 비동기 응답 전이므로 빈 문자열, blur 시 me.username 폴백
  const operatorValidRef = useRef<string>("");

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
  // 외부 Freight 탭(Selling/Buying) 그리드와 동일한 컬럼 구성·너비로 표시
  const lineColumns: GridColumn<SelectedFreightLine>[] = [
    {
      key: "freightCode",
      label: tf("cols.freightCode"),
      width: 80,
      align: "center",
    },
    {
      key: "freightName",
      label: tf("cols.freightName"),
      width: 260,
    },
    {
      key: "currency",
      label: tf("cols.currency"),
      width: 60,
      align: "center",
    },
    {
      key: "exchangeRate",
      label: tf("cols.exchangeRate"),
      className: "is-num",
      width: 90,
      render: (_, row) => row.exchangeRate != null ? row.exchangeRate.toFixed(2) : "",
    },
    {
      key: "per",
      label: tf("cols.per"),
      width: 80,
      align: "center",
      render: (_, row) => row.per ? resolvePerLabel(row.per) : "",
    },
    {
      key: "qty",
      label: tf("cols.qty"),
      className: "is-num",
      width: 80,
      render: (_, row) => row.qty != null ? String(row.qty) : "",
    },
    {
      key: "price",
      label: tf("cols.price"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.price != null ? row.price.toFixed(2) : "",
    },
    {
      key: "settleAmount",
      label: tf("cols.settleAmount"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.settleAmount != null ? row.settleAmount.toFixed(2) : "",
    },
    {
      key: "localAmount",
      label: tf("cols.localAmount"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.localAmount != null ? row.localAmount.toFixed(2) : "",
    },
    {
      key: "usdAmount",
      label: tf("cols.usdAmount"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.usdAmount != null ? row.usdAmount.toFixed(2) : "",
    },
    {
      key: "taxType",
      label: tf("cols.taxType"),
      width: 100,
      align: "center",
      render: (_, row) => row.taxType ? (taxTypeLabelMap.get(row.taxType) ?? row.taxType) : "",
    },
    {
      key: "vat",
      label: tf("cols.vat"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.vat != null ? row.vat.toFixed(2) : "",
    },
    {
      key: "_total",
      label: tf("cols.total"),
      className: "is-num",
      width: 100,
      render: (_, row) => {
        if (row.localAmount == null && row.vat == null) return "";
        return ((row.localAmount ?? 0) + (row.vat ?? 0)).toFixed(2);
      },
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
      <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 8, marginBottom: 12 }}>
        <div className="field">
          <div className="field__label">{ti("documentNo")}</div>
          <div className="field__input">
            <input className="input" readOnly value="" placeholder={ti("documentNoPlaceholder")} />
          </div>
        </div>
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
            <CodeBox
              kind="code-only"
              clearInvalidOnBlur={false}
              codeProps={{
                value: teamCode,
                onChange: (e) => setTeamCode(e.target.value),
                // 미선택 blur 시 직전 유효값으로 복원(자유 입력 차단)
                onBlur: () => setTeamCode(teamValidRef.current),
                placeholder: ti("team"),
              }}
              onSearch={teamAc.onSearch}
              suggestions={teamAc.suggestions}
              suggestionsLoading={teamAc.suggestionsLoading}
              onSelect={(it) => {
                setTeamCode(it.code);
                teamValidRef.current = it.code;
              }}
            />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{ti("operator")}</div>
          <div className="field__input">
            <CodeBox
              kind="code-only"
              clearInvalidOnBlur={false}
              codeProps={{
                value: operator || (meData?.username ?? ""),
                onChange: (e) => setOperator(e.target.value),
                // 미선택 blur 시 직전 유효값으로 복원, me.username 폴백(세션 초기값 보장)
                onBlur: () => setOperator(operatorValidRef.current || meData?.username || ""),
                placeholder: ti("operator"),
              }}
              onSearch={operatorAc.onSearch}
              suggestions={operatorAc.suggestions}
              suggestionsLoading={operatorAc.suggestionsLoading}
              onSelect={(it) => {
                setOperator(it.code);
                operatorValidRef.current = it.code;
              }}
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
