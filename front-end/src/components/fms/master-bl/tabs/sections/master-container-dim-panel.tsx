import { getModeLabels } from "@/lib/bl-mode-labels";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

interface Props { variant?: AnyVariantConfig }

const SEA_ROWS: { cno: string; type: string; seal: string; pkg: number; unit: string; gw: string; cbm: string }[] = [];
const AIR_ROWS: { length: string; width: string; height: string; qty: string; cbm: string; volWt: string }[] = [];

export function MasterContainerDimPanel({ variant }: Props) {
  if (!variant) return null;
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
