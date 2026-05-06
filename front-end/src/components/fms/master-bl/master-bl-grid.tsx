"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { masterBlPort } from "@/lib/ports";
import type { MasterBlRow, MasterBlFilter } from "@/domain/master-bl";
import type { Bound } from "@/domain/house-bl";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { getModeLabels } from "@/lib/bl-mode-labels";

interface Props {
  variantKey: string;
  variant: MasterVariantConfig;
  extraFilter?: Partial<MasterBlFilter>;
}

export function MasterBlGrid({ variantKey, variant, extraFilter = {} }: Props) {
  const router = useRouter();
  const [selected, setSelected] = useState<number | null>(null);
  const modeLabels = getModeLabels(variant.mode);

  // TRUCK/NON_BL은 direction이 null이므로 해당 variant에서는 쿼리를 실행하지 않음
  const { data: rows = [], isFetching, error } = useQuery({
    queryKey: ["master-bl", "list", variantKey, extraFilter],
    queryFn: () => masterBlPort.list({ bound: variant.direction as Bound, ...extraFilter }),
    enabled: variant.direction !== null,
  });

  const columns: GridColumn<MasterBlRow>[] = [
    {
      key: "mblNo",
      label: modeLabels.blNo,
      minWidth: 140,
      render: (_v, row) => (
        <span
          className="cell-hbl"
          onDoubleClick={() => router.push(`/fms/master-bl/${variantKey}/entry?id=${row.id}`)}
          style={{ cursor: "pointer" }}
          title="더블클릭하여 Entry 열기"
        >
          {row.mblNo}
        </span>
      ),
    },
    {
      // TODO: BE 미반영 — masterRefNo 필드가 MasterBlRow에 없음
      key: "masterRefNo",
      label: "Master Ref",
      minWidth: 130,
      render: () => <span className="cell-mono">-</span>,
    },
    {
      key: "bound",
      label: "Bound",
      minWidth: 66,
      align: "center" as const,
      render: (_v, row) => (
        <span className={`chip${row.bound === "EXP" ? " chip--accent" : ""}`}>
          {row.bound}
        </span>
      ),
    },
    {
      key: "shipperCode",
      label: "Shipper",
      minWidth: 130,
      render: (_v, row) => <span className="cell-mono">{row.shipperCode}</span>,
    },
    {
      key: "consigneeCode",
      label: "Consignee",
      minWidth: 130,
      render: (_v, row) => <span className="cell-mono">{row.consigneeCode}</span>,
    },
    {
      key: "polCode",
      label: "POL",
      minWidth: 70,
      align: "center" as const,
      render: (_v, row) => <span className="port__code">{row.polCode}</span>,
    },
    {
      key: "podCode",
      label: "POD",
      minWidth: 70,
      align: "center" as const,
      render: (_v, row) => <span className="port__code">{row.podCode}</span>,
    },
    {
      key: "etd",
      label: "ETD",
      minWidth: 88,
      align: "center" as const,
      render: (_v, row) => <span className="cell-mono">{row.etd}</span>,
    },
    {
      key: "eta",
      label: "ETA",
      minWidth: 88,
      align: "center" as const,
      render: (_v, row) => <span className="cell-mono">{row.eta}</span>,
    },
    {
      // TODO: BE 미반영 — operatorCode 필드가 MasterBlRow에 없음
      key: "operatorCode",
      label: "Operator",
      minWidth: 100,
      render: () => <span className="cell-mono">-</span>,
    },
    {
      // TODO: BE 미반영 — createdAt 필드가 MasterBlRow에 없음
      key: "createdAt",
      label: "Reg. Date",
      minWidth: 88,
      align: "center" as const,
      render: () => <span className="cell-mono">-</span>,
    },
  ];

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Master B/L</span>
        </div>
        <div className="list-wrap" style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}>
          <span className="text-error">데이터를 불러오지 못했습니다.</span>
        </div>
      </div>
    );
  }

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Master B/L</span>
        <span className="panel__rowcount">{rows.length}</span>
        <ColumnVisibilityMenu<MasterBlRow> gridId="master-bl" defaultColumns={columns} />
      </div>
      <div className="list-wrap">
        <GridList<MasterBlRow>
          columns={columns}
          data={rows}
          onRowClick={(row) => setSelected(row.id ?? null)}
          rowKey={(row) => row.id ?? row.mblNo}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          gridId="master-bl"
          isLoading={isFetching}
        />
      </div>
    </div>
  );
}
