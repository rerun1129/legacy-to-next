import { Search } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface RateRow {
  code: string; desc: string; qty: string; unit: string;
  sell: string; buy: string; cur: string;
}

type AccountRow = Record<string, never>;

const SELLING_COLS: GridColumn<RateRow>[] = [
  { key: "_no",    label: "#",          className: "row-num",
    render: (_, __, i) => i + 1 },
  { key: "code",   label: "Charge Code", required: true,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
  { key: "desc",   label: "Description",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",    label: "Qty / Basis",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "unit",   label: "Unit",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "sell",   label: "Rate",       className: "is-num", required: true,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "_amt",   label: "Amount",     className: "is-num",
    render: (_, row) => <input className="grid__cell-input" defaultValue={(parseFloat(row.sell) * 2).toFixed(2)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "cur",    label: "Currency",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "_krw",   label: "KRW Equiv.", className: "is-num",
    render: (_, row) => <input className="grid__cell-input" defaultValue={(parseFloat(row.sell) * 2 * 1376.5).toFixed(0)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "_rem",   label: "Remark",
    render: () => <input className="grid__cell-input" /> },
];

const BUYING_COLS: GridColumn<RateRow>[] = [
  { key: "_no",    label: "#",          className: "row-num",
    render: (_, __, i) => i + 1 },
  { key: "code",   label: "Charge Code", required: true,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /> },
  { key: "desc",   label: "Description",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",    label: "Qty / Basis",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "unit",   label: "Unit",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "buy",    label: "Rate",       className: "is-num", required: true,
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "_amt",   label: "Amount",     className: "is-num",
    render: (_, row) => <input className="grid__cell-input" defaultValue={(parseFloat(row.buy) * 2).toFixed(2)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "cur",    label: "Currency",
    render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "_krw",   label: "KRW Equiv.", className: "is-num",
    render: (_, row) => <input className="grid__cell-input" defaultValue={(parseFloat(row.buy) * 2 * 1376.5).toFixed(0)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /> },
  { key: "_rem",   label: "Remark",
    render: () => <input className="grid__cell-input" /> },
];

const ACCOUNT_COLS: GridColumn<AccountRow>[] = [
  { key: "docType",   label: "Doc Type" },
  { key: "docNo",     label: "Doc No" },
  { key: "issueDate", label: "Issue Date" },
  { key: "amount",    label: "Amount",   className: "is-num" },
  { key: "currency",  label: "Currency" },
  { key: "status",    label: "Status" },
];

const rateRows: RateRow[] = [
  { code: "OFR", desc: "Ocean Freight",     qty: "2 CONT", unit: "CONT", sell: "400.00", buy: "320.00", cur: "USD" },
  { code: "BAF", desc: "Bunker Adjustment", qty: "2 CONT", unit: "CONT", sell: "120.00", buy: "100.00", cur: "USD" },
  { code: "CAF", desc: "Currency Adj.",     qty: "2 CONT", unit: "CONT", sell: "50.00",  buy: "40.00",  cur: "USD" },
  { code: "LSF", desc: "Low Sulphur Fee",   qty: "2 CONT", unit: "CONT", sell: "80.00",  buy: "65.00",  cur: "USD" },
];

export function FreightTab() {
  return (
    <div className="page-body layout-freight">
      {/* Rate headers: Actual Customer / Liner / Settle Partner */}
      <div>
        <div className="rate-head">
          {[
            { title: "ACTUAL CUSTOMER",  label: "Customer",   code: "HJTR001", name: "한진무역(주)" },
            { title: "LINER",            label: "Liner",      code: "COSCO",   name: "COSCO SHIPPING" },
            { title: "SETTLE PARTNER",   label: "Settle",     code: "HJTR001", name: "한진무역(주)" },
          ].map((col) => (
            <div key={col.title} className="rate-head__col">
              <div className="rate-head__group">{col.title}</div>
              <div className="rate-head__row">
                <span className="rate-head__label is-required">{col.label}</span>
                <div style={{ position: "relative", display: "flex", alignItems: "center", flex: 1 }}>
                  <input
                    defaultValue={col.code}
                    style={{ height: 22, padding: "0 24px 0 8px", fontSize: 10, background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)", outline: "none", width: 90, flexShrink: 0 }}
                  />
                  <Search size={12} style={{ position: "absolute", right: "calc(100% - 82px)", color: "var(--ink-3)" }} />
                  <input
                    defaultValue={col.name}
                    style={{ height: 22, padding: "0 8px", fontSize: 10, background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none", flex: 1, marginLeft: 6, minWidth: 0 }}
                  />
                </div>
              </div>
              <div className="rate-head__row" style={{ gap: 6, fontSize: 10 }}>
                <span style={{ color: "var(--ink-3)", fontSize: 10, whiteSpace: "nowrap" }}>Cur</span>
                <select style={{ height: 22, padding: "0 6px", fontSize: 10, background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none", width: 55 }}>
                  <option>USD</option><option>KRW</option><option>EUR</option>
                </select>
                <span style={{ color: "var(--ink-3)", fontSize: 10, whiteSpace: "nowrap" }}>Ex Rate</span>
                <input defaultValue="1,376.50" style={{ height: 22, width: 75, padding: "0 6px", fontSize: 10, background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)", textAlign: "right", outline: "none" }} />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Selling (Debit) */}
      <div>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Selling / Debit</span>
            <span className="panel__rowcount">{rateRows.length}</span>
            <div className="panel__actions">
              <button className="btn btn--sm">+ Add</button>
            </div>
          </div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <GridList columns={SELLING_COLS} data={rateRows} rowKey={(_, i) => i} />
          </div>
          <div className="grid-foot">
            <div className="grid-foot__spacer" />
            <span>Total Sell: <strong className="grid-foot__total">USD 1,300.00</strong></span>
            <span>≈ <strong className="grid-foot__total">₩ 1,789,450</strong></span>
          </div>
        </div>
      </div>

      {/* Buying (Credit) */}
      <div>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Buying / Credit</span>
            <span className="panel__rowcount">{rateRows.length}</span>
            <div className="panel__actions">
              <button className="btn btn--sm">+ Add</button>
            </div>
          </div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <GridList columns={BUYING_COLS} data={rateRows} rowKey={(_, i) => i} />
          </div>
          <div className="grid-foot">
            <div className="grid-foot__spacer" />
            <span>Total Buy: <strong className="grid-foot__total">USD 1,050.00</strong></span>
            <span>Margin: <strong className="grid-foot__total" style={{ color: "var(--success)" }}>USD 250.00</strong></span>
            <span>≈ <strong className="grid-foot__total" style={{ color: "var(--success)" }}>₩ 344,125</strong></span>
          </div>
        </div>
      </div>

      {/* Account Documents */}
      <div>
        <div className="panel">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Account Documents</span>
            <span className="panel__rowcount">0</span>
          </div>
          <div className="panel__body--flush">
            <GridList
              columns={ACCOUNT_COLS}
              data={[]}
              emptyMessage="No account documents"
            />
          </div>
        </div>
      </div>
    </div>
  );
}
