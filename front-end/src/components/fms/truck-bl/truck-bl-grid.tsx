"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { GridList, GridColumn } from "@/components/shared/grid-list";

const ROWS = [
  { tbl: "TRUCK-2026-04-001", status: "배차완료", cust: "한진무역(주)", shipper: "한진무역(주)", cons: "서울물류센터", etd: "04/24", eta: "04/25", pol: "KRBSAN", pod: "KRSEL", pickup: "04/23", trucker: "(주)부산트럭", op: "KYS" },
  { tbl: "TRUCK-2026-04-002", status: "운송중",   cust: "LG전자(주)",  shipper: "LG전자(주)",  cons: "수원창고",    etd: "04/23", eta: "04/23", pol: "KRICN", pod: "KRSWN", pickup: "04/23", trucker: "경기운수",   op: "PKH" },
  { tbl: "TRUCK-2026-04-003", status: "접수",     cust: "삼성전자",    shipper: "삼성전자",    cons: "평택항",      etd: "04/25", eta: "04/25", pol: "KRSEL", pod: "KRPTK", pickup: "04/25", trucker: "",          op: "LJY" },
];

type TruckBlRow = typeof ROWS[number];

const STATUS_PILL: Record<string, string> = {
  "배차완료": "pill--sent",
  "운송중":   "pill--ok",
  "접수":     "pill--draft",
};

export function TruckBlGrid() {
  const router = useRouter();
  const [selected, setSelected] = useState<string | null>(null);

  const columns: GridColumn<TruckBlRow>[] = [
    {
      key: "tbl",
      label: "Truck B/L No.",
      minWidth: 160,
      render: (value) => (
        <span
          className="cell-hbl"
          onDoubleClick={() => router.push("/fms/truck-bl/entry")}
          style={{ cursor: "pointer" }}
          title="더블클릭하여 Entry 열기"
        >
          {String(value ?? "")}
        </span>
      ),
    },
    {
      key: "status",
      label: "Status",
      minWidth: 80,
      render: (v) => {
        const s = String(v ?? "");
        return <span className={`pill ${STATUS_PILL[s] ?? ""}`}>{s}</span>;
      },
    },
    { key: "cust",    label: "Actual Customer", minWidth: 130 },
    { key: "shipper", label: "Shipper",         minWidth: 130 },
    { key: "cons",    label: "Consignee",       minWidth: 120 },
    { key: "etd",     label: "ETD",    minWidth: 60, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "eta",     label: "ETA",    minWidth: 60, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "pol",     label: "POL",    minWidth: 70, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "pod",     label: "POD",    minWidth: 70, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "pickup",  label: "Pick-up Date", minWidth: 90, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "trucker", label: "Trucker",      minWidth: 110 },
    { key: "op",      label: "Operator",     minWidth: 70, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
  ];

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Truck B/L</span>
        <span className="panel__rowcount">{ROWS.length}</span>
      </div>
      <div className="list-wrap">
        <GridList<TruckBlRow>
          columns={columns}
          data={ROWS}
          onRowClick={(row) => setSelected(row.tbl)}
          rowKey={(row) => row.tbl}
          rowClassName={(row) => (selected === row.tbl ? "is-selected" : undefined)}
        />
      </div>
    </div>
  );
}
