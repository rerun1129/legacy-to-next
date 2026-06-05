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
import { useEnumOptions } from "@/application/enums/use-enum";
import { type FieldPrefix } from "./freight-cells";
import { buildFreightColumns } from "./freight-columns";
import { recalcFromExchangeRate } from "@/components/fms/house-bl/freight-calc";
import { FreightIssueModal, type SelectedFreightLine } from "./freight-issue-modal";
import { toast } from "@/lib/toast-store";
import { buildNewFreightRow } from "./freight-row-factory";
import { evaluateIssueSelection } from "./freight-issue-gate";

export type { FieldPrefix };

// ── 공통 패널 컴포넌트 ────────────────────────────────────────

interface FreightPanelProps {
  prefix: FieldPrefix;
  panelTitle: string;
  mode?: Mode;
  blType?: string;
  blId?: number | null;
  onFreightMutated?: () => void;
}

function FreightPanel({ prefix, panelTitle, mode, blType, blId, onFreightMutated }: FreightPanelProps) {
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

  // 발행 행 TaxType 코드 → 표시명 변환 맵 (freight-issue-columns.tsx 기준 패턴)
  const { options: taxTypeOptions } = useEnumOptions("TaxType");
  const taxTypeLabelMap = useMemo(
    () => new Map(taxTypeOptions.map((o) => [o.value, o.label])),
    [taxTypeOptions],
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
        taxTypeLabelMap,
      }),
    [prefix, tf, perOptions, handlePerChange, handleCurrencySelect, taxTypeLabelMap],
  );

  const selectedIdx =
    selectedKey !== null ? fields.findIndex((f) => f.id === selectedKey) : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    const row = buildNewFreightRow(getValues(), prefix, mode);
    append(row);
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

  // ── 발행 버튼 onClick — 게이트 평가 후 토스트/모달 처리 ────────
  function handleIssueClick() {
    const formValues = getValues();
    const result = evaluateIssueSelection({
      rows: (formValues[prefix] ?? []) as FreightRow[],
      fieldIds: fields.map((f) => (f as unknown as { id: string }).id),
      issueSelectedKeys,
      prefixDirtyFields: (dirtyFields as Record<string, Record<number, object> | undefined>)[prefix],
      freightType,
    });
    if (result.kind === "error") { toast.error(ti(result.messageKey)); return; }
    setModalSelectedLines(result.lines);
    setIsIssueModalOpen(true);
  }

  // 발행 성공 후 모달에서 호출 — 그리드 선택 해제 + entry detail 재조회 트리거
  function handleIssueSuccess() {
    setIssueSelectedKeys(new Set());
    onFreightMutated?.();
  }

  // 미저장(blId 없음) 또는 발행 버튼 비활성 조건
  const hasBlId = Boolean(blId);
  // freightType: handleIssueClick 스냅샷 빌드 및 모달 prop 전달에 공통 사용
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
  blId?: number | null;
  onFreightMutated?: () => void;
}

export function FreightSellingPanel({ mode, blType, blId, onFreightMutated }: FreightSidePanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  return (
    <FreightPanel
      prefix="freightSelling"
      panelTitle={tf("panels.sellingDebit")}
      mode={mode}
      blType={blType}
      blId={blId}
      onFreightMutated={onFreightMutated}
    />
  );
}

export function FreightBuyingPanel({ mode, blType, blId, onFreightMutated }: FreightSidePanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  return (
    <FreightPanel
      prefix="freightBuying"
      panelTitle={tf("panels.buyingCredit")}
      mode={mode}
      blType={blType}
      blId={blId}
      onFreightMutated={onFreightMutated}
    />
  );
}
