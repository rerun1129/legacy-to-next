import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

interface ContainerRow {
  id: number;
  cno: string; type: string; seal: string; pkg: number; pkgT: string;
  gw: string; cbm: number; vgm: string;
}

const CONTAINER_COLS: GridColumn<ContainerRow>[] = [
  { key: "_no",  label: "#",            width: 36,  className: "row-num", render: (_, __, i) => i + 1 },
  { key: "cno",  label: "Container No", width: 160, render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }} /> },
  { key: "type", label: "Type",         width: 70,  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "seal", label: "Seal No",      width: 110, render: (v) => <input className="grid__cell-input" defaultValue={String(v)} style={{ fontFamily: "var(--font-mono)" }} /> },
  { key: "pkg",  label: "Pkg",          width: 70,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "pkgT", label: "Unit",         width: 60,  render: (v) => <input className="grid__cell-input" defaultValue={String(v)} /> },
  { key: "gw",   label: "G/W",          width: 90,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "cbm",  label: "CBM",          width: 80,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
  { key: "vgm",  label: "VGM",          width: 90,  className: "is-num", render: (v) => <NumericCell defaultValue={String(v)} /> },
];

const CONTAINER_DATA: ContainerRow[] = [];

export function ContainerGridPanel() {
  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Container</span>
        <span className="panel__rowcount">{CONTAINER_DATA.length}</span>
        <div className="panel__actions"><button className="btn btn--sm">+</button></div>
      </div>
      <GridList columns={CONTAINER_COLS} data={CONTAINER_DATA} rowKey={(row) => row.id} style={{ flex: 1, minHeight: 0 }} />
    </div>
  );
}
