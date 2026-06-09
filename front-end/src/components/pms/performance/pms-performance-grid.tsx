"use client";

import { useMemo, useState } from "react";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { usePmsEnumOptions } from "@/application/pms/enums/use-pms-enum";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { buildDetailColumns } from "./pms-performance-detail-columns";
import { buildPmsDimensionCatalog, PMS_MEASURES } from "./pms-performance-aggregate-model";
import { PmsPerformanceAggregateView } from "./pms-performance-aggregate-view";
import { PmsViewToggle, PmsAggregateDimBar, type PmsViewMode } from "./pms-performance-aggregate-toolbar";
import { usePmsPerformanceSearch } from "./use-pms-performance-search";
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
  const t = useTranslations("pms.performance.filter");
  const { options: jobDivOptions } = usePmsEnumOptions("JobDiv");
  const { options: boundOptions }  = usePmsEnumOptions("Bound");

  const [viewMode, setViewMode] = useState<PmsViewMode>("detail");
  const [dimKeys, setDimKeys]   = useState<string[]>([]);

  // 정확 count opt-in: 필터 시그니처가 일치할 때만 요청 유지. 재조회(searchFilter 변경) 시 자동 해제(derived).
  const [exactSig, setExactSig] = useState<string | null>(null);
  const filterSig = useMemo(() => (searchFilter ? JSON.stringify(searchFilter) : null), [searchFilter]);
  const exactRequested = exactSig !== null && exactSig === filterSig;

  const {
    rows,
    isFetching,
    totalElements,
    totalPages,
    isApprox,
    isExactLoading,
    canRequestExact,
  } = usePmsPerformanceSearch({ searchFilter, currentPage, pageSize, exactRequested });

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
          <span
            className="panel__rowcount"
            title={isApprox ? t("approxCountTooltip") : undefined}
          >
            {canRequestExact ? (
              <button
                type="button"
                onClick={() => setExactSig(filterSig)}
                title={t("approxClickToExact")}
                style={{ background: "none", border: "none", padding: 0, font: "inherit", color: "inherit", cursor: "pointer", textDecoration: "underline dotted" }}
              >
                {`~${totalElements}`}
              </button>
            ) : (
              <>{isApprox ? `~${totalElements}` : totalElements}</>
            )}
            {isExactLoading && (
              <Loader2
                size={11}
                style={{
                  marginLeft: 3,
                  display: "inline-block",
                  verticalAlign: "middle",
                  animation: "spin 1s linear infinite",
                }}
              />
            )}
          </span>
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
