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
  selectable?: boolean;
  /** 합계에 포함할 행 필터. 미지정 시 전체 data 합산. */
  footerSumFilter?: (row: T) => boolean;
}

/**
 * 집계 컬럼이 1개 이상인 경우에만 tfoot 1행(Page Total)을 렌더한다.
 * data가 비어있으면 표시하지 않는다.
 *
 * - "Page Total": 현재 data 행의 합계.
 *
 * 라벨은 첫 번째 비집계 컬럼 셀에 표시한다.
 * selectable=true이면 체크박스 컬럼 정렬을 맞추기 위해 맨 앞에 빈 td를 추가한다.
 */
export function GridFooter<T>({
  columns,
  data,
  selectable = false,
  footerSumFilter,
}: GridFooterProps<T>) {
  const hasAggregate = columns.some((c) => c.aggregate === "sum");
  if (!hasAggregate || data.length === 0) return null;

  // footerSumFilter가 지정된 경우 해당 행만 합산 대상으로 사용
  const sumData = footerSumFilter ? data.filter(footerSumFilter) : data;

  // 라벨을 표시할 첫 번째 비집계 컬럼 인덱스
  const labelColIndex = columns.findIndex((c) => c.aggregate !== "sum");

  return (
    <tfoot>
      {/* 1행: 현재 페이지(sumData) 합계 */}
      <tr>
        {selectable && <td className="grid__select-cell" />}
        {columns.map((col, idx) => {
          const key = String(col.key);
          if (col.aggregate === "sum") {
            // footerValue 접근자가 있으면 우선 사용, 없으면 row[key] 기본 합산
            const pageTotal = col.footerValue
              ? sumData.reduce((acc, row) => acc + (Number(col.footerValue!(row)) || 0), 0)
              : sumColumn(sumData, key);
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
    </tfoot>
  );
}
