"use client";

import { useCallback, useMemo, useRef, useState } from "react";
import { useTranslations } from "next-intl";
import { useFormContext, useFieldArray, useFormState, useWatch } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import type { HouseBlFormValues, FreightRow } from "@/components/fms/house-bl/house-bl-schema";
import { Button } from "@/components/shared/button";
import type { Mode } from "@/lib/bl-variants";
import { getPerOptions, computeQtySnapshot } from "@/components/fms/house-bl/freight-per";
import { type FieldPrefix } from "./freight-cells";
import { buildFreightColumns } from "./freight-columns";
import { recalcFromExchangeRate } from "@/components/fms/house-bl/freight-calc";
import { FreightIssueModal, type SelectedFreightLine } from "./freight-issue-modal";
import { toast } from "@/lib/toast-store";

export type { FieldPrefix };

// ── 상수 ──────────────────────────────────────────────────────

const EMPTY_FREIGHT_ROW: FreightRow = {
  freightCode:         "",
  freightName:         "",
  per:                 "",
  qty:                 "",
  price:               "",
  currency:            "",
  exchangeRate:        "",
  customerCode:        "",
  customerName:        "",
  taxType:             "",
  performanceDt:       "",
  settleAmount:        "",
  localAmount:         "",
  vat:                 "",
  usdExchangeRate:     "",
  usdAmount:           "",
  financialDocType:    "",
  taxNo:               "",
  slipNo:              "",
  financialDocumentNo: "",
};

// ── 공통 패널 컴포넌트 ────────────────────────────────────────

interface FreightPanelProps {
  prefix: FieldPrefix;
  panelTitle: string;
  mode?: Mode;
  blType?: string;
  blId?: string | number | null;
  blDomainKey?: "house-bl" | "master-bl" | "truck-bl" | "non-bl";
}

function FreightPanel({ prefix, panelTitle, mode, blType, blId, blDomainKey = "house-bl" }: FreightPanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const ti = useTranslations("fms.houseBl.entry.freight.issue");
  const { control, getValues, setValue } = useFormContext<HouseBlFormValues>();
  // dirtyFields 구독 — 핸들러에서 stale 없이 최신값을 읽기 위해 useFormState 사용
  const { dirtyFields } = useFormState({ control });
  const { fields, append, remove } = useFieldArray({ control, name: prefix });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);
  const [isIssueModalOpen, setIsIssueModalOpen] = useState(false);

  // 그리드 행 체크박스 선택 — 발행 게이트 전용 (단일 선택과 독립)
  const [issueSelectedKeys, setIssueSelectedKeys] = useState<Set<string | number>>(new Set());
  // 모달에 전달할 스냅샷 — 모달 오픈 시 고정, 이후 그리드 선택 변경과 독립
  const [modalSelectedLines, setModalSelectedLines] = useState<SelectedFreightLine[]>([]);

  // containers는 per 옵션 동적 치환에 필요 — useWatch로 리렌더 최소화
  // rawContainers를 useMemo로 안정화해 ?? [] 인라인 폴백의 매 렌더 새 배열 참조 방지
  const rawContainers = useWatch({ control, name: "containers" });
  const containers = useMemo(() => rawContainers ?? [], [rawContainers]);

  const perOptions = useMemo(
    () => getPerOptions(mode ?? "NON_BL", containers),
    [mode, containers],
  );

  // per 선택 시 qty 스냅샷 1회 setValue — watch 아닌 getValues 사용(형제 focus 보호)
  const handlePerChange = useCallback(
    (index: number, perCode: string) => {
      const formValues = getValues();
      const qty = computeQtySnapshot(perCode, formValues);
      setValue(`${prefix}.${index}.qty`, qty);
    },
    [getValues, setValue, prefix],
  );

  // currency 선택 시: 헤더 통화와 같으면 헤더 환율을 exchangeRate에 주입 → 하위 재계산
  const handleCurrencySelect = useCallback(
    (index: number, currencyCode: string) => {
      const formValues = getValues();
      const headerCurrency =
        prefix === "freightSelling"
          ? formValues.sellRateCurrencyCode
          : formValues.buyRateCurrencyCode;
      const headerRate =
        prefix === "freightSelling" ? formValues.sellRate : formValues.buyRate;

      if (currencyCode && currencyCode === headerCurrency && headerRate) {
        setValue(`${prefix}.${index}.exchangeRate` as Parameters<typeof setValue>[0], headerRate, { shouldDirty: true });
        // exchangeRate 갱신 후 계산 체인 실행 (settle → local → usd, vat)
        const rows = formValues[prefix];
        const row = rows?.[index];
        const calcRow = {
          settleAmount:    row?.settleAmount,
          exchangeRate:    headerRate,
          localAmount:     row?.localAmount,
          usdExchangeRate: row?.usdExchangeRate,
          taxType:         row?.taxType,
          vat:             row?.vat,
        };
        const result = recalcFromExchangeRate(calcRow);
        const outputKeys = ["localAmount", "usdAmount", "vat"] as const;
        for (const key of outputKeys) {
          const val = result[key];
          if (val !== undefined) {
            setValue(
              `${prefix}.${index}.${key}` as Parameters<typeof setValue>[0],
              val,
              { shouldDirty: true },
            );
          }
        }
      }
    },
    [getValues, setValue, prefix],
  );

  const columns = useMemo<GridColumn<FreightRow>[]>(
    () =>
      buildFreightColumns({
        prefix,
        tf,
        perOptions,
        onPerChange: handlePerChange,
        onCurrencySelect: handleCurrencySelect,
      }),
    [prefix, tf, perOptions, handlePerChange, handleCurrencySelect],
  );

  const selectedIdx =
    selectedKey !== null ? fields.findIndex((f) => f.id === selectedKey) : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    // 헤더 환율 → 신규 행에 기본 바인딩 후 계산 체인 실행
    const formValues = getValues();
    const currency =
      prefix === "freightSelling"
        ? (formValues.sellRateCurrencyCode ?? "")
        : (formValues.buyRateCurrencyCode ?? "");
    const exchangeRate =
      prefix === "freightSelling"
        ? (formValues.sellRate ?? "")
        : (formValues.buyRate ?? "");
    const usdExchangeRate = formValues.usdRate ?? "";

    // 헤더 당사자 → 신규 행 Customer 자동 바인딩
    let customerCode = "";
    let customerName = "";
    if (prefix === "freightSelling") {
      customerCode = formValues.actualCustomerCode ?? "";
      customerName = formValues.actualCustomerName ?? "";
    } else {
      if (mode === "AIR") {
        customerCode = formValues.airDetail?.airlineCode ?? "";
        customerName = formValues.airDetail?.airlineName ?? "";
      } else {
        customerCode = formValues.seaDetail?.linerCode ?? "";
        customerName = formValues.linerName ?? "";
      }
    }

    // settle/local/usd/vat는 qty·price 없으므로 빈칸으로 시작
    append({
      ...EMPTY_FREIGHT_ROW,
      currency,
      exchangeRate,
      usdExchangeRate,
      customerCode,
      customerName,
    });
    setSelectedKey(null);
  }

  // 발행 라인 판정: RHF fieldArray fields[]는 setValue로 갱신 안 됨 →
  // financialDocumentNo는 detail-load/reset으로만 채워지므로 fields[].financialDocumentNo 직접 참조 OK
  function isIssuedRow(idx: number): boolean {
    const row = fields[idx] as unknown as FreightRow;
    return Boolean(row?.financialDocumentNo);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const focused = focusedRowKeyRef.current;
    let targetIdx = -1;
    if (focused !== null) {
      targetIdx = fields.findIndex(
        (f) => (f as unknown as { id: string }).id === focused,
      );
    }
    if (targetIdx === -1 && selectedKey !== null && selectedIdx !== -1) {
      targetIdx = selectedIdx;
    }
    if (targetIdx === -1) targetIdx = fields.length - 1;
    // 발행 라인은 삭제 불가(§6.17 immutable)
    if (targetIdx !== -1 && isIssuedRow(targetIdx)) return;
    remove(targetIdx);
    setSelectedKey(null);
    focusedRowKeyRef.current = null;
  }

  // ── 발행 버튼 onClick 검증 ────────────────────────────────────
  function handleIssueClick() {
    // 1. 선택 0개 → 차단
    if (issueSelectedKeys.size === 0) {
      toast.error(ti("selectRequired"));
      return;
    }

    // 2. dirty 검사 — 선택된 행 중 하나라도 변경(미저장)되면 전체 차단
    // dirtyFields[prefix]?.[index]가 존재하고 키가 있으면 해당 행은 dirty
    // 신규 추가행도 defaultValues에 없으므로 RHF가 dirty로 간주 → 동일 게이트에서 차단
    const prefixDirtyFields = (dirtyFields as Record<string, Record<number, object> | undefined>)[prefix];
    const hasDirtySelected = Array.from(issueSelectedKeys).some((key) => {
      const idx = fields.findIndex((f) => (f as unknown as { id: string }).id === key);
      if (idx === -1) return false;
      const rowDirty = prefixDirtyFields?.[idx];
      return rowDirty !== undefined && Object.keys(rowDirty).length > 0;
    });
    if (hasDirtySelected) {
      toast.error(ti("dirtyRowsSaveFirst"));
      return;
    }

    const formValues = getValues();
    const rows = (formValues[prefix] ?? []) as FreightRow[];

    // 3. 선택된 행에서 발행 완료(financialDocumentNo 보유) 행 제외 → 후보 수집
    const candidateRows: FreightRow[] = [];
    for (let i = 0; i < fields.length; i++) {
      const fieldId = (fields[i] as unknown as { id: string }).id;
      if (!issueSelectedKeys.has(fieldId)) continue;

      const row = rows[i];
      if (!row) continue;
      if (row.financialDocumentNo) continue;

      candidateRows.push(row);
    }

    if (candidateRows.length === 0) {
      toast.error(ti("selectRequired"));
      return;
    }

    // 4. customerCode distinct 검증
    const customerCodes = new Set(candidateRows.map((r) => r.customerCode ?? ""));
    if (customerCodes.size > 1) {
      toast.error(ti("customerMixed"));
      return;
    }

    // 5. financialDocType distinct 검증
    const docTypes = new Set(candidateRows.map((r) => r.financialDocType ?? ""));
    if (docTypes.size > 1) {
      toast.error(ti("docTypeMixed"));
      return;
    }

    // 6. 검증 통과 — 스냅샷 고정 후 모달 오픈
    const snapshot: SelectedFreightLine[] = candidateRows.map((r) => ({
      freightLineId:    r.freightLineId!,
      customerCode:     r.customerCode ?? "",
      customerName:     r.customerName ?? "",
      financialDocType: r.financialDocType ?? "",
      currency:         r.currency ?? "",
      settleAmount:     r.settleAmount ? Number(r.settleAmount) : null,
      localAmount:      r.localAmount  ? Number(r.localAmount)  : null,
      vat:              r.vat          ? Number(r.vat)          : null,
      usdAmount:        r.usdAmount    ? Number(r.usdAmount)    : null,
      performanceDt:    r.performanceDt ?? "",
      freightCode:      r.freightCode ?? "",
      freightName:      r.freightName ?? "",
      exchangeRate:     r.exchangeRate ? Number(r.exchangeRate) : null,
      per:              r.per ?? "",
      qty:              r.qty ? Number(r.qty) : null,
      price:            r.price ? Number(r.price) : null,
      taxType:          r.taxType ?? "",
    }));
    setModalSelectedLines(snapshot);
    setIsIssueModalOpen(true);
  }

  // 발행 성공 후 모달에서 호출 — 그리드 선택 해제
  function handleIssueSuccess() {
    setIssueSelectedKeys(new Set());
  }

  // 미저장(blId 없음) 또는 발행 버튼 비활성 조건
  const hasBlId = Boolean(blId);
  // freightType 결정
  const freightType: "SELLING" | "BUYING" =
    prefix === "freightSelling" ? "SELLING" : "BUYING";
  // blType 기본값(undefined 보호)
  const resolvedBlType = blType ?? "HOUSE";

  // Minus 버튼: 선택/포커스 행이 발행 라인이면 비활성
  const selectedRowIsIssued =
    selectedIdx !== -1 ? isIssuedRow(selectedIdx) : false;

  return (
    <>
      <div
        className="panel"
        style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{panelTitle}</span>
          <span className="panel__rowcount">{fields.length}</span>
          <div className="panel__actions">
            <Button variant="success" size="sm" iconOnly onClick={handleAdd}>
              <Plus size={12} />
            </Button>
            <Button
              variant="danger"
              size="sm"
              iconOnly
              onMouseDown={captureFocusedRow}
              onClick={handleRemove}
              disabled={fields.length === 0 || selectedRowIsIssued}
            >
              <Minus size={12} />
            </Button>
            {/* 발행 버튼 — 미저장 B/L이면 disabled */}
            <Button
              variant="transaction"
              size="sm"
              disabled={!hasBlId}
              onClick={handleIssueClick}
            >
              {ti("issueBtn")}
            </Button>
          </div>
        </div>
        <GridList
          columns={columns}
          data={fields as unknown as FreightRow[]}
          rowKey={(r) => String((r as unknown as { id: string }).id)}
          onRowClick={(r) => setSelectedKey((r as unknown as { id: string }).id)}
          rowClassName={(r) =>
            (r as unknown as { id: string }).id === selectedKey ? "is-selected" : undefined
          }
          onClearRow={() => setSelectedKey(null)}
          style={{ flex: 1, minHeight: 0 }}
          selectable
          selectedKeys={issueSelectedKeys}
          onSelectionChange={setIssueSelectedKeys}
        />
      </div>
      {/* 발행 모달 — FreightPanel 외부에 mount하여 DOM 깊이 문제 방지 */}
      {hasBlId && (
        <FreightIssueModal
          isOpen={isIssueModalOpen}
          onClose={() => setIsIssueModalOpen(false)}
          blType={resolvedBlType}
          blId={blId!}
          freightType={freightType}
          blDomainKey={blDomainKey}
          selectedLines={modalSelectedLines}
          onIssueSuccess={handleIssueSuccess}
        />
      )}
    </>
  );
}

// ── 공개 패널 컴포넌트 ────────────────────────────────────────

interface FreightSidePanelProps {
  mode?: Mode;
  blType?: string;
  blId?: string | number | null;
  blDomainKey?: "house-bl" | "master-bl" | "truck-bl" | "non-bl";
}

export function FreightSellingPanel({ mode, blType, blId, blDomainKey }: FreightSidePanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  return (
    <FreightPanel
      prefix="freightSelling"
      panelTitle={tf("panels.sellingDebit")}
      mode={mode}
      blType={blType}
      blId={blId}
      blDomainKey={blDomainKey}
    />
  );
}

export function FreightBuyingPanel({ mode, blType, blId, blDomainKey }: FreightSidePanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  return (
    <FreightPanel
      prefix="freightBuying"
      panelTitle={tf("panels.buyingCredit")}
      mode={mode}
      blType={blType}
      blId={blId}
      blDomainKey={blDomainKey}
    />
  );
}
