"use client";

import { useFormContext, useWatch, type Path } from "react-hook-form";
import { useTranslations } from "next-intl";
import { type GridColumn } from "@/components/shared/grid-list";
import { TextBox } from "@/components/shared/inputs";
import type { HouseBlFormValues, FreightRow } from "@/components/fms/house-bl/house-bl-schema";
import { getPerOptions, resolvePerLabel } from "@/components/fms/house-bl/freight-per";
import { formatDateDisplay } from "@/lib/date";
import {
  FreightCodeCell,
  CurrencyCell,
  CustomerCell,
  PerCell,
  TaxTypeCell,
  PerformanceDtCell,
  FinancialDocTypeCell,
  ReadOnlyCell,
  FreightLineIdCell,
  getFinancialDocOptions,
  type FieldPrefix,
} from "./freight-cells";
import {
  QtyCell,
  PriceCell,
  ExchangeRateCell,
  SettleAmountCell,
  LocalAmountCell,
  VatCell,
  UsdExchangeRateCell,
} from "./freight-calc-cells";

// ── 공통 컬럼 빌더 ────────────────────────────────────────────

export interface BuildColumnsArgs {
  prefix: FieldPrefix;
  tf: ReturnType<typeof useTranslations>;
  perOptions: ReturnType<typeof getPerOptions>;
  onPerChange: (index: number, value: string) => void;
  onCurrencySelect: (index: number, currencyCode: string) => void;
  taxTypeLabelMap: Map<string, string>;
}

// 발행 라인 판정 헬퍼 — financialDocumentNo 있는 행은 immutable(§6.17)
function isIssued(row: FreightRow): boolean {
  return Boolean(row.financialDocumentNo);
}

export function buildFreightColumns({
  prefix,
  tf,
  perOptions,
  onPerChange,
  onCurrencySelect,
  taxTypeLabelMap,
}: BuildColumnsArgs): GridColumn<FreightRow>[] {
  return [
    {
      key: "_no",
      label: tf("cols.no"),
      className: "row-num",
      width: 36,
      // FreightLineIdCell: hidden input으로 freightLineId를 RHF에 등록 (useFieldArray UUID 덮어쓰기 방지)
      render: (_, __, i) => (
        <>
          {i + 1}
          <FreightLineIdCell prefix={prefix} index={i} />
        </>
      ),
    },
    {
      key: "customerCode",
      label: tf("cols.customer"),
      width: 100,
      isRequired: true,
      render: (_, row, i) =>
        isIssued(row) ? <ReadOnlyCell value={row.customerCode} align="center" /> : <CustomerCell prefix={prefix} index={i} />,
    },
    {
      key: "customerName",
      label: tf("cols.customerName"),
      width: 260,
      render: (_, __, i) => <WatchReadOnlyCell prefix={prefix} index={i} field="customerName" />,
    },
    {
      key: "financialDocType",
      label: tf("cols.financialDocType"),
      width: 90,
      render: (_, row, i) => {
        if (isIssued(row)) {
          const docLabel = row.financialDocType
            ? (getFinancialDocOptions(prefix).find((o) => o.value === row.financialDocType)?.label ?? row.financialDocType)
            : "";
          return <ReadOnlyCell value={docLabel} align="center" />;
        }
        return <FinancialDocTypeCell prefix={prefix} index={i} />;
      },
    },
    {
      key: "freightCode",
      label: tf("cols.freightCode"),
      width: 80,
      isRequired: true,
      render: (_, row, i) =>
        isIssued(row) ? <ReadOnlyCell value={row.freightCode} align="center" /> : <FreightCodeCell prefix={prefix} index={i} />,
    },
    {
      key: "freightName",
      label: tf("cols.freightName"),
      width: 260,
      render: (_, __, i) => <WatchReadOnlyCell prefix={prefix} index={i} field="freightName" />,
    },
    {
      key: "currency",
      label: tf("cols.currency"),
      width: 60,
      isRequired: true,
      render: (_, row, i) =>
        isIssued(row)
          ? <ReadOnlyCell value={row.currency} align="center" />
          : <CurrencyCell prefix={prefix} index={i} onCurrencySelect={onCurrencySelect} />,
    },
    {
      key: "exchangeRate",
      label: tf("cols.exchangeRate"),
      className: "is-num",
      width: 90,
      render: (_, row, i) => {
        if (isIssued(row)) {
          const v = row.exchangeRate;
          return <ReadOnlyCell value={v != null && v !== "" ? Number(v).toFixed(2) : ""} />;
        }
        return <ExchangeRateCell prefix={prefix} index={i} />;
      },
    },
    {
      key: "per",
      label: tf("cols.per"),
      width: 80,
      isRequired: true,
      render: (_, row, i) =>
        isIssued(row)
          ? <ReadOnlyCell value={row.per ? resolvePerLabel(row.per) : ""} align="center" />
          : (
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
      width: 80,
      isRequired: true,
      render: (_, row, i) =>
        isIssued(row) ? <ReadOnlyCell value={row.qty} /> : <QtyCell prefix={prefix} index={i} />,
    },
    {
      key: "price",
      label: tf("cols.price"),
      className: "is-num",
      width: 100,
      isRequired: true,
      render: (_, row, i) => {
        if (isIssued(row)) {
          const v = row.price;
          return <ReadOnlyCell value={v != null && v !== "" ? Number(v).toFixed(2) : ""} />;
        }
        return <PriceCell prefix={prefix} index={i} />;
      },
    },
    {
      key: "settleAmount",
      label: tf("cols.settleAmount"),
      className: "is-num",
      width: 100,
      render: (_, row, i) => {
        if (isIssued(row)) {
          const v = row.settleAmount;
          return <ReadOnlyCell value={v != null && v !== "" ? Number(v).toFixed(2) : ""} />;
        }
        return <SettleAmountCell prefix={prefix} index={i} />;
      },
    },
    {
      key: "localAmount",
      label: tf("cols.localAmount"),
      className: "is-num",
      width: 100,
      render: (_, row, i) => {
        if (isIssued(row)) {
          const v = row.localAmount;
          return <ReadOnlyCell value={v != null && v !== "" ? Number(v).toFixed(2) : ""} />;
        }
        return <LocalAmountCell prefix={prefix} index={i} />;
      },
    },
    {
      key: "taxType",
      label: tf("cols.taxType"),
      width: 100,
      isRequired: true,
      render: (_, row, i) =>
        isIssued(row)
          ? <ReadOnlyCell value={row.taxType ? (taxTypeLabelMap.get(row.taxType) ?? row.taxType) : ""} align="center" />
          : <TaxTypeCell prefix={prefix} index={i} />,
    },
    {
      key: "vat",
      label: tf("cols.vat"),
      className: "is-num",
      width: 100,
      render: (_, row, i) => {
        if (isIssued(row)) {
          const v = row.vat;
          return <ReadOnlyCell value={v != null && v !== "" ? Number(v).toFixed(2) : ""} />;
        }
        return <VatCell prefix={prefix} index={i} />;
      },
    },
    {
      key: "total",
      label: tf("cols.total"),
      className: "is-num",
      width: 100,
      render: (_, __, i) => <TotalCell prefix={prefix} index={i} />,
    },
    {
      key: "performanceDt",
      label: tf("cols.performanceDt"),
      width: 100,
      isRequired: true,
      render: (_, row, i) =>
        isIssued(row) ? <ReadOnlyCell value={formatDateDisplay(row.performanceDt)} align="center" /> : <PerformanceDtCell prefix={prefix} index={i} />,
    },
    {
      key: "usdExchangeRate",
      label: tf("cols.usdExchangeRate"),
      className: "is-num",
      width: 90,
      render: (_, row, i) => {
        if (isIssued(row)) {
          const v = row.usdExchangeRate;
          return <ReadOnlyCell value={v != null && v !== "" ? Number(v).toFixed(2) : ""} />;
        }
        return <UsdExchangeRateCell prefix={prefix} index={i} />;
      },
    },
    {
      key: "usdAmount",
      label: tf("cols.usdAmount"),
      className: "is-num",
      width: 100,
      render: (_, __, i) => <WatchReadOnlyCell prefix={prefix} index={i} field="usdAmount" decimals={2} />,
    },
    {
      key: "financialDocumentNo",
      label: tf("cols.financialDocumentNo"),
      width: 120,
      render: (_, row) => <ReadOnlyCell value={row.financialDocumentNo} />,
    },
    {
      key: "taxNo",
      label: tf("cols.taxNo"),
      width: 120,
      render: (_, row) => <ReadOnlyCell value={row.taxNo} />,
    },
    {
      key: "slipNo",
      label: tf("cols.slipNo"),
      width: 120,
      render: (_, row) => <ReadOnlyCell value={row.slipNo} />,
    },
  ];
}

// ── Total 셀 — localAmount + vat 실시간 합산, readOnly, FE 표시 전용 ────
// useWatch로 직접 구독해 RHF fields[] 업데이트 지연 없이 실시간 반영

function TotalCell({ prefix, index }: { prefix: FieldPrefix; index: number }) {
  const { control } = useFormContext<HouseBlFormValues>();
  const local = useWatch({ control, name: `${prefix}.${index}.localAmount` as Path<HouseBlFormValues> });
  const vat = useWatch({ control, name: `${prefix}.${index}.vat` as Path<HouseBlFormValues> });
  const hasValue = (local != null && local !== "") || (vat != null && vat !== "");
  const total = (Number(local) || 0) + (Number(vat) || 0);
  return <TextBox variant="cell" readOnly value={hasValue ? total.toFixed(2) : ""} />;
}

// ── setValue로 갱신되는 readOnly 셀 — useWatch로 직접 구독 ────
// RHF useFieldArray fields[]는 setValue로 갱신되지 않으므로
// freightName/customerName/usdAmount 는 control 구독으로 실시간 반영.

function WatchReadOnlyCell({
  prefix,
  index,
  field,
  decimals,
}: {
  prefix: FieldPrefix;
  index: number;
  field: "freightName" | "customerName" | "usdAmount";
  decimals?: number;
}) {
  const { control } = useFormContext<HouseBlFormValues>();
  const value = useWatch({
    control,
    name: `${prefix}.${index}.${field}` as Path<HouseBlFormValues>,
  });
  let display = "";
  if (value != null && value !== "") {
    const n = Number(value);
    display = decimals != null && !Number.isNaN(n) ? n.toFixed(decimals) : String(value);
  }
  return <TextBox variant="cell" readOnly value={display} />;
}
