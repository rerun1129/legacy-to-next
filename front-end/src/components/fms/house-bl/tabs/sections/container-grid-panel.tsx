import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { NumericCell } from "@/components/shared/grid-cell-inputs";

interface ContainerRow {
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

const CONTAINER_DATA: ContainerRow[] = [
  { cno: "CSNU1234567", type: "20GP", seal: "SL123456", pkg: 500, pkgT: "CTN", gw: "12400", cbm: 22.5, vgm: "12540" },
  { cno: "TCKU9876543", type: "40HC", seal: "SL789012", pkg: 800, pkgT: "CTN", gw: "18200", cbm: 65.0, vgm: "18380" },
  { cno: "MSKU3456789", type: "40GP", seal: "SL345678", pkg: 650, pkgT: "CTN", gw: "15800", cbm: 60.2, vgm: "15960" },
  { cno: "HLXU2345678", type: "20GP", seal: "SL456789", pkg: 420, pkgT: "CTN", gw: "10500", cbm: 21.0, vgm: "10640" },
  { cno: "GESU5678901", type: "40HC", seal: "SL567890", pkg: 750, pkgT: "CTN", gw: "19400", cbm: 67.5, vgm: "19580" },
  { cno: "TCNU8901234", type: "20GP", seal: "SL678901", pkg: 350, pkgT: "CTN", gw: "8750",  cbm: 19.8, vgm: "8900"  },
];

export function ContainerGridPanel() {
  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Container</span>
        <span className="panel__rowcount">{CONTAINER_DATA.length}</span>
        <div className="panel__actions"><button className="btn btn--sm">+</button></div>
      </div>
      <GridList columns={CONTAINER_COLS} data={CONTAINER_DATA} rowKey={(_, i) => i} style={{ flex: 1, minHeight: 0 }} />
    </div>
  );
}
