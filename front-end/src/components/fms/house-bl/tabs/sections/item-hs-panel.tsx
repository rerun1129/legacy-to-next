import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

interface ItemRow {
  id: number;
  hs: string; desc: string; qty: string; unit: string; value: string; cur: string;
}

const ITEM_COLS: GridColumn<ItemRow>[] = [
  { key: "_no",   label: "#",           width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
  { key: "hs",    label: "HS Code",     width: 100, render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "desc",  label: "Description", width: 200, render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "qty",   label: "Qty",         width: 70,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "unit",  label: "Unit",        width: 60,  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "value", label: "Value",       width: 100, className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "cur",   label: "Currency",    width: 80,  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
];

const ITEM_DATA: ItemRow[] = [];

export function ItemHsPanel() {
  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Item / HS Code</span>
        <span className="panel__rowcount">{ITEM_DATA.length}</span>
        <div className="panel__actions"><button className="btn btn--sm">+</button></div>
      </div>
      <GridList columns={ITEM_COLS} data={ITEM_DATA} rowKey={(row) => row.id} style={{ flex: 1, minHeight: 0 }} />
    </div>
  );
}
