import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

interface ItemRow {
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

const ITEM_DATA: ItemRow[] = [
  { hs: "8517.13", desc: "MOBILE PHONE PARTS",         qty: "1300", unit: "CTN", value: "48500.00", cur: "USD" },
  { hs: "8517.62", desc: "WIRELESS MODULE PARTS",      qty: "200",  unit: "CTN", value: "12000.00", cur: "USD" },
  { hs: "8542.31", desc: "SEMICONDUCTOR IC CHIPS",     qty: "500",  unit: "CTN", value: "35000.00", cur: "USD" },
  { hs: "8504.40", desc: "POWER SUPPLY UNITS",         qty: "150",  unit: "CTN", value: "8500.00",  cur: "USD" },
  { hs: "8516.40", desc: "FLAT IRON HEATING ELEMENTS", qty: "200",  unit: "CTN", value: "6200.00",  cur: "USD" },
  { hs: "8536.20", desc: "AUTOMATIC CIRCUIT BREAKERS", qty: "80",   unit: "CTN", value: "3200.00",  cur: "USD" },
];

export function ItemHsPanel() {
  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Item / HS Code</span>
        <span className="panel__rowcount">{ITEM_DATA.length}</span>
        <div className="panel__actions"><button className="btn btn--sm">+</button></div>
      </div>
      <GridList columns={ITEM_COLS} data={ITEM_DATA} rowKey={(_, i) => i} style={{ flex: 1, minHeight: 0 }} />
    </div>
  );
}
