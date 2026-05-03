import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

interface DimRow { id: number; length: string; width: string; height: string; qty: string; cbm: string; volWt: string; }

const COLS: GridColumn<DimRow>[] = [
  { key: "_no",    label: "#",          className: "row-num", render: (_, __, i) => i + 1 },
  { key: "length", label: "Length",     className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "width",  label: "Width",      className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "height", label: "Height",     className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "qty",    label: "Qty",        className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "cbm",    label: "CBM",        className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
  { key: "volWt",  label: "Volume Wt.", className: "is-num",  render: v => <NumericCell defaultValue={String(v)} /> },
];

const DATA: DimRow[] = [];

export function DimensionPanel() {
  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Dimension</span>
        <span className="panel__rowcount">{DATA.length}</span>
        <div className="panel__actions"><button className="btn btn--sm">+</button></div>
      </div>
      <GridList columns={COLS} data={DATA} rowKey={(row) => row.id} style={{ flex: 1, minHeight: 0 }} />
    </div>
  );
}
