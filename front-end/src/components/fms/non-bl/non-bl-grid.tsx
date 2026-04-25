"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const ROWS = [
  { nbl: "NBL-2026-04-001", div: "Sea",       status: "처리중", cust: "한진무역(주)", sh: "한진무역(주)", cn: "SHANGHAI CO.",    etd: "04/24", eta: "05/08", pol: "KRBSAN", pod: "CNSHA", ref: "HBLKR24041956", op: "KYS" },
  { nbl: "NBL-2026-04-002", div: "Air",       status: "완료",   cust: "LG전자(주)",  sh: "LG전자(주)",  cn: "LG JAPAN K.K.", etd: "04/22", eta: "04/23", pol: "ICN",    pod: "NRT",   ref: "HAWBKR24041002", op: "PKH" },
  { nbl: "NBL-2026-04-003", div: "Warehouse", status: "접수",   cust: "삼성전자",    sh: "-",          cn: "-",             etd: "04/20", eta: "04/20", pol: "KRICN",  pod: "KRICN", ref: "",               op: "LJY" },
  { nbl: "NBL-2026-04-004", div: "Trucking",  status: "접수",   cust: "포스코",      sh: "포스코",      cn: "평택항",         etd: "04/25", eta: "04/25", pol: "KRSEL",  pod: "KRPTK", ref: "",               op: "KYS" },
];

const statusPill: Record<string, string> = {
  "완료":   "pill--ok",
  "처리중": "pill--sent",
  "접수":   "pill--draft",
};

export function NonBlGrid() {
  const router = useRouter();
  const [selected, setSelected] = useState<number | null>(null);

  function handleDoubleClick() {
    router.push("/fms/non-bl/new");
  }

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Non B/L</span>
        <span className="panel__rowcount">{ROWS.length}</span>
      </div>
      <div className="list-wrap">
        <table className="grid--list">
          <thead>
            <tr>
              <th className="row-num">#</th>
              <th title="더블클릭 → Entry 이동">Non B/L No.</th>
              <th>Work Div.</th><th>Status</th>
              <th>Actual Customer</th><th>Shipper</th><th>Consignee</th>
              <th>ETD</th><th>ETA</th><th>POL</th><th>POD</th>
              <th>원본 B/L Ref</th><th>Operator</th>
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
                  {r.nbl}
                </td>
                <td><span className="chip chip--accent">{r.div}</span></td>
                <td><span className={`pill ${statusPill[r.status]}`}>{r.status}</span></td>
                <td>{r.cust}</td><td>{r.sh}</td><td>{r.cn}</td>
                <td className="cell-mono">{r.etd}</td><td className="cell-mono">{r.eta}</td>
                <td className="cell-mono">{r.pol}</td><td className="cell-mono">{r.pod}</td>
                <td className="cell-mono cell-dim">{r.ref}</td>
                <td className="cell-mono">{r.op}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
