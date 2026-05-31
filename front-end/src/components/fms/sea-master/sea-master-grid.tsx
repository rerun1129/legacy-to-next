"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import { useTabs } from "@/lib/use-tabs";
import { Ship } from "lucide-react";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { seaMasterPort } from "@/lib/ports";
import { fmtDate, fmtWeight } from "@/lib/grid-formatters";
import type { SeaMasterRow, SeaMasterFilter } from "@/domain/sea-master";

interface Props {
  extraFilter: SeaMasterFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  showAll: boolean;
  onToggleShowAll: () => void;
  bound: "EXP" | "IMP";
}

export function SeaMasterGrid({ extraFilter, currentPage, onPageChange, showAll, onToggleShowAll, bound }: Props) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const setFocus = useEntryFocusStore((s) => s.setFocus);
  const clearDraft = useBLDraftStore((s) => s.clearDraft);
  const addTab = useTabs((s) => s.addTab);
  const [selected, setSelected] = useState<number | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["sea-master", "list", bound, extraFilter, showAll ? "all" : currentPage],
    queryFn: () => seaMasterPort.list(
      { ...extraFilter!, bound },
      showAll ? 1 : currentPage,
      showAll ? 10000000 : 50,
    ),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity, // staleTime: Infinity만으로는 gcTime 기본 5분에 막혀 무력화됨 (§6.36)
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  const columns: GridColumn<SeaMasterRow>[] = [
    {
      key: "mblNo",
      label: "Master B/L No",
      minWidth: 160,
      render: (v, row) => (
        <div
          onDoubleClick={() => {
            const variantKey = bound === "EXP" ? "sea-exp" : "sea-imp";
            const path = `/fms/master-bl/${variantKey}/entry`;
            // 프레시 조회: stale 캐시·draft 제거 후 Entry 진입
            queryClient.invalidateQueries({ queryKey: ["master-bl", "detail", row.id] });
            clearDraft(`master:${variantKey}:${row.id}`);
            setFocus(entryFocusKeys.masterBl(variantKey), row.id);
            sessionStorage.setItem(`master-bl-entry:hot:${row.id}`, "1");
            addTab(`Master B/L Sea ${bound === "EXP" ? "Export" : "Import"} Entry`, path);
            router.push(path);
          }}
          style={{ cursor: "pointer" }}
        >
          {String(v ?? "")}
        </div>
      ),
    },
    { key: "masterRefNo",   label: "Master Reference No.", minWidth: 160 },
    { key: "bound",         label: "Bound",                minWidth: 60 },
    { key: "etd",           label: "ETD",                  minWidth: 100, render: (v) => fmtDate(v) },
    { key: "eta",           label: "ETA",                  minWidth: 100, render: (v) => fmtDate(v) },
    { key: "polCode",       label: "POL",                  minWidth: 90 },
    { key: "podCode",       label: "POD",                  minWidth: 90 },
    { key: "vesselName",    label: "Vessel Name",          minWidth: 140, render: (v) => String(v ?? '') },
    { key: "voyageNo",      label: "Voyage",               minWidth: 100, render: (v) => String(v ?? '') },
    { key: "shipperCode",   label: "Shipper",              minWidth: 90 },
    { key: "shipperName",   label: "Shipper Name",         minWidth: 140 },
    { key: "consigneeCode", label: "Consignee",            minWidth: 90 },
    { key: "consigneeName", label: "Consignee Name",       minWidth: 150 },
    { key: "notifyCode",    label: "Notify",               minWidth: 90 },
    { key: "notifyName",    label: "Notify Name",          minWidth: 140 },
    { key: "linerCode",     label: "Liner",                minWidth: 90 },
    { key: "linerName",     label: "Liner Name",           minWidth: 140 },
    { key: "loadType",      label: "Load Type",            minWidth: 100, render: (v) => String(v ?? '') },
    { key: "houseBlCount",  label: "House B/L Count",      minWidth: 120 },
    { key: "pkgQty",        label: "Package",              minWidth: 90 },
    { key: "pkgUnit",       label: "Unit",                 minWidth: 70 },
    { key: "grossWeightKg", label: "Gross W/T",            minWidth: 100, render: (v) => fmtWeight(v) },
    { key: "cbm",           label: "CBM",                  minWidth: 90,  render: (v) => (v != null ? Number(v).toFixed(3) : '') },
    { key: "operatorCode",  label: "Operator",             minWidth: 90 },
    { key: "teamCode",      label: "Team",                 minWidth: 90 },
    { key: "teamName",      label: "Team Name",            minWidth: 140 },
  ];

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Sea Master B/L</span>
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
        <Ship size={14} style={{ marginRight: 4 }} />
        <span className="panel__title">Sea Master B/L</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        <ColumnVisibilityMenu<SeaMasterRow> gridId="sea-master" defaultColumns={columns} />
      </div>
      <div className="list-wrap">
        <GridList<SeaMasterRow>
          columns={columns}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          gridId="sea-master"
          isLoading={extraFilter !== null && isFetching}
          scrollPositionKey="list-scroll:sea-master"
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
