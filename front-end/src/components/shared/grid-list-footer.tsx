import React from "react";
import type { GridColumn } from "./grid-list";
import { fmtSum } from "@/lib/grid-formatters";

function sumColumn<T>(data: T[], key: string): number {
  return data.reduce(
    (acc, row) => acc + (Number((row as Record<string, unknown>)[key]) || 0),
    0,
  );
}

interface GridFooterProps<T> {
  columns: GridColumn<T>[];
  data: T[];
  totalAggregates?: Record<string, number>;
  selectable?: boolean;
}

/**
 * 집계 컬럼이 1개 이상인 경우에만 tfoot 2행(Page Total / Total)을 렌더한다.
 * data가 비어있으면 표시하지 않는다.
 *
 * - 1행 "Page Total": 현재 data 행의 합계.
 * - 2행 "Total": totalAggregates 주입 값, 미전달 시 data 합계(FE-only).
 *   향후 BE 집계 연동 시 totalAggregates를 주입하면 전체 합계로 전환된다.
 *
 * 라벨은 첫 번째 비집계 컬럼 셀에 표시한다.
 * selectable=true이면 체크박스 컬럼 정렬을 맞추기 위해 맨 앞에 빈 td를 추가한다.
 */
export function GridFooter<T>({
  columns,
  data,
  totalAggregates,
  selectable = false,
}: GridFooterProps<T>) {
  const hasAggregate = columns.some((c) => c.aggregate === "sum");
  if (!hasAggregate || data.length === 0) return null;

  // 라벨을 표시할 첫 번째 비집계 컬럼 인덱스
  const labelColIndex = columns.findIndex((c) => c.aggregate !== "sum");

  return (
    <tfoot>
      {/* 1행: 현재 페이지(data) 합계 */}
      <tr>
        {selectable && <td className="grid__select-cell" />}
        {columns.map((col, idx) => {
          const key = String(col.key);
          if (col.aggregate === "sum") {
            const pageTotal = sumColumn(data, key);
            return (
              <td
                key={key}
                style={{
                  width: col.width ?? col.minWidth,
                  textAlign: col.align ?? "right",
                }}
              >
                {fmtSum(pageTotal, col.aggregateDecimals ?? 0)}
              </td>
            );
          }
          return (
            <td
              key={key}
              className={idx === labelColIndex ? "grid-foot-label" : undefined}
              style={{ width: col.width ?? col.minWidth }}
            >
              {idx === labelColIndex ? "Page Total" : ""}
            </td>
          );
        })}
      </tr>
      {/* 2행: 전체 조회 합계 (totalAggregates 미전달 시 data 합계와 동일) */}
      <tr>
        {selectable && <td className="grid__select-cell" />}
        {columns.map((col, idx) => {
          const key = String(col.key);
          if (col.aggregate === "sum") {
            const total =
              totalAggregates?.[key] ?? sumColumn(data, key);
            return (
              <td
                key={key}
                style={{
                  width: col.width ?? col.minWidth,
                  textAlign: col.align ?? "right",
                }}
              >
                {fmtSum(total, col.aggregateDecimals ?? 0)}
              </td>
            );
          }
          return (
            <td
              key={key}
              className={idx === labelColIndex ? "grid-foot-label" : undefined}
              style={{ width: col.width ?? col.minWidth }}
            >
              {idx === labelColIndex ? "Total" : ""}
            </td>
          );
        })}
      </tr>
    </tfoot>
  );
}
