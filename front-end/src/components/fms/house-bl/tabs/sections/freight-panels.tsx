"use client";

import { useState } from "react";
import { useFormContext, useFieldArray } from "react-hook-form";
import { Search, Plus, Minus } from "lucide-react";
import type { HouseBlFormValues } from "../../house-bl-schema";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";

// ── Types ──────────────────────────────────────────────────
interface FreightRow {
  id: number;
  code: string; desc: string; qty: string; unit: string; amount: string; cur: string;
}

interface AccountRow {
  id: number;
  docType: string; docNo: string; issueDate: string; amount: string; currency: string; status: string;
}

// ── Selling columns ────────────────────────────────────────
const SELLING_COLS: GridColumn<FreightRow>[] = [
  { key: "_no",    label: "#",           className: "row-num", render: (_, __, i) => i + 1 },
  { key: "code",   label: "Charge Code", isRequired: true,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
  { key: "desc",   label: "Description",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",    label: "Qty", className: "is-num",
    render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "unit",   label: "Unit",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "amount", label: "Amount",      className: "is-num", isRequired: true,
    render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "cur",    label: "Currency",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "_rem",   label: "Remark",      render: () => <input className="grid__cell-input" /> },
];

// ── Buying columns ─────────────────────────────────────────
const BUYING_COLS: GridColumn<FreightRow>[] = [
  { key: "_no",    label: "#",           className: "row-num", render: (_, __, i) => i + 1 },
  { key: "code",   label: "Charge Code", isRequired: true,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
  { key: "desc",   label: "Description",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",    label: "Qty", className: "is-num",
    render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "unit",   label: "Unit",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "amount", label: "Amount",      className: "is-num", isRequired: true,
    render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "cur",    label: "Currency",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "_rem",   label: "Remark",      render: () => <input className="grid__cell-input" /> },
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

const ACCOUNT_ROWS: AccountRow[] = [];

const EMPTY_FREIGHT_ROW = { code: "", desc: "", qty: "", unit: "", amount: "", cur: "" };

// ── Rate input styles ──────────────────────────────────────
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
        <input type="date" defaultValue="" style={DATE_INPUT} />
        <select style={CUR_SELECT}><option>USD</option><option>KRW</option><option>EUR</option></select>
        <input type="number" min="0" step="0.01" defaultValue="" style={RATE_INPUT} />
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
  const { control } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "freightSelling" });
  const [selectedKey, setSelectedKey] = useState<number | null>(null);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function handleAdd() {
    const nextId = fields.length > 0 ? Math.max(...fields.map(f => f.id)) + 1 : 1;
    append({ ...EMPTY_FREIGHT_ROW, id: nextId });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const targetIdx = selectedKey !== null && selectedIdx !== -1 ? selectedIdx : fields.length - 1;
    if (window.confirm("삭제하시겠습니까?")) {
      remove(targetIdx);
      setSelectedKey(null);
    }
  }

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Selling / Debit</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={SELLING_COLS}
        data={fields as unknown as FreightRow[]}
        rowKey={(row) => row.id}
        onRowClick={(row) => setSelectedKey(row.id === selectedKey ? null : row.id)}
        rowClassName={(row) => row.id === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1 }}
      />
    </div>
  );
}

// ── Buying Panel ───────────────────────────────────────────
export function FreightBuyingPanel() {
  const { control } = useFormContext<HouseBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "freightBuying" });
  const [selectedKey, setSelectedKey] = useState<number | null>(null);

  const selectedIdx = selectedKey !== null
    ? fields.findIndex(f => f.id === selectedKey)
    : -1;

  function handleAdd() {
    const nextId = fields.length > 0 ? Math.max(...fields.map(f => f.id)) + 1 : 1;
    append({ ...EMPTY_FREIGHT_ROW, id: nextId });
    setSelectedKey(null);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const targetIdx = selectedKey !== null && selectedIdx !== -1 ? selectedIdx : fields.length - 1;
    if (window.confirm("삭제하시겠습니까?")) {
      remove(targetIdx);
      setSelectedKey(null);
    }
  }

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Buying / Credit</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button type="button" className="btn btn--sm" onClick={handleAdd}><Plus size={12} /></button>
          <button type="button" className="btn btn--sm" onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></button>
        </div>
      </div>
      <GridList
        columns={BUYING_COLS}
        data={fields as unknown as FreightRow[]}
        rowKey={(row) => row.id}
        onRowClick={(row) => setSelectedKey(row.id === selectedKey ? null : row.id)}
        rowClassName={(row) => row.id === selectedKey ? "is-selected" : undefined}
        style={{ flex: 1 }}
      />
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
