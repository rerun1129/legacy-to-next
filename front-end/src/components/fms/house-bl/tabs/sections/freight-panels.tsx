"use client";

import { useCallback, useMemo, useRef, useState } from "react";
import { useTranslations } from "next-intl";
import { useFormContext, useFieldArray, useWatch } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import type { HouseBlFormValues, FreightRow } from "@/components/fms/house-bl/house-bl-schema";
import { Button } from "@/components/shared/button";
import type { Mode } from "@/lib/bl-variants";
import { getPerOptions, computeQtySnapshot } from "@/components/fms/house-bl/freight-per";
import { type FieldPrefix } from "./freight-cells";
import { buildFreightColumns } from "./freight-columns";
import { recalcFromExchangeRate } from "@/components/fms/house-bl/freight-calc";

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
  remark:              "",
};

// ── 공통 패널 컴포넌트 ────────────────────────────────────────

interface FreightPanelProps {
  prefix: FieldPrefix;
  panelTitle: string;
  mode?: Mode;
}

function FreightPanel({ prefix, panelTitle, mode }: FreightPanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const { control, getValues, setValue } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: prefix });
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);

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

    // settle/local/usd/vat는 qty·price 없으므로 빈칸으로 시작
    append({
      ...EMPTY_FREIGHT_ROW,
      currency,
      exchangeRate,
      usdExchangeRate,
    });
    setSelectedKey(null);
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
    remove(targetIdx);
    setSelectedKey(null);
    focusedRowKeyRef.current = null;
  }

  return (
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
            disabled={fields.length === 0}
          >
            <Minus size={12} />
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
      />
    </div>
  );
}

// ── 공개 패널 컴포넌트 ────────────────────────────────────────

interface FreightSidePanelProps {
  mode?: Mode;
}

export function FreightSellingPanel({ mode }: FreightSidePanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  return (
    <FreightPanel
      prefix="freightSelling"
      panelTitle={tf("panels.sellingDebit")}
      mode={mode}
    />
  );
}

export function FreightBuyingPanel({ mode }: FreightSidePanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");
  return (
    <FreightPanel
      prefix="freightBuying"
      panelTitle={tf("panels.buyingCredit")}
      mode={mode}
    />
  );
}
