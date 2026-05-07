"use client";

import React from "react";
import type { GridColumn, GridListProps } from "./grid-list";

export interface GridRowProps<T> {
  row: T;
  rowIndex: number;
  columns: GridColumn<T>[];
  rowKey: GridListProps<T>["rowKey"];
  rowClassName: GridListProps<T>["rowClassName"];
  onRowClick: GridListProps<T>["onRowClick"];
  selectedRowKey: string | number | null | undefined;
  onSelectRow: ((row: T | null, index: number | null) => void) | undefined;
}

function GridRowInner<T>(props: GridRowProps<T>) {
  const { row, rowIndex, columns, rowKey, rowClassName, onRowClick, selectedRowKey, onSelectRow } = props;

  const key = rowKey
    ? rowKey(row, rowIndex)
    : ((row as Record<string, unknown>).id as string | number | undefined) ?? rowIndex;
  const extraClass = rowClassName ? rowClassName(row, rowIndex) : undefined;
  const isSelected = selectedRowKey != null && key === selectedRowKey;
  const finalClassName =
    [extraClass, isSelected ? "is-selected" : undefined].filter(Boolean).join(" ") || undefined;

  function handleClick() {
    if (onRowClick) onRowClick(row, rowIndex);
    if (onSelectRow) {
      onSelectRow(isSelected ? null : row, isSelected ? null : rowIndex);
    }
  }

  return (
    <tr
      className={finalClassName}
      onClick={onRowClick || onSelectRow ? handleClick : undefined}
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
