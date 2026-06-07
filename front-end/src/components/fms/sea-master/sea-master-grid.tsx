"use client";

import { useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
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
  pageSize: number;
  onPageSizeChange: (size: number) => void;
  bound: "EXP" | "IMP";
}

export function SeaMasterGrid({ extraFilter, currentPage, onPageChange, pageSize, onPageSizeChange, bound }: Props) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const setFocus = useEntryFocusStore((s) => s.setFocus);
  const clearDraft = useBLDraftStore((s) => s.clearDraft);
  const addTab = useTabs((s) => s.addTab);
  const [selected, setSelected] = useState<number | null>(null);
  const tc = useTranslations("fms.seaMaster.list.cols");
  const tl = useTranslations("fms.seaMaster.list");
  const tCommon = useTranslations("common");

  const { data, isFetching, error } = useQuery({
    queryKey: ["sea-master", "list", bound, extraFilter, currentPage, pageSize],
    queryFn: () => seaMasterPort.list(
      { ...extraFilter!, bound },
      currentPage,
      pageSize,
    ),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity, // staleTime: Infinity만으로는 gcTime 기본 5분에 막혀 무력화됨 (§6.36)
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  // 컬럼 배열은 tc 참조가 바뀔 때만 재계산 — useMemo([tc])로 render loop 방지
  const columns = useMemo<GridColumn<SeaMasterRow>[]>(() => [
    {
      key: "mblNo",
      label: tc("mblNo"),
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
    { key: "masterRefNo",   label: tc("masterRefNo"),   minWidth: 160 },
    { key: "bound",         label: tc("bound"),         minWidth: 60 },
    { key: "etd",           label: tc("etd"),           minWidth: 100, render: (v) => fmtDate(v) },
    { key: "eta",           label: tc("eta"),           minWidth: 100, render: (v) => fmtDate(v) },
    { key: "polCode",       label: tc("polCode"),       minWidth: 90 },
    { key: "podCode",       label: tc("podCode"),       minWidth: 90 },
    { key: "vesselName",    label: tc("vesselName"),    minWidth: 140, render: (v) => String(v ?? '') },
    { key: "voyageNo",      label: tc("voyageNo"),      minWidth: 100, render: (v) => String(v ?? '') },
    { key: "shipperCode",   label: tc("shipperCode"),   minWidth: 90 },
    { key: "shipperName",   label: tc("shipperName"),   minWidth: 140 },
    { key: "consigneeCode", label: tc("consigneeCode"), minWidth: 90 },
    { key: "consigneeName", label: tc("consigneeName"), minWidth: 150 },
    { key: "notifyCode",    label: tc("notifyCode"),    minWidth: 90 },
    { key: "notifyName",    label: tc("notifyName"),    minWidth: 140 },
    { key: "linerCode",     label: tc("linerCode"),     minWidth: 90 },
    { key: "linerName",     label: tc("linerName"),     minWidth: 140 },
    { key: "loadType",      label: tc("loadType"),      minWidth: 100, render: (v) => String(v ?? '') },
    { key: "houseBlCount",  label: tc("houseBlCount"),  minWidth: 120, aggregate: "sum", aggregateDecimals: 0 },
    { key: "pkgQty",        label: tc("pkgQty"),        minWidth: 90 },
    { key: "pkgUnit",       label: tc("pkgUnit"),       minWidth: 70 },
    { key: "grossWeightKg", label: tc("grossWeightKg"), minWidth: 100, render: (v) => fmtWeight(v), aggregate: "sum", aggregateDecimals: 3 },
    { key: "cbm",           label: tc("cbm"),           minWidth: 90,  render: (v) => (v != null ? Number(v).toFixed(3) : ''), aggregate: "sum", aggregateDecimals: 3 },
    { key: "operatorCode",  label: tc("operatorCode"),  minWidth: 90 },
    { key: "teamCode",      label: tc("teamCode"),      minWidth: 90 },
    { key: "teamName",      label: tc("teamName"),      minWidth: 140 },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [tc]);
  // bound·router·queryClient 등은 렌더마다 새 참조지만 안정 deps 포함 시 columns가
  // 매 렌더 재계산되어 그리드 reset loop 유발 가능 → tc만 의존(행 클릭 핸들러는 render closure 허용)

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
        <Ship size={14} style={{ marginRight: 4 }} />
        <span className="panel__title">{tl("panel")}</span>
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
        pageSize={pageSize}
        onPageSizeChange={onPageSizeChange}
      />
    </div>
  );
}
