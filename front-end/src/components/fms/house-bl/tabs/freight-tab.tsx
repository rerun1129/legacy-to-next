import { Search } from "lucide-react";

const rateRows = [
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
                    style={{ height: 26, padding: "0 24px 0 8px", fontSize: 12, background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)", outline: "none", width: 90, flexShrink: 0 }}
                  />
                  <Search size={12} style={{ position: "absolute", right: "calc(100% - 82px)", color: "var(--ink-3)" }} />
                  <input
                    defaultValue={col.name}
                    style={{ height: 26, padding: "0 8px", fontSize: 12, background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none", flex: 1, marginLeft: 6, minWidth: 0 }}
                  />
                </div>
              </div>
              <div className="rate-head__row" style={{ gap: 6, fontSize: 11 }}>
                <span style={{ color: "var(--ink-3)", fontSize: 11, whiteSpace: "nowrap" }}>Cur</span>
                <select style={{ height: 26, padding: "0 6px", fontSize: 11, background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none", width: 55 }}>
                  <option>USD</option><option>KRW</option><option>EUR</option>
                </select>
                <span style={{ color: "var(--ink-3)", fontSize: 11, whiteSpace: "nowrap" }}>Ex Rate</span>
                <input defaultValue="1,376.50" style={{ height: 26, width: 75, padding: "0 6px", fontSize: 11, background: "var(--surface-0)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", fontFamily: "var(--font-mono)", textAlign: "right", outline: "none" }} />
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
            <table className="grid">
              <thead>
                <tr>
                  <th className="row-num">#</th>
                  <th className="is-required">Charge Code</th>
                  <th>Description</th>
                  <th>Qty / Basis</th>
                  <th>Unit</th>
                  <th className="is-num is-required">Rate</th>
                  <th className="is-num">Amount</th>
                  <th>Currency</th>
                  <th className="is-num">KRW Equiv.</th>
                  <th>Remark</th>
                </tr>
              </thead>
              <tbody>
                {rateRows.map((r, i) => (
                  <tr key={i}>
                    <td className="row-num">{i + 1}</td>
                    <td><input className="grid__cell-input" defaultValue={r.code} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.desc} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.qty} style={{ fontFamily: "var(--font-mono)" }} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.unit} /></td>
                    <td className="is-num"><input className="grid__cell-input" defaultValue={r.sell} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td className="is-num"><input className="grid__cell-input" defaultValue={(parseFloat(r.sell) * 2).toFixed(2)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.cur} style={{ fontFamily: "var(--font-mono)" }} /></td>
                    <td className="is-num"><input className="grid__cell-input" defaultValue={(parseFloat(r.sell) * 2 * 1376.5).toFixed(0)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td><input className="grid__cell-input" /></td>
                  </tr>
                ))}
              </tbody>
            </table>
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
            <table className="grid">
              <thead>
                <tr>
                  <th className="row-num">#</th>
                  <th className="is-required">Charge Code</th>
                  <th>Description</th>
                  <th>Qty / Basis</th>
                  <th>Unit</th>
                  <th className="is-num is-required">Rate</th>
                  <th className="is-num">Amount</th>
                  <th>Currency</th>
                  <th className="is-num">KRW Equiv.</th>
                  <th>Remark</th>
                </tr>
              </thead>
              <tbody>
                {rateRows.map((r, i) => (
                  <tr key={i}>
                    <td className="row-num">{i + 1}</td>
                    <td><input className="grid__cell-input" defaultValue={r.code} style={{ fontFamily: "var(--font-mono)", fontWeight: 600, color: "var(--accent-ink)" }} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.desc} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.qty} style={{ fontFamily: "var(--font-mono)" }} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.unit} /></td>
                    <td className="is-num"><input className="grid__cell-input" defaultValue={r.buy} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td className="is-num"><input className="grid__cell-input" defaultValue={(parseFloat(r.buy) * 2).toFixed(2)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td><input className="grid__cell-input" defaultValue={r.cur} style={{ fontFamily: "var(--font-mono)" }} /></td>
                    <td className="is-num"><input className="grid__cell-input" defaultValue={(parseFloat(r.buy) * 2 * 1376.5).toFixed(0)} style={{ textAlign: "right", fontFamily: "var(--font-mono)" }} /></td>
                    <td><input className="grid__cell-input" /></td>
                  </tr>
                ))}
              </tbody>
            </table>
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
            <table className="grid">
              <thead>
                <tr>
                  <th>Doc Type</th>
                  <th>Doc No</th>
                  <th>Issue Date</th>
                  <th className="is-num">Amount</th>
                  <th>Currency</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr><td colSpan={6} className="grid__empty">No account documents</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
