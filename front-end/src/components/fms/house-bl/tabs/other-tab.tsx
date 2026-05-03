import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface CoLoadRow {
  id: number;
  hblNo: string; shipper: string; consignee: string; pkg: string; gw: string; cbm: string; remark: string;
}

const CO_LOAD_ROWS: CoLoadRow[] = [];

const CO_LOAD_COLS: GridColumn<CoLoadRow>[] = [
  { key: "_no",       label: "#",         className: "row-num", render: (_, __, i) => i + 1 },
  { key: "hblNo",     label: "HBL No" },
  { key: "shipper",   label: "Shipper" },
  { key: "consignee", label: "Consignee" },
  { key: "pkg",       label: "Pkg",       className: "is-num" },
  { key: "gw",        label: "G/W",       className: "is-num" },
  { key: "cbm",       label: "CBM",       className: "is-num" },
  { key: "remark",    label: "Remark" },
];

export function OtherTab() {
  return (
    <div style={{ flex: 1, overflow: "hidden", padding: "12px 16px" }}>
      <div style={{ display: "flex", gap: 10, height: "100%" }}>
        <div style={{ flex: 1, minWidth: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        <div className="panel panel--full">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Reference Numbers</span>
          </div>
          <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
            <div className="sched-list">
              {[
                { label: "PO No",         value: "" },
                { label: "Invoice No",    value: "" },
                { label: "Contract No",   value: "" },
                { label: "L/C No",        value: "" },
                { label: "Customer Ref",  value: "" },
                { label: "Booking Ref",   value: "" },
              ].map((f) => (
                <div key={f.label} className="li">
                  <span className="li__label">{f.label}</span>
                  <div className="li__input">
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
                  </div>
                </div>
              ))}
            </div>

            <div className="subhead" style={{ marginTop: 12 }}><div className="subhead__bar" />Additional Info</div>
            <div className="sched-list">
              {[
                { label: "Inco Place",    value: "" },
                { label: "Payment Term",  value: "" },
                { label: "Country Origin",value: "" },
                { label: "Country Dest",  value: "" },
              ].map((f) => (
                <div key={f.label} className="li">
                  <span className="li__label">{f.label}</span>
                  <div className="li__input">
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

        <div style={{ flex: 1, minWidth: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <div className="panel">
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Co-Load B/L</span>
            <span className="panel__rowcount">{CO_LOAD_ROWS.length}</span>
            <div className="panel__actions">
              <button className="btn btn--sm">+</button>
            </div>
          </div>
          <div className="grid-wrap" style={{ flex: 1, overflow: "auto" }}>
            <GridList
              columns={CO_LOAD_COLS}
              data={CO_LOAD_ROWS}
              rowKey={(row) => row.id}
            />
          </div>
        </div>
        </div>{/* /Co-Load B/L */}
      </div>{/* /row */}
    </div>
  );
}
