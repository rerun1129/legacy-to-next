export function OtherTab() {
  return (
    <div className="page-body layout-other" style={{ overflow: "auto", gap: 10 }}>
      {/* Reference Numbers */}
      <div>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Reference Numbers</span>
          </div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            <div className="sched-list">
              {[
                { label: "PO No",         value: "PO-2026-04156" },
                { label: "Invoice No",    value: "INV-20260415" },
                { label: "Contract No",   value: "" },
                { label: "L/C No",        value: "" },
                { label: "Customer Ref",  value: "CR-HJ-2604" },
                { label: "Booking Ref",   value: "BK-COSCO-0412" },
              ].map((f) => (
                <div key={f.label} className="li">
                  <span className="li__label">{f.label}</span>
                  <div className="li__input">
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} />
                  </div>
                </div>
              ))}
            </div>

            <div className="subhead" style={{ marginTop: 12 }}><div className="subhead__bar" />Additional Info</div>
            <div className="sched-list">
              {[
                { label: "Inco Place",    value: "BUSAN PORT" },
                { label: "Payment Term",  value: "T/T 30 DAYS" },
                { label: "Country Origin",value: "KR" },
                { label: "Country Dest",  value: "CN" },
              ].map((f) => (
                <div key={f.label} className="li">
                  <span className="li__label">{f.label}</span>
                  <div className="li__input">
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10, background: "var(--surface-1)", border: "1px solid var(--border)", borderRadius: 4, color: "var(--ink)", outline: "none" }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Co-Load B/L list */}
      <div>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Co-Load B/L</span>
            <span className="panel__rowcount">0</span>
            <div className="panel__actions">
              <button className="btn btn--sm">+ Add</button>
            </div>
          </div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <table className="grid">
              <thead>
                <tr>
                  <th className="row-num">#</th>
                  <th>HBL No</th>
                  <th>Shipper</th>
                  <th>Consignee</th>
                  <th className="is-num">Pkg</th>
                  <th className="is-num">G/W</th>
                  <th className="is-num">CBM</th>
                  <th>Remark</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td colSpan={8} className="grid__empty">No co-load B/L entries</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
