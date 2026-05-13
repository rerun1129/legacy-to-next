"use client";

import { useMemo } from "react";
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



// ── Account columns ────────────────────────────────────────
const ACCOUNT_COLS: GridColumn<AccountRow>[] = [
  { key: "docType",   label: "Doc Type"  },
  { key: "docNo",     label: "Doc No"    },
  { key: "issueDate", label: "Issue Date" },
  { key: "amount",    label: "Amount",   className: "is-num" },
  { key: "currency",  label: "Currency"  },
  { key: "status",    label: "Status"    },
];

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

// ── CUSTOMERS — 3-col FieldItemGrid ───────────────────────
const CUSTOMER_ITEMS: FieldItemDef[] = [
  { key: "actual-customer", label: "ACTUAL CUSTOMER", render: () => <CustomerItem label="ACTUAL CUSTOMER" code="" name="" /> },
  { key: "liner",           label: "LINER",           render: () => <CustomerItem label="LINER"           code="" name="" /> },
  { key: "settle-partner",  label: "SETTLE PARTNER",  render: () => <CustomerItem label="SETTLE PARTNER"  code="" name="" /> },
];

// ── Ex. Rate Info — 3-col FieldItemGrid ───────────────────
const EXRATE_ITEMS: FieldItemDef[] = [
  { key: "selling-rate", label: "SELLING", render: () => <ExRateItem label="SELLING" /> },
  { key: "buying-rate",  label: "BUYING",  render: () => <ExRateItem label="BUYING"  /> },
  { key: "perf-rate",    label: "PERF.",   render: () => <ExRateItem label="PERF."   /> },
];

// ── Rate Headers Panel (FieldWidgetList + 3-col FieldItemGrid) ──
export function FreightRatePanel() {
  const fields: FieldWidgetDef[] = [
    { key: "customers",    label: "CUSTOMERS",    render: () => <FieldItemGrid itemScope="freight-rate-v2.customers"    items={CUSTOMER_ITEMS} cols={3} /> },
    { key: "ex-rate-info", label: "Ex. Rate Info", render: () => <FieldItemGrid itemScope="freight-rate-v2.ex-rate-info-v2" items={EXRATE_ITEMS} cols={3} /> },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Rate Headers</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="freight-rate-v2" fields={fields} />
      </div>
    </div>
  );
}

// ── Selling Panel ──────────────────────────────────────────
export function FreightSellingPanel() {
  const { register } = useFormContext<HouseBlFormValues>();

  const sellingCols: GridColumn<FreightRow>[] = useMemo(() => [
    { key: "_no",  label: "#",           className: "row-num", render: (_, __, i) => i + 1 },
    { key: "code", label: "Charge Code", isRequired: true,
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.code`)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
    { key: "desc", label: "Description",
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.desc`)} /> },
    { key: "qty",  label: "Qty", className: "is-num",
      render: (_, __, i) => <NumberBox variant="cell" name={`freightSelling.${i}.qty`}  valueAsNumber={false} /> },
    { key: "unit", label: "Unit",
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.unit`)} /> },
    { key: "sell", label: "Rate", className: "is-num", isRequired: true,
      render: (_, __, i) => <NumberBox variant="cell" name={`freightSelling.${i}.sell`} valueAsNumber={false} /> },
    { key: "_amt", label: "Amount", className: "is-num",
      render: (_, row) => <TextBox variant="cell" readOnly defaultValue={(parseFloat(row.sell ?? "0") * 2).toFixed(2)} /> },
    { key: "cur",  label: "Currency",
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.cur`)} style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "_krw", label: "KRW Equiv.", className: "is-num",
      render: (_, row) => <TextBox variant="cell" readOnly defaultValue={(parseFloat(row.sell ?? "0") * 2 * 1376.5).toFixed(0)} /> },
    { key: "_rem", label: "Remark",
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightSelling.${i}.remark`)} /> },
  ], [register]);

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Selling / Debit</span>
      </div>
      <GridList columns={sellingCols} data={RATE_ROWS} rowKey={(row) => row.id} style={{ flex: 1 }} />
    </div>
  );
}

// ── Buying Panel ───────────────────────────────────────────
export function FreightBuyingPanel() {
  const { register } = useFormContext<HouseBlFormValues>();

  const buyingCols: GridColumn<FreightRow>[] = useMemo(() => [
    { key: "_no",  label: "#",           className: "row-num", render: (_, __, i) => i + 1 },
    { key: "code", label: "Charge Code", isRequired: true,
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.code`)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
    { key: "desc", label: "Description",
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.desc`)} /> },
    { key: "qty",  label: "Qty", className: "is-num",
      render: (_, __, i) => <NumberBox variant="cell" name={`freightBuying.${i}.qty`}  valueAsNumber={false} /> },
    { key: "unit", label: "Unit",
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.unit`)} /> },
    { key: "buy",  label: "Rate", className: "is-num", isRequired: true,
      render: (_, __, i) => <NumberBox variant="cell" name={`freightBuying.${i}.buy`}  valueAsNumber={false} /> },
    { key: "_amt", label: "Amount", className: "is-num",
      render: (_, row) => <TextBox variant="cell" readOnly defaultValue={(parseFloat(row.buy ?? "0") * 2).toFixed(2)} /> },
    { key: "cur",  label: "Currency",
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.cur`)} style={{ fontFamily: "var(--font-mono)" }} /> },
    { key: "_krw", label: "KRW Equiv.", className: "is-num",
      render: (_, row) => <TextBox variant="cell" readOnly defaultValue={(parseFloat(row.buy ?? "0") * 2 * 1376.5).toFixed(0)} /> },
    { key: "_rem", label: "Remark",
      render: (_, __, i) => <TextBox variant="cell" {...register(`freightBuying.${i}.remark`)} /> },
  ], [register]);

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Buying / Credit</span>
      </div>
      <GridList columns={buyingCols} data={RATE_ROWS} rowKey={(row) => row.id} style={{ flex: 1 }} />
    </div>
  );
}

// ── Account Documents Panel ────────────────────────────────
export function FreightAccountPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Account Documents</span>
        <span className="panel__rowcount">{ACCOUNT_ROWS.length}</span>
      </div>
      <div className="panel__body--flush">
        <GridList columns={ACCOUNT_COLS} data={ACCOUNT_ROWS} rowKey={(row) => row.id} />
      </div>
    </div>
  );
}
