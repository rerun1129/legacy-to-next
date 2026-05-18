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
  /** true이면 행 앞에 체크박스 td를 렌더한다. */
  selectable?: boolean;
  /** 체크박스의 checked 상태. */
  selected?: boolean;
  /** 체크박스 클릭 시 호출. stopPropagation으로 onRowClick과 분리된다. */
  onToggleSelect?: () => void;
}

function GridRowInner<T>(props: GridRowProps<T>) {
  const { row, rowIndex, columns, rowKey, extraClassName, onRowClick, selectedRowKey, onSelectRow, measureRef, dataIndex, selectable, selected, onToggleSelect } = props;

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

  function handleCheckboxClick(e: React.MouseEvent<HTMLTableCellElement>) {
    // 행 onRowClick/onSelectRow가 발동되지 않도록 이벤트 전파를 차단한다.
    e.stopPropagation();
    onToggleSelect?.();
  }

  return (
    <tr
      ref={measureRef}
      data-index={dataIndex}
      className={finalClassName}
      onMouseDown={onRowClick || onSelectRow ? handleMouseDown : undefined}
      style={onRowClick || onSelectRow ? { cursor: "pointer" } : undefined}
    >
      {selectable && (
        <td className="grid__select-cell" onClick={handleCheckboxClick}>
          <input
            type="checkbox"
            className="chk"
            checked={selected ?? false}
            onChange={() => { /* checked는 onClick으로 제어 */ }}
          />
        </td>
      )}
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
