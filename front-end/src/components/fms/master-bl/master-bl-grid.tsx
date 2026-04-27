"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { Mode } from "@/lib/bl-variants";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { getModeLabels } from "@/lib/bl-mode-labels";

function selectActiveColumns<T>(mode: Mode, seaColumns: T[], airColumns: T[]): T[] {
  return mode === "SEA" ? seaColumns : airColumns;
}

const ROWS = [
  { mbl: "COSCO2404195",  isSea: true,  ref: "MR-2026-04195", bkg: "BKG-COSCO-0412", vessel: "COSCO EXCELLENCE", etd: "04/24", eta: "05/08", pol: "KRBSAN", pod: "CNSHA", houses: 2 },
  { mbl: "HAPAG0418011",  isSea: true,  ref: "MR-2026-04180", bkg: "BKG-HAPAG-0419", vessel: "HAPAG EXPRESS",    etd: "04/23", eta: "05/12", pol: "KRICN",  pod: "DEHAM",  houses: 1 },
  { mbl: "180-12345678",  isSea: false, ref: "MR-2026-04195", bkg: "",                vessel: "KE851",            etd: "04/24", eta: "04/24", pol: "ICN",    pod: "PVG",    houses: 2 },
  { mbl: "176-87654321",  isSea: false, ref: "MR-2026-04180", bkg: "",                vessel: "OZ741",            etd: "04/23", eta: "04/23", pol: "GMP",    pod: "NRT",    houses: 1 },
];

type MblRow = typeof ROWS[number];

interface Props { variantKey: string; mode: Mode }

export function MasterBlGrid({ variantKey, mode }: Props) {
  const router = useRouter();
  const [selected, setSelected] = useState<string | null>(null);
  const isSea = mode === "SEA";
  const modeLabels = getModeLabels(mode);
  const rows = ROWS.filter((r) => r.isSea === isSea);

  const seaColumns: GridColumn<MblRow>[] = [
    {
      key: "mbl",
      label: modeLabels.blNo,
      minWidth: 140,
      render: (value) => (
        <span
          className="cell-hbl"
          onDoubleClick={() => router.push(`/fms/master-bl/${variantKey}/entry`)}
          style={{ cursor: "pointer" }}
          title="더블클릭하여 Entry 열기"
        >
          {String(value ?? "")}
        </span>
      ),
    },
    { key: "ref",    label: "Master Ref",   minWidth: 130, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "bkg",    label: "Line Bkg. No", minWidth: 130, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "vessel", label: "Vessel / Flight", minWidth: 160 },
    { key: "etd",    label: "ETD", minWidth: 60, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "eta",    label: "ETA", minWidth: 60, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "pol",    label: "POL", minWidth: 70, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "pod",    label: "POD", minWidth: 70, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "houses", label: "Houses", minWidth: 70, align: "right", render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "status", label: "Status", minWidth: 90, render: () => <span className="pill pill--ok">Confirmed</span> },
  ];

  const airColumns: GridColumn<MblRow>[] = seaColumns.filter((c) => c.key !== "bkg");

  const activeColumns = selectActiveColumns(mode, seaColumns, airColumns);

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Master B/L</span>
        <span className="panel__rowcount">{rows.length}</span>
        <ColumnVisibilityMenu<MblRow> gridId="master-bl" defaultColumns={activeColumns} />
      </div>
      <div className="list-wrap">
        <GridList<MblRow>
          columns={activeColumns}
          data={rows}
          onRowClick={(row) => setSelected(row.mbl)}
          rowKey={(row) => row.mbl}
          rowClassName={(row) => (selected === row.mbl ? "is-selected" : undefined)}
          gridId="master-bl"
        />
      </div>
    </div>
  );
}
