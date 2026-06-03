"use client";

import { useFormContext } from "react-hook-form";
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
      render: (_, __, i) => <QtyCell prefix={prefix} index={i} />,
    },
    {
      key: "price",
      label: tf("cols.price"),
      className: "is-num",
      width: 80,
      render: (_, __, i) => <PriceCell prefix={prefix} index={i} />,
    },
    {
      key: "currency",
      label: tf("cols.currency"),
      width: 60,
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
      render: (_, __, i) => <TaxTypeCell prefix={prefix} index={i} />,
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
      render: (_, __, i) => <SettleAmountCell prefix={prefix} index={i} />,
    },
    {
      key: "localAmount",
      label: tf("cols.localAmount"),
      className: "is-num",
      width: 90,
      render: (_, __, i) => <LocalAmountCell prefix={prefix} index={i} />,
    },
    {
      key: "vat",
      label: tf("cols.vat"),
      className: "is-num",
      width: 80,
      render: (_, __, i) => <VatCell prefix={prefix} index={i} />,
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
      width: 80,
      render: (_, row) => <ReadOnlyCell value={row.usdAmount} />,
    },
    {
      key: "financialDocType",
      label: tf("cols.financialDocType"),
      width: 90,
      render: (_, __, i) => <FinancialDocTypeCell prefix={prefix} index={i} />,
    },
    {
      key: "taxNo",
      label: tf("cols.taxNo"),
      width: 100,
      render: (_, row) => <ReadOnlyCell value={row.taxNo} />,
    },
    {
      key: "slipNo",
      label: tf("cols.slipNo"),
      width: 90,
      render: (_, row) => <ReadOnlyCell value={row.slipNo} />,
    },
    {
      key: "financialDocumentNo",
      label: tf("cols.financialDocumentNo"),
      width: 110,
      render: (_, row) => <ReadOnlyCell value={row.financialDocumentNo} />,
    },
    {
      key: "remark",
      label: tf("cols.remark"),
      width: 120,
      render: (_, __, i) => <FreightRemarkCell prefix={prefix} index={i} />,
    },
  ];
}

// ── Remark 셀 (inline — register 필요) ──────────────────────

function FreightRemarkCell({ prefix, index }: { prefix: FieldPrefix; index: number }) {
  const { register } = useFormContext<HouseBlFormValues>();
  return <TextBox variant="cell" {...register(`${prefix}.${index}.remark`)} />;
}
