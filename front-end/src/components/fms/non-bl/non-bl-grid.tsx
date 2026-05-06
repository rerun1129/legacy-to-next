"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { nonBlPort } from "@/lib/ports";
import { useEnumOptions } from "@/application/enums/use-enum";
import { fmtDate } from "@/lib/grid-formatters";
import type { NonBlRow, NonBlFilter } from "@/domain/non-bl";

interface Props {
  extraFilter: NonBlFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  showAll: boolean;
  onToggleShowAll: () => void;
}

export function NonBlGrid({ extraFilter, currentPage, onPageChange, showAll, onToggleShowAll }: Props) {
  const router = useRouter();
  const { options: boundOptions } = useEnumOptions("Bound");
  const [selected, setSelected] = useState<number | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ["non-bl", "list", extraFilter, showAll ? "all" : currentPage],
    queryFn: () => nonBlPort.list(extraFilter!, showAll ? 1 : currentPage, showAll ? 10000000 : 50),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  const columns: GridColumn<NonBlRow>[] = [
    {
      key: "nonBlNo",
      label: "Non B/L No",
      minWidth: 150,
      render: (v) => (
        <div
          onDoubleClick={() => router.push("/fms/non-bl/entry")}
          style={{ cursor: "pointer" }}
        >
          {String(v ?? "")}
        </div>
      ),
    },
    {
      key: "bound",
      label: "Bound",
      minWidth: 90,
      render: (v) => {
        const code = String(v ?? '');
        return boundOptions.find(o => o.value === code)?.label ?? code;
      },
    },
    { key: "etd", label: "ETD", minWidth: 110, render: (v) => fmtDate(v) },
    { key: "eta", label: "ETA", minWidth: 110, render: (v) => fmtDate(v) },
    { key: "pol", label: "POL", minWidth: 80 },
    { key: "pod", label: "POD", minWidth: 80 },
    { key: "vesselName", label: "Vessel", minWidth: 130 },
    { key: "voyNo", label: "Voyage", minWidth: 80 },
    { key: "shipperCode", label: "Shipper", minWidth: 90 },
    { key: "shipperName", label: "Shipper Name", minWidth: 140 },
    { key: "consigneeCode", label: "Consignee", minWidth: 90 },
    { key: "consigneeName", label: "Consignee Name", minWidth: 150 },
    { key: "notifyCode", label: "Notify", minWidth: 90 },
    { key: "notifyName", label: "Notify Name", minWidth: 140 },
    { key: "settlePartnerCode", label: "Partner", minWidth: 90 },
    { key: "settlePartnerName", label: "Partner Name", minWidth: 140 },
    { key: "linerCode", label: "Liner", minWidth: 80 },
    { key: "linerName", label: "Liner Name", minWidth: 120 },
    { key: "actualCustomerCode", label: "Actual Customer", minWidth: 110 },
    { key: "actualCustomerName", label: "Actual Customer Name", minWidth: 160 },
    { key: "pkgQty", label: "Package", minWidth: 80 },
    { key: "pkgUnit", label: "Package Unit", minWidth: 90 },
    { key: "grossWt", label: "Gross W/T", minWidth: 100 },
    { key: "cbm", label: "CBM", minWidth: 90 },
    { key: "teamName", label: "Team Name", minWidth: 90 },
  ];

  if (extraFilter !== null && isLoading) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Non B/L</span>
        </div>
        <div className="list-wrap" style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}>
          <span className="text-muted">Loading...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Non B/L</span>
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
        <span className="panel__title">Non B/L</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        <ColumnVisibilityMenu<NonBlRow> gridId="non-bl" defaultColumns={columns} />
      </div>
      <div className="list-wrap">
        <GridList<NonBlRow>
          columns={columns}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          gridId="non-bl"
        />
      </div>
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={onPageChange}
        disabled={isLoading}
        showAll={showAll}
        onToggleShowAll={onToggleShowAll}
      />
    </div>
  );
}
