"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { houseBLRows } from "@/adapter/out/mock/mock-data";

const statusClass: Record<string, string> = {
  ok:     "pill--ok",
  inprog: "pill--draft",
  draft:  "pill--hold",
};
const statusLabel: Record<string, string> = {
  ok:     "Confirmed",
  inprog: "In Progress",
  draft:  "Draft",
};

interface Props { variantKey: string }

export function ListGrid({ variantKey }: Props) {
  const router   = useRouter();
  const [selected, setSelected] = useState<number | null>(null);

  function handleHblDoubleClick(hbl: string) {
    // TODO: 실제 ID 기반 라우팅으로 교체 (현재는 /new로 이동)
    router.push(`/fms/house-bl/${variantKey}/new`);
  }

  return (
    <div className="panel panel--list" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title">
          <div className="panel__title-accent" />
          House B/L
          <span className="panel__rowcount">{houseBLRows.length}</span>
        </div>
        <div className="panel__actions">
          <div className="seg-view">
            <button className="is-active">Cozy</button>
            <button>Compact</button>
          </div>
        </div>
      </div>

      <div className="panel__body panel__body--flush" style={{ flex: 1, minHeight: 0, display: "flex" }}>
        <div className="list-wrap">
          <table className="grid--list">
            <thead>
              <tr>
                <th className="row-num" style={{ width: 38 }}>#</th>
                <th title="더블클릭 → Entry 이동">HBL No</th>
                <th>Exp/Imp</th>
                <th>Doc Status</th>
                <th>MBL No</th>
                <th>Type</th>
                <th>Load</th>
                <th>ETD</th>
                <th>ETA</th>
                <th>Reg. Date</th>
                <th>POL</th>
                <th>POD</th>
                <th>Vessel</th>
                <th>Voyage</th>
                <th>Shipper</th>
                <th>Consignee</th>
              </tr>
            </thead>
            <tbody>
              {houseBLRows.map((row) => (
                <tr
                  key={row.no}
                  className={selected === row.no ? "is-selected" : ""}
                  onClick={() => setSelected(row.no)}
                >
                  <td className="row-num">{row.no}</td>
                  <td
                    className="cell-hbl"
                    onDoubleClick={() => handleHblDoubleClick(row.hbl)}
                    style={{ cursor: "pointer" }}
                    title="더블클릭하여 Entry 열기"
                  >
                    {row.hbl}
                  </td>
                  <td>
                    <span className={`chip${row.expImp === "EXP" ? " chip--accent" : ""}`}>{row.expImp}</span>
                  </td>
                  <td>
                    <span className={`pill ${statusClass[row.docStatus]}`}>
                      {statusLabel[row.docStatus]}
                    </span>
                  </td>
                  <td className="cell-mono">{row.mbl}</td>
                  <td>{row.sType}</td>
                  <td className="cell-dim">{row.lType}</td>
                  <td className="cell-mono">{row.etd}</td>
                  <td className="cell-mono">{row.eta}</td>
                  <td className="cell-mono">{row.regDate}</td>
                  <td><span className="port__code">{row.pol}</span></td>
                  <td><span className="port__code">{row.pod}</span></td>
                  <td>{row.vessel}</td>
                  <td className="cell-mono">{row.voyage}</td>
                  <td>{row.shipper}</td>
                  <td>{row.consignee}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
