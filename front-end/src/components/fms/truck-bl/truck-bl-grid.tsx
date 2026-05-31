"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { truckBlPort } from "@/lib/ports";
import { useTabs } from "@/lib/use-tabs";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useEnumOptions } from "@/application/enums/use-enum";
import { fmtDate } from "@/lib/grid-formatters";
import type { TruckBlRow, TruckBlFilter } from "@/domain/truck-bl";

interface Props {
  extraFilter: TruckBlFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  showAll: boolean;
  onToggleShowAll: () => void;
}

export function TruckBlGrid({ extraFilter, currentPage, onPageChange, showAll, onToggleShowAll }: Props) {
  const router = useRouter();
  const addTab = useTabs((s) => s.addTab);
  const setFocus = useEntryFocusStore((s) => s.setFocus);
  const queryClient = useQueryClient();
  const clearDraft = useBLDraftStore((s) => s.clearDraft);
  const { options: boundOptions } = useEnumOptions("Bound");
  const [selected, setSelected] = useState<number | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["truck-bl", "list", extraFilter, showAll ? "all" : currentPage],
    queryFn: () => truckBlPort.list(extraFilter!, showAll ? 1 : currentPage, showAll ? 10000000 : 50),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity, // staleTime: Infinity만으로는 gcTime 기본 5분에 막혀 무력화됨 (§6.36)
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  const columns: GridColumn<TruckBlRow>[] = [
    {
      key: "truckBlNo",
      label: "Truck B/L No",
      minWidth: 150,
      render: (v, row) => (
        <div
          onDoubleClick={() => {
            // 프레시 조회: stale 캐시·draft 제거 후 Entry 진입
            queryClient.invalidateQueries({ queryKey: ["truck-bl", "detail", row.id] });
            clearDraft("truck::" + row.id);
            // hot-marker: Entry 화면이 신규 진입 시 하이라이트에 사용 (§6.16)
            sessionStorage.setItem(`truck-bl-entry:hot:${row.id}`, "1");
            setFocus(entryFocusKeys.truckBl, row.id);
            addTab("Truck B/L Entry", "/fms/truck-bl/entry");
            router.push("/fms/truck-bl/entry");
          }}
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
    { key: "truckerCode", label: "Trucker", minWidth: 110 },
    { key: "truckerName", label: "Trucker Name", minWidth: 140 },
    { key: "shipperCode", label: "Shipper", minWidth: 90 },
    { key: "shipperName", label: "Shipper Name", minWidth: 140 },
    { key: "consigneeCode", label: "Consignee", minWidth: 90 },
    { key: "consigneeName", label: "Consignee Name", minWidth: 150 },
    { key: "notifyCode", label: "Notify", minWidth: 90 },
    { key: "notifyName", label: "Notify Name", minWidth: 140 },
    { key: "docPartnerCode", label: "DOC Partner", minWidth: 90 },
    { key: "docPartnerName", label: "DOC Partner Name", minWidth: 140 },
    { key: "pkgQty", label: "Package", minWidth: 80 },
    { key: "pkgUnit", label: "Package Unit", minWidth: 90 },
    { key: "grossWt", label: "Gross W/T", minWidth: 100 },
    { key: "cbm", label: "CBM", minWidth: 90 },
    { key: "teamCode", label: "Team", minWidth: 90 },
    { key: "teamName", label: "Team Name", minWidth: 140 },
  ];

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Truck B/L</span>
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
        <span className="panel__title">Truck B/L</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        <ColumnVisibilityMenu<TruckBlRow> gridId="truck-bl" defaultColumns={columns} />
      </div>
      <div className="list-wrap">
        <GridList<TruckBlRow>
          columns={columns}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          gridId="truck-bl"
          isLoading={extraFilter !== null && isFetching}
          scrollPositionKey="list-scroll:truck-bl"
        />
      </div>
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={onPageChange}
        disabled={isFetching}
        showAll={showAll}
        onToggleShowAll={onToggleShowAll}
      />
    </div>
  );
}
