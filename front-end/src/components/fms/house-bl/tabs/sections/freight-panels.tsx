"use client";

import { useCallback, useMemo, useRef, useState } from "react";
import { useTranslations } from "next-intl";
import { useFormContext, useFieldArray, useWatch } from "react-hook-form";
import { Plus, Minus } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { TextBox, NumberBox } from "@/components/shared/inputs";
import type { HouseBlFormValues, FreightRow } from "@/components/fms/house-bl/house-bl-schema";
import { Button } from "@/components/shared/button";
import type { Mode } from "@/lib/bl-variants";
import { getPerOptions, computeQtySnapshot, resolvePerLabel } from "@/components/fms/house-bl/freight-per";
import {
  FreightCodeCell,
  CurrencyCell,
  CustomerCell,
  PerCell,
  TaxTypeCell,
  PerformanceDtCell,
  ReadOnlyCell,
  TAX_TYPE_OPTIONS,
  type FieldPrefix,
} from "./freight-cells";

export type { FieldPrefix };

// ── 상수 ──────────────────────────────────────────────────────

const EMPTY_FREIGHT_ROW: FreightRow = {
  freightCode:      "",
  freightName:      "",
  per:              "",
  qty:              "",
  price:            "",
  currency:         "",
  customerCode:     "",
  customerName:     "",
  taxType:          "",
  performanceDt:    "",
  settleAmount:     "",
  localAmount:      "",
  usdAmount:        "",
  financialDocType: "",
  remark:           "",
};

// ── 공통 컬럼 빌더 ────────────────────────────────────────────

interface BuildColumnsArgs {
  prefix: FieldPrefix;
  tf: ReturnType<typeof useTranslations>;
  perOptions: ReturnType<typeof getPerOptions>;
  onPerChange: (index: number, value: string) => void;
}

function buildFreightColumns({
  prefix,
  tf,
  perOptions,
  onPerChange,
}: BuildColumnsArgs): GridColumn<FreightRow>[] {
  return [
    {
      key: "_no",
      label: tf("cols.no"),
      className: "row-num",
      width: 36,
      render: (_, __, i) => i + 1,
    },
    {
      key: "freightCode",
      label: tf("cols.freightCode"),
      width: 80,
      render: (_, __, i) => <FreightCodeCell prefix={prefix} index={i} />,
    },
    {
      key: "freightName",
      label: tf("cols.freightName"),
      width: 130,
      render: (_, row) => <TextBox variant="cell" readOnly value={row.freightName ?? ""} />,
    },
    {
      key: "per",
      label: tf("cols.per"),
      width: 80,
      render: (_, __, i) => (
        <PerCell
          prefix={prefix}
          index={i}
          perOptions={perOptions}
          onPerChange={onPerChange}
          resolveLabel={(code) => resolvePerLabel(code)}
        />
      ),
    },
    {
      key: "qty",
      label: tf("cols.qty"),
      className: "is-num",
      width: 60,
      render: (_, __, i) => (
        <NumberBox variant="cell" name={`${prefix}.${i}.qty`} valueAsNumber={false} />
      ),
    },
    {
      key: "price",
      label: tf("cols.price"),
      className: "is-num",
      width: 80,
      render: (_, __, i) => (
        <NumberBox variant="cell" name={`${prefix}.${i}.price`} valueAsNumber={false} />
      ),
    },
    {
      key: "currency",
      label: tf("cols.currency"),
      width: 60,
      render: (_, __, i) => <CurrencyCell prefix={prefix} index={i} />,
    },
    {
      key: "customerCode",
      label: tf("cols.customer"),
      width: 80,
      render: (_, __, i) => <CustomerCell prefix={prefix} index={i} />,
    },
    {
      key: "customerName",
      label: tf("cols.customerName"),
      width: 120,
      render: (_, row) => <TextBox variant="cell" readOnly value={row.customerName ?? ""} />,
    },
    {
      key: "taxType",
      label: tf("cols.taxType"),
      width: 80,
      render: (_, __, i) => (
        <TaxTypeCell prefix={prefix} index={i} options={TAX_TYPE_OPTIONS} />
      ),
    },
    {
      key: "performanceDt",
      label: tf("cols.performanceDt"),
      width: 100,
      render: (_, __, i) => <PerformanceDtCell prefix={prefix} index={i} />,
    },
    {
      key: "settleAmount",
      label: tf("cols.settleAmount"),
      className: "is-num",
      width: 90,
      render: () => <ReadOnlyCell />, // A2 BE 산정
    },
    {
      key: "localAmount",
      label: tf("cols.localAmount"),
      className: "is-num",
      width: 90,
      render: () => <ReadOnlyCell />, // A2 BE 산정
    },
    {
      key: "usdAmount",
      label: tf("cols.usdAmount"),
      className: "is-num",
      width: 80,
      render: () => <ReadOnlyCell />, // A2 BE 산정
    },
    {
      key: "financialDocType",
      label: tf("cols.financialDocType"),
      width: 80,
      render: () => <ReadOnlyCell />, // A2 BE 산정
    },
    {
      key: "remark",
      label: tf("cols.remark"),
      width: 120,
      render: (_, __, i) => {
        // TextBox register는 useFormContext에서 직접 사용 불가 — 컬럼 render는 컴포넌트 밖
        // FreightRemarkCell로 분리하지 않고 name prop 활용
        return <FreightRemarkCell prefix={prefix} index={i} />;
      },
    },
  ];
}

// ── Remark 셀 (inline — register 필요) ──────────────────────

function FreightRemarkCell({ prefix, index }: { prefix: FieldPrefix; index: number }) {
  const { register } = useFormContext<HouseBlFormValues>();
  return <TextBox variant="cell" {...register(`${prefix}.${index}.remark`)} />;
}

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
    // getValues/setValue/computeQtySnapshot은 렌더 사이클 내 안정 참조.
    // prefix는 컴포넌트 수명 내 불변.
    [getValues, setValue, prefix],
  );

  const columns = useMemo<GridColumn<FreightRow>[]>(
    () =>
      buildFreightColumns({
        prefix,
        tf,
        perOptions,
        onPerChange: handlePerChange,
      }),
    [prefix, tf, perOptions, handlePerChange],
  );

  const selectedIdx =
    selectedKey !== null ? fields.findIndex((f) => f.id === selectedKey) : -1;

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
  }

  function handleAdd() {
    append({ ...EMPTY_FREIGHT_ROW });
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

// ── Account Documents Panel ────────────────────────────────────

interface AccountRow {
  id: number;
  docType: string;
  docNo: string;
  issueDate: string;
  amount: string;
  currency: string;
  status: string;
}

const ACCOUNT_ROWS: AccountRow[] = [];

export function FreightAccountPanel() {
  const tf = useTranslations("fms.houseBl.entry.freight");

  const accountCols: GridColumn<AccountRow>[] = [
    { key: "docType",   label: tf("cols.docType")  },
    { key: "docNo",     label: tf("cols.docNo")    },
    { key: "issueDate", label: tf("cols.issueDate") },
    { key: "amount",    label: tf("cols.amount"),  className: "is-num" },
    { key: "currency",  label: tf("cols.currency") },
    { key: "status",    label: tf("cols.status")   },
  ];

  return (
    <div
      className="panel"
      style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}
    >
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tf("panels.accountDocuments")}</span>
        <span className="panel__rowcount">{ACCOUNT_ROWS.length}</span>
      </div>
      <div className="panel__body--flush">
        <GridList columns={accountCols} data={ACCOUNT_ROWS} rowKey={(row) => row.id} />
      </div>
    </div>
  );
}
