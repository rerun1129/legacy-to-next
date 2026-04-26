"use client";

import { Search } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

// ── Types ──────────────────────────────────────────────────
interface RateRow {
  code: string; desc: string; qty: string; unit: string;
  sell: string; buy: string; cur: string;
}

interface AccountRow {
  docType: string; docNo: string; issueDate: string; amount: string; currency: string; status: string;
}

// ── Selling columns ────────────────────────────────────────
const SELLING_COLS: GridColumn<RateRow>[] = [
  { key: "_no",  label: "#",           className: "row-num", render: (_, __, i) => i + 1 },
  { key: "code", label: "Charge Code", required: true,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
  { key: "desc", label: "Description",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",  label: "Qty", className: "is-num",
    render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "unit", label: "Unit",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "sell", label: "Rate",        className: "is-num", required: true,
    render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "_amt", label: "Amount",      className: "is-num",
    render: (_, row) => <NumericCell defaultValue={(parseFloat(row.sell) * 2).toFixed(2)} /> },
  { key: "cur",  label: "Currency",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "_krw", label: "KRW Equiv.",  className: "is-num",
    render: (_, row) => <NumericCell defaultValue={(parseFloat(row.sell) * 2 * 1376.5).toFixed(0)} /> },
  { key: "_rem", label: "Remark",      render: () => <input className="grid__cell-input" /> },
];

// ── Buying columns ─────────────────────────────────────────
const BUYING_COLS: GridColumn<RateRow>[] = [
  { key: "_no",  label: "#",           className: "row-num", render: (_, __, i) => i + 1 },
  { key: "code", label: "Charge Code", required: true,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
  { key: "desc", label: "Description",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",  label: "Qty", className: "is-num",
    render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "unit", label: "Unit",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "buy",  label: "Rate",        className: "is-num", required: true,
    render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "_amt", label: "Amount",      className: "is-num",
    render: (_, row) => <NumericCell defaultValue={(parseFloat(row.buy) * 2).toFixed(2)} /> },
  { key: "cur",  label: "Currency",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "_krw", label: "KRW Equiv.",  className: "is-num",
    render: (_, row) => <NumericCell defaultValue={(parseFloat(row.buy) * 2 * 1376.5).toFixed(0)} /> },
  { key: "_rem", label: "Remark",      render: () => <input className="grid__cell-input" /> },
];

// ── Account columns ────────────────────────────────────────
const ACCOUNT_COLS: GridColumn<AccountRow>[] = [
  { key: "docType",   label: "Doc Type"  },
  { key: "docNo",     label: "Doc No"    },
  { key: "issueDate", label: "Issue Date" },
  { key: "amount",    label: "Amount",   className: "is-num" },
  { key: "currency",  label: "Currency"  },
  { key: "status",    label: "Status"    },
];

// ── Sample data ────────────────────────────────────────────
const rateRows: RateRow[] = [
  { code: "OFR", desc: "Ocean Freight",     qty: "2 CONT", unit: "CONT", sell: "400.00", buy: "320.00", cur: "USD" },
  { code: "BAF", desc: "Bunker Adjustment", qty: "2 CONT", unit: "CONT", sell: "120.00", buy: "100.00", cur: "USD" },
  { code: "CAF", desc: "Currency Adj.",     qty: "2 CONT", unit: "CONT", sell: "50.00",  buy: "40.00",  cur: "USD" },
  { code: "LSF", desc: "Low Sulphur Fee",   qty: "2 CONT", unit: "CONT", sell: "80.00",  buy: "65.00",  cur: "USD" },
  { code: "THC", desc: "Terminal Handling", qty: "2 CONT", unit: "CONT", sell: "95.00",  buy: "80.00",  cur: "USD" },
  { code: "DOC", desc: "Documentation Fee", qty: "1 BL",  unit: "BL",   sell: "45.00",  buy: "30.00",  cur: "USD" },
];

const accountRows: AccountRow[] = [
  { docType: "INVOICE",     docNo: "INV-20260415", issueDate: "2026-04-15", amount: "48,500.00", currency: "USD", status: "발행완료" },
  { docType: "C/I",         docNo: "CI-20260415",  issueDate: "2026-04-15", amount: "48,500.00", currency: "USD", status: "발행완료" },
  { docType: "DEBIT NOTE",  docNo: "DN-20260420",  issueDate: "2026-04-20", amount: "1,490.00",  currency: "USD", status: "미결"    },
  { docType: "CREDIT NOTE", docNo: "CN-20260421",  issueDate: "2026-04-21", amount: "320.00",    currency: "USD", status: "미결"    },
  { docType: "RECEIPT",     docNo: "REC-20260423", issueDate: "2026-04-23", amount: "1,170.00",  currency: "USD", status: "수령"    },
  { docType: "B/L COPY",   docNo: "BLC-20260424", issueDate: "2026-04-24", amount: "0.00",      currency: "USD", status: "발행완료" },
];

// ── Rate input styles ──────────────────────────────────────
const CODE_INPUT: React.CSSProperties = {
  height: 22, padding: "0 24px 0 8px", fontSize: 10,
  background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4,
  color: "var(--ink)", fontFamily: "var(--font-mono)", outline: "none", width: 90, flexShrink: 0,
};
const NAME_INPUT: React.CSSProperties = {
  height: 22, padding: "0 8px", fontSize: 10,
  background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4,
  color: "var(--ink)", outline: "none", flex: 1, marginLeft: 6, minWidth: 0,
};
const CUR_SELECT: React.CSSProperties = {
  height: 22, padding: "0 6px", fontSize: 10,
  background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4,
  color: "var(--ink)", outline: "none", width: 55,
};
const RATE_INPUT: React.CSSProperties = {
  height: 22, width: 100, padding: "0 6px", fontSize: 10,
  background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4,
  color: "var(--ink)", fontFamily: "var(--font-mono)", textAlign: "right", outline: "none",
};
const DATE_INPUT: React.CSSProperties = {
  height: 22, width: 110, padding: "0 6px", fontSize: 10,
  background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4,
  color: "var(--ink)", outline: "none",
};

// ── Rate header 공통 블록 ──────────────────────────────────
interface CustomerItemProps { label: string; code: string; name: string }

function CustomerItem({ label, code, name }: CustomerItemProps) {
  return (
    <div className="party-block__head">
      <span style={{ fontSize: 10, minWidth: 110, flexShrink: 0 }}>{label}</span>
      <div className="party-cn">
        <div className="party-cn__code">
          <input className="text-mono" placeholder="Code" defaultValue={code} readOnly
            style={{ background: "var(--surface)", cursor: "default" }} />
          <Search size={12} className="party-cn__icon" />
        </div>
        <input className="party-cn__name" placeholder="Company Name" defaultValue={name} readOnly
          style={{ background: "var(--surface)", cursor: "default" }} />
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
        <input type="date" defaultValue="2026-04-26" style={DATE_INPUT} />
        <select style={CUR_SELECT}><option>USD</option><option>KRW</option><option>EUR</option></select>
        <input type="number" min="0" step="0.01" defaultValue="1376.50" style={RATE_INPUT} />
      </div>
    </div>
  );
}

// ── CUSTOMERS — 3-col FieldItemGrid ───────────────────────
const CUSTOMER_ITEMS: FieldItemDef[] = [
  { key: "actual-customer", label: "ACTUAL CUSTOMER", render: () => <CustomerItem label="ACTUAL CUSTOMER" code="HJTR001" name="한진무역(주)"    /> },
  { key: "liner",           label: "LINER",           render: () => <CustomerItem label="LINER"           code="COSCO"   name="COSCO SHIPPING" /> },
  { key: "settle-partner",  label: "SETTLE PARTNER",  render: () => <CustomerItem label="SETTLE PARTNER"  code="HJTR001" name="한진무역(주)"    /> },
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
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Selling / Debit</span>
        <span className="panel__rowcount">{rateRows.length}</span>
        <div className="panel__actions"><button className="btn btn--sm">+</button></div>
      </div>
      <GridList columns={SELLING_COLS} data={rateRows} rowKey={(_, i) => i} style={{ flex: 1 }} />
    </div>
  );
}

// ── Buying Panel ───────────────────────────────────────────
export function FreightBuyingPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Buying / Credit</span>
        <span className="panel__rowcount">{rateRows.length}</span>
        <div className="panel__actions"><button className="btn btn--sm">+</button></div>
      </div>
      <GridList columns={BUYING_COLS} data={rateRows} rowKey={(_, i) => i} style={{ flex: 1 }} />
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
        <span className="panel__rowcount">{accountRows.length}</span>
      </div>
      <div className="panel__body--flush">
        <GridList columns={ACCOUNT_COLS} data={accountRows} />
      </div>
    </div>
  );
}
