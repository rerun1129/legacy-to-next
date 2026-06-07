"use client";

import { useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import { Ship } from "lucide-react";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { seaHousePort } from "@/lib/ports";
import { fmtDate, fmtWeight } from "@/lib/grid-formatters";
import type { SeaHouseRow, SeaHouseFilter } from "@/domain/sea-house";

interface Props {
  extraFilter: SeaHouseFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  pageSize: number;
  onPageSizeChange: (size: number) => void;
  bound: "EXP" | "IMP";
}

export function SeaHouseGrid({ extraFilter, currentPage, onPageChange, pageSize, onPageSizeChange, bound }: Props) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const setFocus = useEntryFocusStore((s) => s.setFocus);
  const clearDraft = useBLDraftStore((s) => s.clearDraft);
  const [selected, setSelected] = useState<number | null>(null);
  const tc = useTranslations("fms.seaHouse.list.cols");
  const tl = useTranslations("fms.seaHouse.list");
  const tCommon = useTranslations("common");

  const { data, isFetching, error } = useQuery({
    queryKey: ["sea-house", "list", bound, extraFilter, currentPage, pageSize],
    queryFn: () => seaHousePort.list(
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

  function handleHblDoubleClick(row: SeaHouseRow) {
    const variantKey = bound === "EXP" ? "sea-exp" : "sea-imp";
    const path = `/fms/house-bl/${variantKey}/entry`;
    // 프레시 조회: stale 캐시·draft 제거 후 Entry 진입
    queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", row.id] });
    clearDraft(`house:${variantKey}:${row.id}`);
    setFocus(entryFocusKeys.houseBl(variantKey), row.id);
    // hot-marker: Entry 진입 시 하이라이트(§6.16)
    sessionStorage.setItem(`house-bl-entry:hot:${row.id}`, "1");
    router.push(path);
  }

  // 컬럼 배열은 t 참조가 바뀔 때만 재계산 — useMemo([tc])로 render loop 방지
  const columns = useMemo<GridColumn<SeaHouseRow>[]>(() => [
    {
      key: "hblNo",
      label: tc("hblNo"),
      minWidth: 160,
      render: (v, row) => (
        <div
          onDoubleClick={() => handleHblDoubleClick(row)}
          style={{ cursor: "pointer" }}
        >
          {String(v ?? "")}
        </div>
      ),
    },
    { key: "bound",               label: tc("bound"),               minWidth: 60 },
    { key: "mblNo",               label: tc("mblNo"),               minWidth: 160 },
    { key: "masterRefNo",         label: tc("masterRefNo"),         minWidth: 160 },
    { key: "shipmentType",        label: tc("shipmentType"),        minWidth: 100 },
    { key: "loadType",            label: tc("loadType"),            minWidth: 90 },
    { key: "etd",                 label: tc("etd"),                 minWidth: 100, render: (v) => fmtDate(v) },
    { key: "eta",                 label: tc("eta"),                 minWidth: 100, render: (v) => fmtDate(v) },
    { key: "polCode",             label: tc("polCode"),             minWidth: 100 },
    { key: "podCode",             label: tc("podCode"),             minWidth: 100 },
    { key: "deliveryCode",        label: tc("deliveryCode"),        minWidth: 90 },
    { key: "vesselName",          label: tc("vesselName"),          minWidth: 140 },
    { key: "voyageNo",            label: tc("voyageNo"),            minWidth: 90 },
    { key: "shipperCode",         label: tc("shipperCode"),         minWidth: 90 },
    { key: "shipperName",         label: tc("shipperName"),         minWidth: 140 },
    { key: "consigneeCode",       label: tc("consigneeCode"),       minWidth: 90 },
    { key: "consigneeName",       label: tc("consigneeName"),       minWidth: 150 },
    { key: "notifyCode",          label: tc("notifyCode"),          minWidth: 90 },
    { key: "notifyName",          label: tc("notifyName"),          minWidth: 140 },
    { key: "settlePartnerCode",   label: tc("settlePartnerCode"),   minWidth: 110 },
    { key: "settlePartnerName",   label: tc("settlePartnerName"),   minWidth: 160 },
    { key: "docPartnerCode",      label: tc("docPartnerCode"),      minWidth: 100 },
    { key: "docPartnerName",      label: tc("docPartnerName"),      minWidth: 150 },
    { key: "linerCode",           label: tc("linerCode"),           minWidth: 110 },
    { key: "linerName",           label: tc("linerName"),           minWidth: 160 },
    { key: "freightTerm",         label: tc("freightTerm"),         minWidth: 100 },
    { key: "incoterms",           label: tc("incoterms"),           minWidth: 90 },
    { key: "actualCustomerCode",  label: tc("actualCustomerCode"),  minWidth: 120 },
    { key: "actualCustomerName",  label: tc("actualCustomerName"),  minWidth: 160 },
    { key: "pkgQty",              label: tc("pkgQty"),              minWidth: 90 },
    { key: "pkgUnit",             label: tc("pkgUnit"),             minWidth: 70 },
    { key: "grossWeightKg",       label: tc("grossWeightKg"),       minWidth: 100, render: (v) => fmtWeight(v), aggregate: "sum", aggregateDecimals: 3 },
    { key: "cbm",                 label: tc("cbm"),                 minWidth: 90,  render: (v) => (v != null ? (v as number).toFixed(3) : ''), aggregate: "sum", aggregateDecimals: 3 },
    { key: "cntr20Qty",           label: tc("cntr20Qty"),           minWidth: 70,  aggregate: "sum", aggregateDecimals: 0 },
    { key: "cntr40Qty",           label: tc("cntr40Qty"),           minWidth: 70,  aggregate: "sum", aggregateDecimals: 0 },
    { key: "teuQty",              label: tc("teuQty"),              minWidth: 70,  render: (v) => (v != null ? (v as number).toFixed(2) : ''), aggregate: "sum", aggregateDecimals: 2 },
    { key: "salesManCode",        label: tc("salesManCode"),        minWidth: 90 },
    { key: "teamCode",            label: tc("teamCode"),            minWidth: 90 },
    { key: "teamName",            label: tc("teamName"),            minWidth: 140 },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [tc]);
  // handleHblDoubleClick은 렌더마다 새 참조지만 안정 deps 포함 시 columns가
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
        <ColumnVisibilityMenu<SeaHouseRow> gridId="sea-house" defaultColumns={columns} />
      </div>
      <div className="list-wrap">
        <GridList<SeaHouseRow>
          columns={columns}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          gridId="sea-house"
          isLoading={extraFilter !== null && isFetching}
          scrollPositionKey="list-scroll:sea-house"
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
