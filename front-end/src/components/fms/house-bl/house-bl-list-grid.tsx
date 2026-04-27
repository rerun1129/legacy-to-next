"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { HOUSE_BL_ROWS } from "@/lib/mock-data";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";

type HouseBLRow = (typeof HOUSE_BL_ROWS)[number];

const STATUS_CLASS: Record<string, string> = {
  ok:     "pill--ok",
  inprog: "pill--draft",
  draft:  "pill--hold",
};
const STATUS_LABEL: Record<string, string> = {
  ok:     "Confirmed",
  inprog: "In Progress",
  draft:  "Draft",
};

interface Props { variantKey: string }

export function HouseBLListGrid({ variantKey }: Props) {
  const router   = useRouter();
  const [selected, setSelected] = useState<number | null>(null);

  function handleHblDoubleClick(_hbl: string) {
    // TODO(2026-04-27): 실제 ID 기반 라우팅으로 교체 (현재는 /entry로 이동)
    router.push(`/fms/house-bl/${variantKey}/entry`);
  }

  const columns: GridColumn<HouseBLRow>[] = [
    {
      key: "no",
      label: "#",
      minWidth: 38,
      align: "right",
      render: (_v, row) => <span className="row-num">{row.no}</span>,
    },
    {
      key: "hbl",
      label: "HBL No",
      minWidth: 140,
      render: (_v, row) => (
        <span
          className="cell-hbl"
          onDoubleClick={() => handleHblDoubleClick(row.hbl)}
          style={{ cursor: "pointer" }}
          title="더블클릭하여 Entry 열기"
        >
          {row.hbl}
        </span>
      ),
    },
    {
      key: "expImp",
      label: "Exp/Imp",
      minWidth: 66,
      align: "center",
      render: (_v, row) => (
        <span className={`chip${row.expImp === "EXP" ? " chip--accent" : ""}`}>
          {row.expImp}
        </span>
      ),
    },
    {
      key: "docStatus",
      label: "Doc Status",
      minWidth: 96,
      align: "center",
      render: (_v, row) => (
        <span className={`pill ${STATUS_CLASS[row.docStatus]}`}>
          {STATUS_LABEL[row.docStatus]}
        </span>
      ),
    },
    { key: "mbl",    label: "MBL No",   minWidth: 140, render: (_v, row) => <span className="cell-mono">{row.mbl}</span> },
    { key: "sType",  label: "Type",     align: "center", minWidth: 56 },
    { key: "lType",  label: "Load",     align: "center", minWidth: 56, render: (_v, row) => <span className="cell-dim">{row.lType}</span> },
    { key: "etd",    label: "ETD",      align: "center", minWidth: 88, render: (_v, row) => <span className="cell-mono">{row.etd}</span> },
    { key: "eta",    label: "ETA",      align: "center", minWidth: 88, render: (_v, row) => <span className="cell-mono">{row.eta}</span> },
    { key: "regDate",label: "Reg. Date",align: "center", minWidth: 88, render: (_v, row) => <span className="cell-mono">{row.regDate}</span> },
    { key: "pol",    label: "POL",      align: "center", minWidth: 60, render: (_v, row) => <span className="port__code">{row.pol}</span> },
    { key: "pod",    label: "POD",      align: "center", minWidth: 60, render: (_v, row) => <span className="port__code">{row.pod}</span> },
    { key: "vessel", label: "Vessel",   minWidth: 160 },
    { key: "voyage", label: "Voyage",   align: "center", minWidth: 72, render: (_v, row) => <span className="cell-mono">{row.voyage}</span> },
    { key: "shipper",   label: "Shipper",   minWidth: 160 },
    { key: "consignee", label: "Consignee", minWidth: 160 },
  ];

  return (
    <div className="panel panel--list" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title">
          <div className="panel__title-accent" />
          House B/L
          <span className="panel__rowcount">{HOUSE_BL_ROWS.length}</span>
        </div>
        <ColumnVisibilityMenu<HouseBLRow> gridId="house-bl-list" defaultColumns={columns} />
      </div>

      <div className="panel__body panel__body--flush" style={{ flex: 1, minHeight: 0, display: "flex" }}>
        <div className="list-wrap">
          <GridList
            columns={columns}
            data={HOUSE_BL_ROWS}
            rowKey={(row) => row.no}
            onRowClick={(row) => setSelected(row.no)}
            rowClassName={(row) => selected === row.no ? "is-selected" : undefined}
            gridId="house-bl-list"
          />
        </div>
      </div>
    </div>
  );
}
