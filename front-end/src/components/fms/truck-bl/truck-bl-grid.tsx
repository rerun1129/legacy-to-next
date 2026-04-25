"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const ROWS = [
  { tbl: "TRUCK-2026-04-001", status: "배차완료", cust: "한진무역(주)", shipper: "한진무역(주)", cons: "서울물류센터", etd: "04/24", eta: "04/25", pol: "KRBSAN", pod: "KRSEL", pickup: "04/23", trucker: "(주)부산트럭", op: "KYS" },
  { tbl: "TRUCK-2026-04-002", status: "운송중",   cust: "LG전자(주)",  shipper: "LG전자(주)",  cons: "수원창고",    etd: "04/23", eta: "04/23", pol: "KRICN", pod: "KRSWN", pickup: "04/23", trucker: "경기운수",   op: "PKH" },
  { tbl: "TRUCK-2026-04-003", status: "접수",     cust: "삼성전자",    shipper: "삼성전자",    cons: "평택항",      etd: "04/25", eta: "04/25", pol: "KRSEL", pod: "KRPTK", pickup: "04/25", trucker: "",          op: "LJY" },
];

const statusPill: Record<string, string> = {
  "배차완료": "pill--sent",
  "운송중":   "pill--ok",
  "접수":     "pill--draft",
};

export function TruckBlGrid() {
  const router = useRouter();
  const [selected, setSelected] = useState<number | null>(null);

  function handleDoubleClick() {
    router.push("/fms/truck-bl/new");
  }

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Truck B/L</span>
        <span className="panel__rowcount">{ROWS.length}</span>
      </div>
      <div className="list-wrap">
        <table className="grid--list">
          <thead>
            <tr>
              <th className="row-num">#</th>
              <th title="더블클릭 → Entry 이동">Truck B/L No.</th>
              <th>Status</th>
              <th>Actual Customer</th><th>Shipper</th><th>Consignee</th>
              <th>ETD</th><th>ETA</th><th>POL</th><th>POD</th>
              <th>Pick-up Date</th><th>Trucker</th>
              <th>Operator</th>
            </tr>
          </thead>
          <tbody>
            {ROWS.map((r, i) => (
              <tr
                key={i}
                className={selected === i ? "is-selected" : ""}
                onClick={() => setSelected(i)}
              >
                <td className="row-num">{i + 1}</td>
                <td
                  className="cell-hbl"
                  onDoubleClick={handleDoubleClick}
                  style={{ cursor: "pointer" }}
                  title="더블클릭하여 Entry 열기"
                >
                  {r.tbl}
                </td>
                <td><span className={`pill ${statusPill[r.status]}`}>{r.status}</span></td>
                <td>{r.cust}</td><td>{r.shipper}</td><td>{r.cons}</td>
                <td className="cell-mono">{r.etd}</td><td className="cell-mono">{r.eta}</td>
                <td className="cell-mono">{r.pol}</td><td className="cell-mono">{r.pod}</td>
                <td className="cell-mono">{r.pickup}</td><td>{r.trucker}</td>
                <td className="cell-mono">{r.op}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
