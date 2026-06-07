"use client";

import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { usePmsEnumOptions } from "@/application/pms/enums/use-pms-enum";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { pmsPerformancePort } from "@/lib/ports";
import { pmsPerformanceKeys } from "@/application/pms/performance/use-cases";
import { buildDetailColumns } from "./pms-performance-detail-columns";
import { buildPmsDimensionCatalog, PMS_MEASURES } from "./pms-performance-aggregate-model";
import { PmsPerformanceAggregateView } from "./pms-performance-aggregate-view";
import { PmsViewToggle, PmsAggregateDimBar, type PmsViewMode } from "./pms-performance-aggregate-toolbar";
import type { PmsPerformanceRow, SearchPmsPerformanceInput } from "@/application/pms/performance/ports";

interface Props {
  searchFilter: SearchPmsPerformanceInput | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  pageSize: number;
  onPageSizeChange: (size: number) => void;
}

/**
 * PS-01 실적 그리드 — 상세(36컬럼)/집계 토글.
 * searchFilter가 null이면 enabled=false로 조회하지 않음 (Reset 정책).
 * viewMode/dimKeys는 로컬 state — 페이지 변경/재조회 시 유지.
 */
export function PmsPerformanceGrid({
  searchFilter,
  currentPage,
  onPageChange,
  pageSize,
  onPageSizeChange,
}: Props) {
  const { options: jobDivOptions } = usePmsEnumOptions("JobDiv");
  const { options: boundOptions }  = usePmsEnumOptions("Bound");

  const [viewMode, setViewMode] = useState<PmsViewMode>("detail");
  const [dimKeys, setDimKeys]   = useState<string[]>([]);

  const enabled = searchFilter !== null;
  const queryInput = searchFilter
    ? { ...searchFilter, page: currentPage - 1, size: pageSize }
    : null;

  const { data, isFetching } = useQuery({
    queryKey: pmsPerformanceKeys.search(queryInput ?? { basis: "FREIGHT_INPUT", page: 0, size: pageSize }),
    queryFn: () => pmsPerformancePort.search(queryInput!),
    enabled,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows: PmsPerformanceRow[] = data?.content ?? [];
  const totalPages    = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const detailColumns = useMemo(
    () => buildDetailColumns(jobDivOptions, boundOptions),
    [jobDivOptions, boundOptions],
  );

  const dimCatalog = useMemo(
    () => buildPmsDimensionCatalog({ jobDivOptions, boundOptions }),
    [jobDivOptions, boundOptions],
  );

  // 선택된 dim 인스턴스 — useMemo로 참조 안정화 (aggregate view useMemo 의존)
  const selectedDims = useMemo(
    () => dimKeys
      .map((k) => dimCatalog.find((d) => d.key === k))
      .filter((d) => d !== undefined),
    [dimKeys, dimCatalog],
  );

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">실적 조회</span>
          <span className="panel__rowcount">{totalElements}</span>
          {/* 토글은 panel__head 우측 */}
          <PmsViewToggle viewMode={viewMode} onViewModeChange={setViewMode} />
        </div>
        {/* dim 피커 서브툴바 — 집계 모드 전용, panel__head 하단 */}
        {viewMode === "aggregate" && (
          <PmsAggregateDimBar
            catalog={dimCatalog}
            dimKeys={dimKeys}
            onDimKeysChange={setDimKeys}
          />
        )}
        {viewMode === "detail" ? (
          <div className="list-wrap">
            <GridList<PmsPerformanceRow>
              columns={detailColumns}
              data={rows}
              rowKey={(row) => `${row.blType}-${row.blId}`}
              isLoading={isFetching}
              emptyMessage="데이터가 없습니다."
            />
          </div>
        ) : (
          <PmsPerformanceAggregateView
            key={dimKeys.join("|") || "all"}
            rows={rows}
            dims={selectedDims}
            measures={PMS_MEASURES}
          />
        )}
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={onPageChange}
          disabled={isFetching}
          pageSize={pageSize}
          onPageSizeChange={onPageSizeChange}
        />
      </div>
    </div>
  );
}
