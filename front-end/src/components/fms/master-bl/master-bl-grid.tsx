"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const ROWS = [
  { mbl: "COSCO2404195",  isSea: true,  ref: "MR-2026-04195", bkg: "BKG-COSCO-0412", vessel: "COSCO EXCELLENCE", etd: "04/24", eta: "05/08", pol: "KRBSAN", pod: "CNSHA", houses: 2 },
  { mbl: "HAPAG0418011",  isSea: true,  ref: "MR-2026-04180", bkg: "BKG-HAPAG-0419", vessel: "HAPAG EXPRESS",    etd: "04/23", eta: "05/12", pol: "KRICN",  pod: "DEHAM",  houses: 1 },
  { mbl: "180-12345678",  isSea: false, ref: "MR-2026-04195", bkg: "",                vessel: "KE851",            etd: "04/24", eta: "04/24", pol: "ICN",    pod: "PVG",    houses: 2 },
  { mbl: "176-87654321",  isSea: false, ref: "MR-2026-04180", bkg: "",                vessel: "OZ741",            etd: "04/23", eta: "04/23", pol: "GMP",    pod: "NRT",    houses: 1 },
];

interface Props { variantKey: string; isSea: boolean }

export function MasterBlGrid({ variantKey, isSea }: Props) {
  const router = useRouter();
  const [selected, setSelected] = useState<number | null>(null);
  const rows = ROWS.filter((r) => r.isSea === isSea);

  function handleMblDoubleClick() {
    router.push(`/fms/master-bl/${variantKey}/new`);
  }

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Master B/L</span>
        <span className="panel__rowcount">{rows.length}</span>
      </div>
      <div className="list-wrap">
        <table className="grid--list">
          <thead>
            <tr>
              <th className="row-num">#</th>
              <th title="더블클릭 → Entry 이동">{isSea ? "MBL No" : "MAWB No"}</th>
              <th>Master Ref</th>
              {isSea && <th>Line Bkg. No</th>}
              <th>Vessel / Flight</th>
              <th>ETD</th><th>ETA</th>
              <th>POL</th><th>POD</th>
              <th className="is-num">Houses</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r, i) => (
              <tr
                key={i}
                className={selected === i ? "is-selected" : ""}
                onClick={() => setSelected(i)}
              >
                <td className="row-num">{i + 1}</td>
                <td
                  className="cell-hbl"
                  onDoubleClick={handleMblDoubleClick}
                  style={{ cursor: "pointer" }}
                  title="더블클릭하여 Entry 열기"
                >
                  {r.mbl}
                </td>
                <td className="cell-mono">{r.ref}</td>
                {isSea && <td className="cell-mono">{r.bkg}</td>}
                <td>{r.vessel}</td>
                <td className="cell-mono">{r.etd}</td>
                <td className="cell-mono">{r.eta}</td>
                <td className="cell-mono">{r.pol}</td>
                <td className="cell-mono">{r.pod}</td>
                <td className="is-num cell-mono">{r.houses}</td>
                <td><span className="pill pill--ok">Confirmed</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
