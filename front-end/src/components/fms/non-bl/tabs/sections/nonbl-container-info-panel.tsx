"use client";

import { Plus, Minus } from "lucide-react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface ContainerInfoRow {
  id: number;
  cno: string;
  contType: string;
  sealNo1: string;
  pkg: number;
  pkgUnit: string;
  grossWt: number;
  cbm: number;
}

const COLS: GridColumn<ContainerInfoRow>[] = [
  { key: "_no",      width: 50, label: "#",                 className: "row-num", render: (_, __, i) => i + 1 },
  { key: "cno",      width: 80, label: "Container No." },
  { key: "contType", width: 80, label: "Cont.Type" },
  { key: "sealNo1",  width: 80, label: "Seal No. 1" },
  { key: "sealNo2",  width: 80, label: "Seal No. 2" },
  { key: "sealNo3",  width: 80, label: "Seal No. 3" },
  { key: "pkg",      width: 80, label: "Package",     className: "is-num" },
  { key: "pkgUnit",  width: 80, label: "Unit" },
  { key: "grossWt",  width: 80, label: "Gross W/T",   className: "is-num" },
  { key: "cbm",      width: 80, label: "CBM",         className: "is-num" },
];

export function NonBLContainerInfoPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Container Information</span>
        <div className="panel__actions">
          <button className="btn btn--sm btn--ghost" onClick={() => { /* TODO */ }}><Plus size={12} /></button>
          <button className="btn btn--sm btn--ghost" onClick={() => { /* TODO */ }}><Minus size={12} /></button>
        </div>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <GridList columns={COLS} data={[]} rowKey={(r) => r.id} />
      </div>
    </div>
  );
}
