"use client";

import { useMemo } from "react";
import { useTranslations } from "next-intl";
import { useFormContext } from "react-hook-form";
import { Search } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { TextBox, NumberBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { HouseBlFormValues, FreightRow } from "@/components/fms/house-bl/house-bl-schema";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

// ── Types ──────────────────────────────────────────────────
interface AccountRow {
  id: number;
  docType: string; docNo: string; issueDate: string; amount: string; currency: string; status: string;
}

const ACCOUNT_ROWS: AccountRow[] = [];

// ── Sample data ────────────────────────────────────────────
const RATE_ROWS: FreightRow[] = [];

// ── Rate header 공통 블록 ──────────────────────────────────
interface CustomerItemProps { label: string; code: string; name: string }

function CustomerItem({ label, code, name }: CustomerItemProps) {
  return (
    <div className="party-block__head">
      <span style={{ fontSize: 10, minWidth: 110, flexShrink: 0 }}>{label}</span>
      <div className="party-cn">
        <div className="party-cn__code">
          <TextBox variant="panel" readOnly defaultValue={code} />
          <Search size={12} className="party-cn__icon" />
        </div>
        <TextBox variant="panel" readOnly defaultValue={name} />
      </div>
    </div>
  );
}

interface ExRateItemProps { label: string }

function ExRateItem({ label }: ExRateItemProps) {
  return (
    <div className="party-block__head">
      <span style={{ fontSize: 10, minWidth: 110, flexShrink: 0, color: "var(--ink-2)" }}>{label}</span>
      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
        <TextBox variant="panel" readOnly defaultValue="" />
        <TextBox variant="panel" readOnly defaultValue="" />
        <TextBox variant="panel" readOnly defaultValue="" />
      </div>
    </div>
  );
}

// ── Rate Headers Panel (FieldWidgetList + 3-col FieldItemGrid) ──
export function FreightRatePanel() {
  const tf = useTranslations("fms.houseBl.entry.freight");

  // ── CUSTOMERS — 3-col FieldItemGrid ─────────────────────
  // layout persisted by itemScope + item key (not label/array identity) — safe to define in-component
  const customerItems: FieldItemDef[] = [
    { key: "actual-customer", label: tf("customers.actualCustomer"), render: () => <CustomerItem label={tf("customers.actualCustomer")} code="" name="" /> },
    { key: "liner",           label: tf("customers.liner"),          render: () => <CustomerItem label={tf("customers.liner")}          code="" name="" /> },
    { key: "settle-partner",  label: tf("customers.settlePartner"),  render: () => <CustomerItem label={tf("customers.settlePartner")}  code="" name="" /> },
  ];

  // ── Ex. Rate Info — 3-col FieldItemGrid ─────────────────
  // layout persisted by itemScope + item key — safe to define in-component
  const exrateItems: FieldItemDef[] = [
    { key: "selling-rate", label: tf("exRate.selling"), render: () => <ExRateItem label={tf("exRate.selling")} /> },
    { key: "buying-rate",  label: tf("exRate.buying"),  render: () => <ExRateItem label={tf("exRate.buying")}  /> },
    { key: "perf-rate",    label: tf("exRate.perf"),    render: () => <ExRateItem label={tf("exRate.perf")}    /> },
  ];

  const fields: FieldWidgetDef[] = [
    { key: "customers",    label: tf("headers.customers"),  render: () => <FieldItemGrid itemScope="freight-rate-v2.customers"      items={customerItems} cols={2} /> },
    { key: "ex-rate-info", label: tf("headers.exRateInfo"), render: () => <FieldItemGrid itemScope="freight-rate-v2.ex-rate-info-v2" items={exrateItems}   cols={2} /> },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{tf("panels.rateHeaders")}</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="freight-rate-v2" fields={fields} />
      </div>
    </div>
  );
}

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

  // layout persisted by GridList column key (not label) — safe to define in-component
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
