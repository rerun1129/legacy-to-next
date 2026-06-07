/**
 * 집계 트리 아웃라인 모드 GridColumn 빌더.
 * 단일 "그룹" 컬럼(들여쓰기+▾/▸ 토글 버튼) + 건수 + measure 컬럼들.
 * footerSumFilter=(r)=>r.kind==="leaf"||(접힌 group) 와 함께 사용해 grand total 불변.
 */

import type { GridColumn } from "@/components/shared/grid-list";
import type { AggregateMeasure, AggregateRow } from "@/lib/grid-aggregate";
import { fmtSum } from "@/lib/grid-formatters";

/**
 * 그룹 컬럼 1개 + 건수 컬럼 + measure 컬럼들을 순서대로 반환.
 * collapsedIds, onToggleCollapse — 그룹 셀 접기/펼치기 제어.
 */
export function buildAggregateColumns<T>(
  measures: AggregateMeasure<T>[],
  collapsedIds: ReadonlySet<string>,
  onToggleCollapse: (id: string) => void,
): GridColumn<AggregateRow>[] {
  const groupColumn: GridColumn<AggregateRow> = {
    key: "group" as keyof AggregateRow,
    label: "그룹",
    minWidth: 220,
    align: "left",
    render: (_value, row) => {
      const indent = row.level * 16;
      const label = row.labels[row.labels.length - 1] ?? "";
      if (row.kind === "group") {
        const isCollapsed = collapsedIds.has(row.id);
        return (
          <button
            type="button"
            className="agg-toggle"
            style={{ paddingLeft: indent, fontWeight: 600 }}
            onMouseDown={(e) => e.stopPropagation()}
            onClick={() => onToggleCollapse(row.id)}
          >
            <span className="agg-toggle__chevron">{isCollapsed ? "▸" : "▾"}</span>
            {label}
          </button>
        );
      }
      // leaf: chevron 폭만큼 spacer로 라벨 정렬
      return (
        <span style={{ paddingLeft: indent }}>
          <span className="agg-toggle__chevron" aria-hidden /> {label}
        </span>
      );
    },
  };

  const countColumn: GridColumn<AggregateRow> = {
    key: "count",
    label: "건수",
    minWidth: 70,
    align: "right",
    className: "is-num",
    aggregate: "sum",
    aggregateDecimals: 0,
    footerValue: (row) => row.count,
    render: (_value, row) => fmtSum(row.count, 0),
  };

  const measureColumns: GridColumn<AggregateRow>[] = measures.map((m) => ({
    key: `sums_${m.key}` as keyof AggregateRow,
    label: m.key
      .replace(/([A-Z])/g, " $1")
      .replace(/^./, (c) => c.toUpperCase()),
    minWidth: 120,
    align: "right" as const,
    className: "is-num",
    aggregate: "sum" as const,
    aggregateDecimals: m.decimals,
    footerValue: (row: AggregateRow) => row.sums[m.key],
    render: (_value: unknown, row: AggregateRow) => fmtSum(row.sums[m.key], m.decimals),
  }));

  return [groupColumn, countColumn, ...measureColumns];
}
