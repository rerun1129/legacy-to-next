"use client";

import { useFormContext, useWatch, type Path } from "react-hook-form";
import { useTranslations } from "next-intl";
import { type GridColumn } from "@/components/shared/grid-list";
import { TextBox } from "@/components/shared/inputs";
import type { HouseBlFormValues, FreightRow } from "@/components/fms/house-bl/house-bl-schema";
import { getPerOptions, resolvePerLabel } from "@/components/fms/house-bl/freight-per";
import {
  FreightCodeCell,
  CurrencyCell,
  CustomerCell,
  PerCell,
  TaxTypeCell,
  PerformanceDtCell,
  FinancialDocTypeCell,
  ReadOnlyCell,
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
}

export function buildFreightColumns({
  prefix,
  tf,
  perOptions,
  onPerChange,
  onCurrencySelect,
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
      key: "customerCode",
      label: tf("cols.customer"),
      width: 100,
      isRequired: true,
      render: (_, __, i) => <CustomerCell prefix={prefix} index={i} />,
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
      render: (_, __, i) => <FinancialDocTypeCell prefix={prefix} index={i} />,
    },
    {
      key: "freightCode",
      label: tf("cols.freightCode"),
      width: 80,
      isRequired: true,
      render: (_, __, i) => <FreightCodeCell prefix={prefix} index={i} />,
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
      render: (_, __, i) => (
        <CurrencyCell prefix={prefix} index={i} onCurrencySelect={onCurrencySelect} />
      ),
    },
    {
      key: "exchangeRate",
      label: tf("cols.exchangeRate"),
      className: "is-num",
      width: 90,
      render: (_, __, i) => <ExchangeRateCell prefix={prefix} index={i} />,
    },
    {
      key: "per",
      label: tf("cols.per"),
      width: 80,
      isRequired: true,
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
      width: 80,
      isRequired: true,
      render: (_, __, i) => <QtyCell prefix={prefix} index={i} />,
    },
    {
      key: "price",
      label: tf("cols.price"),
      className: "is-num",
      width: 100,
      isRequired: true,
      render: (_, __, i) => <PriceCell prefix={prefix} index={i} />,
    },
    {
      key: "settleAmount",
      label: tf("cols.settleAmount"),
      className: "is-num",
      width: 100,
      render: (_, __, i) => <SettleAmountCell prefix={prefix} index={i} />,
    },
    {
      key: "localAmount",
      label: tf("cols.localAmount"),
      className: "is-num",
      width: 100,
      render: (_, __, i) => <LocalAmountCell prefix={prefix} index={i} />,
    },
    {
      key: "taxType",
      label: tf("cols.taxType"),
      width: 100,
      isRequired: true,
      render: (_, __, i) => <TaxTypeCell prefix={prefix} index={i} />,
    },
    {
      key: "vat",
      label: tf("cols.vat"),
      className: "is-num",
      width: 100,
      render: (_, __, i) => <VatCell prefix={prefix} index={i} />,
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
      render: (_, __, i) => <PerformanceDtCell prefix={prefix} index={i} />,
    },
    {
      key: "usdExchangeRate",
      label: tf("cols.usdExchangeRate"),
      className: "is-num",
      width: 90,
      render: (_, __, i) => <UsdExchangeRateCell prefix={prefix} index={i} />,
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
