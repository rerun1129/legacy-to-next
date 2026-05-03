import { getModeLabels } from "@/lib/bl-mode-labels";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

interface Props { variant?: AnyVariantConfig }

const ROWS: { no: number; hbl: string; shipper: string; consignee: string; doc: string; pkg: number; unit: string; gw: number; cbm: number; }[] = [];

export function MasterHouseBLGrid({ variant }: Props) {
  if (!variant) return null;
  const isSea    = variant.mode === "SEA";
  const ml       = getModeLabels(variant.mode);
  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{ml.hblList}</span>
        <span className="panel__rowcount">{ROWS.length}</span>
        <div className="panel__actions">
          <button className="btn btn--sm">+ {ml.newHbl}</button>
          <button className="btn btn--sm">House Consol</button>
        </div>
      </div>
      <div style={{ overflow: "auto", flex: 1 }}>
        <table className="grid--list">
          <thead>
            <tr>
              <th className="row-num">#</th>
              <th>{ml.hblNo}</th>
              <th>Shipper</th><th>Consignee</th><th>DOC Partner</th>
              <th className="is-num">Package</th><th>Unit</th>
              <th className="is-num">G/W</th><th className="is-num">CBM</th>
              {isSea && <><th>ETD</th><th>POL</th></>}
            </tr>
          </thead>
          <tbody>
            {ROWS.map(r => (
              <tr key={r.no}>
                <td className="row-num">{r.no}</td>
                <td className="cell-hbl">{r.hbl}</td>
                <td>{r.shipper}</td><td>{r.consignee}</td><td className="cell-mono">{r.doc}</td>
                <td className="is-num cell-mono">{r.pkg}</td><td>{r.unit}</td>
                <td className="is-num cell-mono">{r.gw}</td><td className="is-num cell-mono">{r.cbm}</td>
                {isSea && <><td className="cell-mono">04/24</td><td className="cell-mono">KRBSAN</td></>}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
