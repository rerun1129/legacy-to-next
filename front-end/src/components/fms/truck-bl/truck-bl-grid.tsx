"use client";

import { useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
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
  pageSize: number;
  onPageSizeChange: (size: number) => void;
}

export function TruckBlGrid({ extraFilter, currentPage, onPageChange, pageSize, onPageSizeChange }: Props) {
  const router = useRouter();
  const addTab = useTabs((s) => s.addTab);
  const setFocus = useEntryFocusStore((s) => s.setFocus);
  const queryClient = useQueryClient();
  const clearDraft = useBLDraftStore((s) => s.clearDraft);
  const { options: boundOptions } = useEnumOptions("Bound");
  const [selected, setSelected] = useState<number | null>(null);
  const tc = useTranslations("fms.truckBl.list.cols");
  const tl = useTranslations("fms.truckBl.list");
  const tCommon = useTranslations("common");

  const { data, isFetching, error } = useQuery({
    queryKey: ["truck-bl", "list", extraFilter, currentPage, pageSize],
    queryFn: () => truckBlPort.list(extraFilter!, currentPage, pageSize),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity, // staleTime: Infinity만으로는 gcTime 기본 5분에 막혀 무력화됨 (§6.36)
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  // 컬럼 배열은 t 참조가 바뀔 때만 재계산 — useMemo([tc])로 render loop 방지
  const columns = useMemo<GridColumn<TruckBlRow>[]>(() => [
    {
      key: "truckBlNo",
      label: tc("truckBlNo"),
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
      label: tc("bound"),
      minWidth: 90,
      render: (v) => {
        const code = String(v ?? '');
        return boundOptions.find(o => o.value === code)?.label ?? code;
      },
    },
    { key: "etd", label: tc("etd"), minWidth: 110, render: (v) => fmtDate(v) },
    { key: "eta", label: tc("eta"), minWidth: 110, render: (v) => fmtDate(v) },
    { key: "pol", label: tc("pol"), minWidth: 80 },
    { key: "pod", label: tc("pod"), minWidth: 80 },
    { key: "truckerCode", label: tc("truckerCode"), minWidth: 110 },
    { key: "truckerName", label: tc("truckerName"), minWidth: 140 },
    { key: "shipperCode", label: tc("shipperCode"), minWidth: 90 },
    { key: "shipperName", label: tc("shipperName"), minWidth: 140 },
    { key: "consigneeCode", label: tc("consigneeCode"), minWidth: 90 },
    { key: "consigneeName", label: tc("consigneeName"), minWidth: 150 },
    { key: "notifyCode", label: tc("notifyCode"), minWidth: 90 },
    { key: "notifyName", label: tc("notifyName"), minWidth: 140 },
    { key: "docPartnerCode", label: tc("docPartnerCode"), minWidth: 90 },
    { key: "docPartnerName", label: tc("docPartnerName"), minWidth: 140 },
    { key: "pkgQty", label: tc("pkgQty"), minWidth: 80 },
    { key: "pkgUnit", label: tc("pkgUnit"), minWidth: 90 },
    { key: "grossWt", label: tc("grossWt"), minWidth: 100, aggregate: "sum", aggregateDecimals: 3 },
    { key: "cbm", label: tc("cbm"), minWidth: 90, aggregate: "sum", aggregateDecimals: 3 },
    { key: "teamCode", label: tc("teamCode"), minWidth: 90 },
    { key: "teamName", label: tc("teamName"), minWidth: 140 },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [tc, boundOptions]);
  // row-level 클릭 핸들러는 render closure 허용 — tc/boundOptions만 의존

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tl("panel")}</span>
        </div>
        <div className="list-wrap" style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}>
          <span className="text-error">{tCommon("loadFailed")}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tl("panel")}</span>
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
        pageSize={pageSize}
        onPageSizeChange={onPageSizeChange}
      />
    </div>
  );
}
