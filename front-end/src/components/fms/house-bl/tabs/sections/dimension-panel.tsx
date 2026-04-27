import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

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

const DATA: DimRow[] = [
  { id: 1, length: "120", width: "80",  height: "90",  qty: "1300", cbm: "87.5",  volWt: "14583" },
  { id: 2, length: "100", width: "70",  height: "80",  qty: "200",  cbm: "15.0",  volWt: "2500"  },
  { id: 3, length: "150", width: "100", height: "120", qty: "50",   cbm: "22.5",  volWt: "3750"  },
  { id: 4, length: "60",  width: "50",  height: "40",  qty: "500",  cbm: "6.0",   volWt: "1000"  },
  { id: 5, length: "200", width: "120", height: "150", qty: "20",   cbm: "72.0",  volWt: "12000" },
  { id: 6, length: "80",  width: "60",  height: "50",  qty: "300",  cbm: "7.2",   volWt: "1200"  },
];

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
