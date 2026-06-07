"use client";

import { useCallback, useMemo, useState } from "react";
import { GridList } from "@/components/shared/grid-list";
import {
  buildAggregateOutline,
  collapseOutline,
  type AggregateDimension,
  type AggregateMeasure,
  type AggregateRow,
} from "@/lib/grid-aggregate";
import { buildAggregateColumns } from "./pms-performance-aggregate-columns";
import type { PmsPerformanceRow } from "@/application/pms/performance/ports";

interface Props {
  rows: PmsPerformanceRow[];
  dims: AggregateDimension<PmsPerformanceRow>[];
  measures: AggregateMeasure<PmsPerformanceRow>[];
}

/**
 * 집계 보기 — 현재 페이지 로드분을 클라이언트에서 그룹 집계 (트리 아웃라인).
 * gridId 미전달 → PlainGridList (synthetic 컬럼이라 reorder/hide 불필요).
 * total 행 미생성 — GridFooter(footerSumFilter=leaf|접힌group)가 grand total 담당.
 * 접힘 초기화는 부모(pms-performance-grid)의 key 리마운트로 처리.
 */
export function PmsPerformanceAggregateView({ rows, dims, measures }: Props) {
  const [collapsed, setCollapsed] = useState<Set<string>>(() => new Set());

  const toggleCollapse = useCallback((id: string) => {
    setCollapsed((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }, []);

  const outlineRows = useMemo(
    () => buildAggregateOutline(rows, dims, measures),
    [rows, dims, measures],
  );

  const displayRows = useMemo(
    () => collapseOutline(outlineRows, collapsed),
    [outlineRows, collapsed],
  );

  const columns = useMemo(
    () => buildAggregateColumns(measures, collapsed, toggleCollapse),
    [measures, collapsed, toggleCollapse],
  );

  // "모두 접기/펼치기" 버튼용 — group 행 id 목록
  const allGroupIds = useMemo(
    () => outlineRows.filter((r) => r.kind === "group").map((r) => r.id),
    [outlineRows],
  );
  const anyCollapsed = collapsed.size > 0;

  if (dims.length === 0) {
    return (
      <div style={{ padding: "24px", textAlign: "center", color: "var(--ink-4)", fontSize: "13px" }}>
        그룹 컬럼을 선택하세요
      </div>
    );
  }

  if (rows.length === 0) {
    return (
      <div style={{ padding: "24px", textAlign: "center", color: "var(--ink-4)", fontSize: "13px" }}>
        집계할 데이터가 없습니다.
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
      <div
        style={{
          display: "flex",
          alignItems: "center",
          padding: "4px 8px",
          fontSize: "11px",
          color: "var(--ink-3)",
          borderBottom: "1px solid var(--divider)",
          flexShrink: 0,
          gap: 8,
        }}
      >
        <span>현재 페이지 {rows.length}행 기준 집계</span>
        {allGroupIds.length > 0 && (
          <button
            type="button"
            style={{
              marginLeft: "auto",
              fontSize: "11px",
              padding: "1px 8px",
              background: "var(--surface-2)",
              border: "1px solid var(--border)",
              borderRadius: "var(--radius-sm)",
              cursor: "pointer",
              color: "var(--ink-3)",
            }}
            onClick={() =>
              anyCollapsed
                ? setCollapsed(new Set())
                : setCollapsed(new Set(allGroupIds))
            }
          >
            {anyCollapsed ? "모두 펼치기" : "모두 접기"}
          </button>
        )}
      </div>
      <div className="list-wrap">
        <GridList<AggregateRow>
          columns={columns}
          data={displayRows}
          rowKey={(r) => r.id}
          rowClassName={(r) =>
            r.kind === "group"
              ? (r.level === 0 ? "agg-grp agg-grp--top" : "agg-grp")
              : undefined
          }
          emptyMessage="집계 결과가 없습니다."
          footerSumFilter={(r) => r.kind === "leaf" || (r.kind === "group" && collapsed.has(r.id))}
        />
      </div>
    </div>
  );
}
