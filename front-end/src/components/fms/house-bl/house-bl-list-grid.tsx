"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { houseBlPort } from "@/application/house-bl/bindings";
import { getBLVariant } from "@/lib/bl-variants";
import type { HouseBlRow } from "@/domain/house-bl";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";

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
  const router  = useRouter();
  const variant = getBLVariant(variantKey);
  const [selected, setSelected] = useState<number | null>(null);

  // variant.mode → jobDiv (SEA/AIR), variant.direction → bound (EXP/IMP)
  const { data: rows = [], isLoading, error } = useQuery({
    queryKey: ["house-bl", "list", variantKey],
    queryFn: () =>
      houseBlPort.list({ jobDiv: variant.mode, bound: variant.direction }),
  });

  if (isLoading) return <div className="p-4 text-sm text-muted-foreground">로딩 중...</div>;
  if (error) return <div className="p-4 text-sm text-destructive">데이터를 불러올 수 없습니다.</div>;

  function handleHblDoubleClick(row: HouseBlRow) {
    router.push(`/fms/house-bl/${variantKey}/entry?id=${row.id}`);
  }

  const columns: GridColumn<HouseBlRow>[] = [
    {
      key: "id",
      label: "#",
      minWidth: 38,
      align: "right",
      render: (_v, row) => <span className="row-num">{row.id}</span>,
    },
    {
      key: "hblNo",
      label: "HBL No",
      minWidth: 140,
      render: (_v, row) => (
        <span
          className="cell-hbl"
          onDoubleClick={() => handleHblDoubleClick(row)}
          style={{ cursor: "pointer" }}
          title="더블클릭하여 Entry 열기"
        >
          {row.hblNo}
        </span>
      ),
    },
    {
      key: "bound",
      label: "Exp/Imp",
      minWidth: 66,
      align: "center",
      render: (_v, row) => (
        <span className={`chip${row.bound === "EXP" ? " chip--accent" : ""}`}>
          {row.bound}
        </span>
      ),
    },
    {
      key: "docStatus",
      label: "Doc Status",
      minWidth: 96,
      align: "center",
      render: (_v, row) => {
        const status = row.docStatus ?? "";
        return (
          <span className={`pill ${STATUS_CLASS[status] ?? ""}`}>
            {STATUS_LABEL[status] ?? "-"}
          </span>
        );
      },
    },
    {
      key: "masterBlId",
      label: "MBL No",
      minWidth: 140,
      render: (_v, row) => (
        <span className="cell-mono">{row.masterBlId ?? "-"}</span>
      ),
    },
    // BE 미반영 — 헤더 유지, 값은 '-' 표시 (key는 컬럼 식별자로만 사용)
    { key: "_sType", label: "Type",  align: "center", minWidth: 56, render: () => <span>-</span> },
    { key: "_lType", label: "Load",  align: "center", minWidth: 56, render: () => <span className="cell-dim">-</span> },
    {
      key: "etd",
      label: "ETD",
      align: "center",
      minWidth: 88,
      render: (_v, row) => <span className="cell-mono">{row.etd ?? "-"}</span>,
    },
    {
      key: "eta",
      label: "ETA",
      align: "center",
      minWidth: 88,
      render: (_v, row) => <span className="cell-mono">{row.eta ?? "-"}</span>,
    },
    {
      key: "createdAt",
      label: "Reg. Date",
      align: "center",
      minWidth: 88,
      render: (_v, row) => <span className="cell-mono">{row.createdAt ?? "-"}</span>,
    },
    {
      key: "polCode",
      label: "POL",
      align: "center",
      minWidth: 60,
      render: (_v, row) => <span className="port__code">{row.polCode ?? "-"}</span>,
    },
    {
      key: "podCode",
      label: "POD",
      align: "center",
      minWidth: 60,
      render: (_v, row) => <span className="port__code">{row.podCode ?? "-"}</span>,
    },
    // BE 미반영
    { key: "_vessel", label: "Vessel", minWidth: 160, render: () => <span>-</span> },
    { key: "_voyage", label: "Voyage", align: "center", minWidth: 72, render: () => <span className="cell-mono">-</span> },
    {
      key: "shipperCode",
      label: "Shipper",
      minWidth: 160,
      render: (_v, row) => <span>{row.shipperCode ?? "-"}</span>,
    },
    {
      key: "consigneeCode",
      label: "Consignee",
      minWidth: 160,
      render: (_v, row) => <span>{row.consigneeCode ?? "-"}</span>,
    },
  ];

  return (
    <div className="panel panel--list" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title">
          <div className="panel__title-accent" />
          House B/L
          <span className="panel__rowcount">{rows.length}</span>
        </div>
        <ColumnVisibilityMenu<HouseBlRow> gridId="house-bl-list" defaultColumns={columns} />
      </div>

      <div className="panel__body panel__body--flush" style={{ flex: 1, minHeight: 0, display: "flex" }}>
        <div className="list-wrap">
          <GridList
            columns={columns}
            data={rows}
            rowKey={(row) => row.id}
            onRowClick={(row) => setSelected(row.id)}
            rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
            gridId="house-bl-list"
          />
        </div>
      </div>
    </div>
  );
}
