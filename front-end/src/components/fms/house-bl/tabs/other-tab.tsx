import { GridList, type GridColumn } from "@/components/shared/grid-list";
// NOTE: Reference Numbers / Additional Info 필드는 house-bl-schema 미포함 — 추후 스키마 확장 시 register 전환

interface CoLoadRow {
  id: number;
  hblNo: string; shipper: string; consignee: string; pkg: string; gw: string; cbm: string; remark: string;
}

const CO_LOAD_ROWS: CoLoadRow[] = [
  { id: 1, hblNo: "HBLKR24041956", shipper: "한진무역(주)",     consignee: "SHANGHAI TRADING CO.",  pkg: "500 CTN", gw: "12,400", cbm: "22.5", remark: ""           },
  { id: 2, hblNo: "HBLKR24041901", shipper: "삼성전자(주)",     consignee: "SAMSUNG EUROPE GmbH",   pkg: "800 CTN", gw: "18,200", cbm: "65.0", remark: ""           },
  { id: 3, hblNo: "HBLKR24041877", shipper: "현대상사(주)",     consignee: "HYUNDAI TRADING USA",   pkg: "300 CTN", gw: "7,500",  cbm: "30.0", remark: ""           },
  { id: 4, hblNo: "HBLKR24041823", shipper: "엘지전자(주)",     consignee: "LG ELECTRONICS INC.",  pkg: "420 CTN", gw: "9,800",  cbm: "40.5", remark: ""           },
  { id: 5, hblNo: "HBLKR24041800", shipper: "코오롱인더스트리", consignee: "KOLON GLOBAL CORP.",    pkg: "250 CTN", gw: "5,200",  cbm: "18.0", remark: "FCL 전환 예정" },
  { id: 6, hblNo: "HBLKR24041756", shipper: "SK하이닉스(주)",  consignee: "SK HYNIX INC.",        pkg: "180 CTN", gw: "4,500",  cbm: "15.0", remark: ""           },
];

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
                    <input defaultValue={f.value} placeholder={f.label} style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} />
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
