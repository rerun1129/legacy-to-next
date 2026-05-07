"use client";

import React from "react";
import { ManagedGridList } from "./grid-list-managed";
import { PlainGridList } from "./grid-list-plain";

export interface GridColumn<T> {
  key: keyof T | string;
  label: string;
  minWidth?: number;
  width?: number;
  align?: "left" | "center" | "right";
  isRequired?: boolean;
  className?: string;
  render?: (value: unknown, row: T, index: number) => React.ReactNode;
}

export interface GridListProps<T> {
  columns: GridColumn<T>[];
  data: T[];
  onRowClick?: (row: T, index: number) => void;
  rowKey?: (row: T, index: number) => string | number;
  rowClassName?: (row: T, index: number) => string | undefined;
  className?: string;
  style?: React.CSSProperties;
  emptyMessage?: string;
  gridId?: string;
  selectedRowKey?: string | number | null;
  onSelectRow?: (row: T | null, index: number | null) => void;
  isLoading?: boolean;
  skeletonRowCount?: number;
}

// --row-h 기본값 24px — tokens.css:21 정의 기준
export const ROW_HEIGHT_PX = 24;

/** 단일 행 렌더링 헬퍼 — ManagedGridList/PlainGridList 공유 */
export function renderSingleRow<T>(
  row: T,
  rowIndex: number,
  columns: GridColumn<T>[],
  rowKey: GridListProps<T>["rowKey"],
  rowClassName: GridListProps<T>["rowClassName"],
  onRowClick: GridListProps<T>["onRowClick"],
  selectedRowKey: string | number | null | undefined,
  onSelectRow: ((row: T | null, index: number | null) => void) | undefined,
  selectedCell: { rowKey: string | number; colKey: string } | null | undefined,
  setSelectedCell: React.Dispatch<React.SetStateAction<{ rowKey: string | number; colKey: string } | null>> | undefined,
  virtualKey?: string | number | bigint,
): React.ReactNode {
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
      // 같은 행 재클릭 시 선택 해제(null 전달), 그 외엔 해당 행 전달
      onSelectRow(isSelected ? null : row, isSelected ? null : rowIndex);
    }
  }

  return (
    <tr
      key={virtualKey != null ? String(virtualKey) : key}
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
        const isCellSelected =
          selectedCell?.rowKey === key && selectedCell?.colKey === ck;
        return (
          <td
            key={ck}
            data-row-key={String(key)}
            data-col-key={ck}
            style={{ width: col.width ?? col.minWidth, textAlign: col.align }}
            className={
              [col.className, isCellSelected ? "is-cell-selected" : undefined]
                .filter(Boolean)
                .join(" ") || undefined
            }
            onClick={
              setSelectedCell
                ? () => {
                    setSelectedCell((prev) =>
                      prev?.rowKey === key && prev?.colKey === ck
                        ? null
                        : { rowKey: key, colKey: ck }
                    );
                  }
                : undefined
            }
          >
            {col.render ? col.render(rawValue, row, rowIndex) : String(rawValue ?? "")}
          </td>
        );
      })}
    </tr>
  );
}

export function GridList<T>({ gridId, ...rest }: GridListProps<T>) {
  if (gridId) return <ManagedGridList<T> gridId={gridId} {...rest} />;
  return <PlainGridList<T> {...rest} />;
}
