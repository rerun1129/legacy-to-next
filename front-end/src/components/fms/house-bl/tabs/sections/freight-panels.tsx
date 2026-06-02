"use client";

import { useMemo } from "react";
import { useTranslations } from "next-intl";
import { useFormContext } from "react-hook-form";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { TextBox, NumberBox } from "@/components/shared/inputs";
import type { HouseBlFormValues, FreightRow } from "@/components/fms/house-bl/house-bl-schema";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

// ── Types ──────────────────────────────────────────────────
interface AccountRow {
  id: number;
  docType: string; docNo: string; issueDate: string; amount: string; currency: string; status: string;
}

const ACCOUNT_ROWS: AccountRow[] = [];
const RATE_ROWS: FreightRow[] = [];

// ── Selling Panel ──────────────────────────────────────────
export function FreightSellingPanel() {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const { register } = useFormContext<HouseBlFormValues>();

  const sellingCols: GridColumn<FreightRow>[] = useMemo(() => [
    { key: "_no",  label: tf("cols.no"),          className: "row-num", render: (_, __, i) => i + 1 },
    { key: "code", label: tf("cols.chargeCode"),   isRequired: true,
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.code`)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
    { key: "desc", label: tf("cols.description"),
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.desc`)} /> },
    { key: "qty",  label: tf("cols.qty"),          className: "is-num",
      render: (_, __, i) => <NumberBox variant="cell" name={`freightSelling.${i}.qty`}  valueAsNumber={false} /> },
    { key: "unit", label: tf("cols.unit"),
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.unit`)} /> },
    { key: "sell", label: tf("cols.rate"),          className: "is-num", isRequired: true,
      render: (_, __, i) => <NumberBox variant="cell" name={`freightSelling.${i}.sell`} valueAsNumber={false} /> },
    { key: "_amt", label: tf("cols.amount"),        className: "is-num",
      render: (_, row) => <TextBox variant="cell" readOnly defaultValue={(parseFloat(row.sell ?? "0") * 2).toFixed(2)} /> },
    { key: "cur",  label: tf("cols.currency"),
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.cur`)} style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "_krw", label: tf("cols.krwEquiv"),      className: "is-num",
      render: (_, row) => <TextBox variant="cell" readOnly defaultValue={(parseFloat(row.sell ?? "0") * 2 * 1376.5).toFixed(0)} /> },
    { key: "_rem", label: tf("cols.remark"),
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.remark`)} /> },
  ], [register, tf]);

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tf("panels.sellingDebit")}</span>
      </div>
      <GridList columns={sellingCols} data={RATE_ROWS} rowKey={(row) => row.id} style={{ flex: 1 }} />
    </div>
  );
}

// ── Buying Panel ───────────────────────────────────────────
export function FreightBuyingPanel() {
  const tf = useTranslations("fms.houseBl.entry.freight");
  const { register } = useFormContext<HouseBlFormValues>();

  const buyingCols: GridColumn<FreightRow>[] = useMemo(() => [
    { key: "_no",  label: tf("cols.no"),          className: "row-num", render: (_, __, i) => i + 1 },
    { key: "code", label: tf("cols.chargeCode"),   isRequired: true,
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.code`)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
    { key: "desc", label: tf("cols.description"),
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.desc`)} /> },
    { key: "qty",  label: tf("cols.qty"),          className: "is-num",
      render: (_, __, i) => <NumberBox variant="cell" name={`freightBuying.${i}.qty`}  valueAsNumber={false} /> },
    { key: "unit", label: tf("cols.unit"),
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.unit`)} /> },
    { key: "buy",  label: tf("cols.rate"),          className: "is-num", isRequired: true,
      render: (_, __, i) => <NumberBox variant="cell" name={`freightBuying.${i}.buy`}  valueAsNumber={false} /> },
    { key: "_amt", label: tf("cols.amount"),        className: "is-num",
      render: (_, row) => <TextBox variant="cell" readOnly defaultValue={(parseFloat(row.buy ?? "0") * 2).toFixed(2)} /> },
    { key: "cur",  label: tf("cols.currency"),
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.cur`)} style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "_krw", label: tf("cols.krwEquiv"),      className: "is-num",
      render: (_, row) => <TextBox variant="cell" readOnly defaultValue={(parseFloat(row.buy ?? "0") * 2 * 1376.5).toFixed(0)} /> },
    { key: "_rem", label: tf("cols.remark"),
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.remark`)} /> },
  ], [register, tf]);

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tf("panels.buyingCredit")}</span>
      </div>
      <GridList columns={buyingCols} data={RATE_ROWS} rowKey={(row) => row.id} style={{ flex: 1 }} />
    </div>
  );
}

// ── Account Documents Panel ────────────────────────────────
export function FreightAccountPanel() {
  const tf = useTranslations("fms.houseBl.entry.freight");

  const accountCols: GridColumn<AccountRow>[] = [
    { key: "docType",   label: tf("cols.docType")   },
    { key: "docNo",     label: tf("cols.docNo")      },
    { key: "issueDate", label: tf("cols.issueDate")  },
    { key: "amount",    label: tf("cols.amount"),    className: "is-num" },
    { key: "currency",  label: tf("cols.currency")   },
    { key: "status",    label: tf("cols.status")     },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
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
