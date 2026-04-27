import { getModeLabels } from "@/lib/bl-mode-labels";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";

interface Props { variant?: AnyVariantConfig }

const ROWS = [
  { no: 1, hbl: "HBLKR24041956", shipper: "한진무역(주)",     consignee: "SHANGHAI TRADING CO.", doc: "HJTR001", pkg: 500, unit: "CTN", gw: "12,400", cbm: "22.5" },
  { no: 2, hbl: "HBLKR24041901", shipper: "삼성전자(주)",     consignee: "SAMSUNG EUROPE GmbH",  doc: "SEHQ001", pkg: 800, unit: "CTN", gw: "18,200", cbm: "65.0" },
  { no: 3, hbl: "HBLKR24041877", shipper: "현대상사(주)",     consignee: "HYUNDAI TRADING USA",  doc: "HTS001",  pkg: 300, unit: "CTN", gw: "7,500",  cbm: "30.0" },
  { no: 4, hbl: "HBLKR24041823", shipper: "엘지전자(주)",     consignee: "LG ELECTRONICS INC.", doc: "LGEL001", pkg: 420, unit: "CTN", gw: "9,800",  cbm: "40.5" },
  { no: 5, hbl: "HBLKR24041800", shipper: "코오롱인더스트리", consignee: "KOLON GLOBAL CORP.",   doc: "KGC001",  pkg: 250, unit: "CTN", gw: "5,200",  cbm: "18.0" },
  { no: 6, hbl: "HBLKR24041756", shipper: "SK하이닉스(주)",  consignee: "SK HYNIX INC.",        doc: "SKH001",  pkg: 180, unit: "CTN", gw: "4,500",  cbm: "15.0" },
];

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
