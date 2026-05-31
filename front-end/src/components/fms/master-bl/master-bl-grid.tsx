"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import type { MasterVariantConfig } from "@/lib/bl-variants";
import { getPageTitle } from "@/lib/bl-variants";
import { masterBlPort } from "@/lib/ports";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useTabs } from "@/lib/use-tabs";
import type { MasterBlRow, MasterBlFilter } from "@/domain/master-bl";
import type { Bound } from "@/domain/house-bl";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { getModeLabels } from "@/lib/bl-mode-labels";
import { DEFAULT_PAGE_SIZE, cyclePageSize } from "@/lib/grid-pagination";

interface Props {
  variantKey: string;
  variant: MasterVariantConfig;
  extraFilter?: Partial<MasterBlFilter>;
}

export function MasterBlGrid({ variantKey, variant, extraFilter = {} }: Props) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const clearDraft = useBLDraftStore((s) => s.clearDraft);
  const setFocus = useEntryFocusStore((s) => s.setFocus);
  const addTab = useTabs((s) => s.addTab);
  const [selected, setSelected] = useState<number | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const modeLabels = getModeLabels(variant.mode);

  const handleCyclePageSize = () => {
    setPageSize(cyclePageSize(pageSize));
    setCurrentPage(1);
  };

  // 렌더 중 이전 값 비교 — variantKey/extraFilter 변경 시 currentPage를 1로 리셋
  // (React "Adjusting state when a prop changes" 패턴, effect 내 setState 금지 규칙 준수)
  const filterKey = JSON.stringify(extraFilter ?? {});
  const [prevKeys, setPrevKeys] = useState({ variantKey, filterKey });
  if (prevKeys.variantKey !== variantKey || prevKeys.filterKey !== filterKey) {
    setPrevKeys({ variantKey, filterKey });
    setCurrentPage(1);
  }

  // TRUCK/NON_BL은 direction이 null이므로 해당 variant에서는 쿼리를 실행하지 않음
  const { data, isFetching, error } = useQuery({
    queryKey: ["master-bl", "list", variantKey, extraFilter, currentPage, pageSize],
    queryFn: () => masterBlPort.list(
      { bound: variant.direction as Bound, ...extraFilter },
      currentPage,
      pageSize,
    ),
    enabled: variant.direction !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  const columns: GridColumn<MasterBlRow>[] = [
    {
      key: "mblNo",
      label: modeLabels.blNo,
      minWidth: 140,
      render: (_v, row) => (
        <span
          className="cell-hbl"
          onDoubleClick={() => {
            const path = `/fms/master-bl/${variantKey}/entry`;
            // 프레시 조회: stale 캐시·draft 제거 후 Entry 진입
            queryClient.invalidateQueries({ queryKey: ["master-bl", "detail", row.id] });
            clearDraft(`master:${variantKey}:${row.id}`);
            setFocus(entryFocusKeys.masterBl(variantKey), row.id);
            sessionStorage.setItem(`master-bl-entry:hot:${row.id}`, "1");
            addTab(getPageTitle(variant, "Master", "Entry"), path);
            router.push(path);
          }}
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
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
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
          scrollPositionKey={`list-scroll:master-bl:${variantKey}`}
        />
      </div>
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
        disabled={isFetching}
        pageSize={pageSize}
        onCyclePageSize={handleCyclePageSize}
      />
    </div>
  );
}
