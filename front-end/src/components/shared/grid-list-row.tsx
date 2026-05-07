"use client";

import React from "react";
import type { GridColumn, GridListProps } from "./grid-list";

export interface GridRowProps<T> {
  row: T;
  rowIndex: number;
  columns: GridColumn<T>[];
  rowKey: GridListProps<T>["rowKey"];
  /** 호출처에서 rowClassName(row, index)를 직접 계산해 전달한다. ref로 고정된 stableRowClassName 대신 이 값이 변하면 GridRow가 재렌더된다. */
  extraClassName?: string;
  onRowClick: GridListProps<T>["onRowClick"];
  selectedRowKey: string | number | null | undefined;
  onSelectRow: ((row: T | null, index: number | null) => void) | undefined;
  /** virtualizer measureElement 콜백. 실측 row 높이를 virtualizer에 전달한다. */
  measureRef?: (el: HTMLTableRowElement | null) => void;
  /** virtualizer의 row 인덱스. measureElement가 DOM 요소와 측정값을 연결하는 데 사용된다. */
  dataIndex?: number;
}

function GridRowInner<T>(props: GridRowProps<T>) {
  const { row, rowIndex, columns, rowKey, extraClassName, onRowClick, selectedRowKey, onSelectRow, measureRef, dataIndex } = props;

  const key = rowKey
    ? rowKey(row, rowIndex)
    : ((row as Record<string, unknown>).id as string | number | undefined) ?? rowIndex;
  const isSelected = selectedRowKey != null && key === selectedRowKey;
  const finalClassName =
    [extraClassName, isSelected ? "is-selected" : undefined].filter(Boolean).join(" ") || undefined;

  function handleMouseDown(e: React.MouseEvent<HTMLTableRowElement>) {
    if (e.button !== 0) return;
    if (onRowClick) onRowClick(row, rowIndex);
    if (onSelectRow) {
      onSelectRow(isSelected ? null : row, isSelected ? null : rowIndex);
    }
  }

  return (
    <tr
      ref={measureRef}
      data-index={dataIndex}
      className={finalClassName}
      onMouseDown={onRowClick || onSelectRow ? handleMouseDown : undefined}
      style={onRowClick || onSelectRow ? { cursor: "pointer" } : undefined}
    >
      {columns.map((col) => {
        const rawValue =
          typeof col.key === "string"
            ? (row as Record<string, unknown>)[col.key]
            : (row[col.key as keyof T] as unknown);
        const ck = String(col.key);
        return (
          <td
            key={ck}
            data-row-key={String(key)}
            data-col-key={ck}
            style={{ width: col.width ?? col.minWidth, textAlign: col.align }}
            className={col.className}
          >
            {col.render ? col.render(rawValue, row, rowIndex) : String(rawValue ?? "")}
          </td>
        );
      })}
    </tr>
  );
}

export const GridRow = React.memo(GridRowInner) as typeof GridRowInner;
