"use client";

import { useMemo, useRef, useState } from "react";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { usePmsEnumOptions } from "@/application/pms/enums/use-pms-enum";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { buildDetailColumns } from "./pms-performance-detail-columns";
import { buildPmsDimensionCatalog, PMS_MEASURES } from "./pms-performance-aggregate-model";
import { PmsPerformanceAggregateView } from "./pms-performance-aggregate-view";
import { PmsViewToggle, PmsAggregateDimBar, type PmsViewMode } from "./pms-performance-aggregate-toolbar";
import { PmsColumnFilterPopover } from "./pms-column-filter-popover";
import { usePmsPerformanceSearch } from "./use-pms-performance-search";
import { distinctDisplayValues, applyColumnFilters, type ColumnFilterState } from "@/lib/grid-column-filter";
import type { PmsPerformanceRow, SearchPmsPerformanceInput } from "@/application/pms/performance/ports";

interface Props {
  searchFilter: SearchPmsPerformanceInput | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  pageSize: number;
  onPageSizeChange: (size: number) => void;
}

/**
 * 깔때기 필터를 부여할 9개 컬럼 키.
 * 모두 render 포매터 없는 평문 컬럼 → 표시값 = 원시 셀 문자열(String(row[key] ?? "")).
 */
const FILTER_COLUMN_KEYS = [
  "teamName",
  "actualCustomerCode",
  "settlePartnerCode",
  "linerCode",
  "polCode",
  "podCode",
  "salesManName",
  "incoterms",
  "loadType",
] as const satisfies ReadonlyArray<keyof PmsPerformanceRow>;

/** 각 컬럼 키에 대응하는 표시값 추출 함수 맵. */
const FILTER_ACCESSORS: Record<string, (r: PmsPerformanceRow) => string> = Object.fromEntries(
  FILTER_COLUMN_KEYS.map((key) => [key, (r: PmsPerformanceRow) => String(r[key] ?? "")]),
);

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

  // ── 컬럼 필터 상태 ────────────────────────────────────────
  const [columnFilters, setColumnFilters] = useState<ColumnFilterState>({});

  // 새 Search/Reset 시(filterSig 변경 시) 컬럼 필터 초기화.
  // useEffect-setState 금지(react-hooks/set-state-in-effect) → 렌더 중 prevRef 가드로 처리.
  const prevSigRef = useRef(filterSig);
  if (prevSigRef.current !== filterSig) {
    prevSigRef.current = filterSig;
    // 렌더 중 직접 setState: 동일 렌더 사이클 내에서 재렌더를 유발하지만 React 공식 지원 패턴.
    setColumnFilters({});
  }

  const activeFilterCount = Object.keys(columnFilters).length;

  /** filteredRows: 클라이언트 컬럼 필터 적용 결과. */
  const filteredRows = useMemo(
    () => applyColumnFilters(rows, FILTER_ACCESSORS, columnFilters),
    [rows, columnFilters],
  );

  const detailColumns = useMemo(
    () => buildDetailColumns(jobDivOptions, boundOptions),
    [jobDivOptions, boundOptions],
  );

  // 9개 컬럼에 headerAccessory(깔때기 버튼) 주입.
  // deps: rows(distinct 목록 소스), columnFilters(활성 상태 표시), detailColumns(기본 컬럼 정의).
  const columnsWithFilter = useMemo(() => {
    return detailColumns.map((col) => {
      const key = String(col.key);
      if (!(key in FILTER_ACCESSORS)) return col;
      return {
        ...col,
        headerAccessory: () => (
          <PmsColumnFilterPopover
            label={col.label}
            values={distinctDisplayValues(rows, FILTER_ACCESSORS[key])}
            selected={columnFilters[key]}
            onChange={(next) => {
              setColumnFilters((prev) => {
                if (next === null) {
                  const rest = { ...prev };
                  delete rest[key];
                  return rest;
                }
                return { ...prev, [key]: next };
              });
            }}
          />
        ),
      };
    });
  }, [detailColumns, rows, columnFilters]);

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
            {/* 컬럼 필터 활성 시 건수 표시 + 전체 지우기 */}
            {activeFilterCount > 0 && (
              <>
                <span style={{ marginLeft: 6, color: "var(--primary, #2563eb)", fontWeight: 500 }}>
                  (필터 {filteredRows.length}건)
                </span>
                <button
                  type="button"
                  onClick={() => setColumnFilters({})}
                  style={{
                    marginLeft: 6,
                    fontSize: "11px",
                    padding: "1px 6px",
                    background: "var(--surface-2, #f9fafb)",
                    border: "1px solid var(--border, #e5e7eb)",
                    borderRadius: "var(--radius-sm, 4px)",
                    cursor: "pointer",
                    color: "var(--ink-3, #6b7280)",
                  }}
                >
                  전체 지우기
                </button>
              </>
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
              columns={columnsWithFilter}
              data={filteredRows}
              rowKey={(row) => `${row.blType}-${row.blId}`}
              isLoading={isFetching}
              emptyMessage="데이터가 없습니다."
            />
          </div>
        ) : (
          <PmsPerformanceAggregateView
            key={dimKeys.join("|") || "all"}
            rows={filteredRows}
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
