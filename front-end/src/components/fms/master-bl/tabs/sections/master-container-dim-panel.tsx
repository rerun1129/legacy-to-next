import { getModeLabels } from "@/lib/bl-mode-labels";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

interface Props { variant: MasterVariantConfig }

const SEA_ROWS = [
  { cno: "CSNU1234567", type: "20GP", seal: "SL123456", pkg: 500,  unit: "CTN", gw: "12,400", cbm: "22.5" },
  { cno: "TCKU9876543", type: "40HC", seal: "SL789012", pkg: 800,  unit: "CTN", gw: "18,200", cbm: "65.0" },
  { cno: "MSKU3456789", type: "40GP", seal: "SL345678", pkg: 650,  unit: "CTN", gw: "15,800", cbm: "60.2" },
];
const AIR_ROWS = [
  { length: "120", width: "80",  height: "90",  qty: "1300", cbm: "87.5",  volWt: "14583" },
  { length: "100", width: "70",  height: "80",  qty: "200",  cbm: "15.0",  volWt: "2500"  },
  { length: "60",  width: "50",  height: "40",  qty: "500",  cbm: "6.0",   volWt: "1000"  },
];

export function MasterContainerDimPanel({ variant }: Props) {
  const isSea = variant.mode === "SEA";
  const ml    = getModeLabels(variant.mode);
  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{ml.containerPanel}</span>
        <span className="panel__rowcount">{isSea ? SEA_ROWS.length : AIR_ROWS.length}</span>
        {!isSea && <div className="panel__actions"><button className="btn btn--sm">Load Dimension</button><button className="btn btn--sm">+</button></div>}
      </div>
      <div style={{ overflow: "auto", flex: 1 }}>
        {isSea ? (
          <table className="grid--list">
            <thead><tr><th className="row-num">#</th><th>Container No.</th><th>Type</th><th>Seal No.</th><th className="is-num">Pkg</th><th>Unit</th><th className="is-num">G/W</th><th className="is-num">CBM</th><th>SOC</th></tr></thead>
            <tbody>
              {SEA_ROWS.map((r, i) => (
                <tr key={r.cno} style={{ color: "var(--ink-3)" }}>
                  <td className="row-num">{i + 1}</td>
                  <td className="cell-mono">{r.cno}</td><td>{r.type}</td><td className="cell-mono">{r.seal}</td>
                  <td className="is-num cell-mono">{r.pkg}</td><td>{r.unit}</td>
                  <td className="is-num cell-mono">{r.gw}</td><td className="is-num cell-mono">{r.cbm}</td><td>N</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <table className="grid--list">
            <thead><tr><th className="row-num">#</th><th className="is-num">Length</th><th className="is-num">Width</th><th className="is-num">Height</th><th className="is-num">Qty</th><th className="is-num">CBM</th><th className="is-num">Vol. Wt.</th></tr></thead>
            <tbody>
              {AIR_ROWS.map((r, i) => (
                <tr key={`${r.length}-${r.width}-${r.height}`}>
                  <td className="row-num">{i + 1}</td>
                  {(["length","width","height","qty","cbm","volWt"] as const).map(k => (
                    <td key={k} className="is-num"><NumericCell defaultValue={r[k]} /></td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
