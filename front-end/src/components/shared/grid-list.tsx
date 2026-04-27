"use client";

import React from "react";
import { useColumnLayout } from "@/lib/use-column-layout";
import { GridListHeader } from "./grid-list-header";

export interface GridColumn<T> {
  key: keyof T | string;
  label: string;
  minWidth?: number;
  width?: number;
  align?: "left" | "center" | "right";
  required?: boolean;
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
}

// gridId 있을 때: 컬럼 커스터마이징(리사이즈/재정렬/숨김) 적용 그리드
function ManagedGridList<T>({
  gridId,
  columns: defaultColumns,
  data,
  onRowClick,
  rowKey,
  rowClassName,
  className,
  style,
  emptyMessage,
}: GridListProps<T> & { gridId: string }) {
  const { visibleColumns, resizeColumn, reorderColumn, hideColumn } =
    useColumnLayout(gridId, defaultColumns);

  return (
    <div className={`grid-wrap${className ? ` ${className}` : ""}`} style={style}>
      <table className="grid--list">
        <colgroup>
          {visibleColumns.map((col) => (
            <col key={String(col.key)} style={{ width: col.width ?? col.minWidth }} />
          ))}
        </colgroup>
        <GridListHeader<T>
          columns={visibleColumns}
          onReorder={reorderColumn}
          onHide={hideColumn}
          onResize={resizeColumn}
        />
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td colSpan={visibleColumns.length} className="grid__empty">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row, rowIndex) => {
              const key = rowKey ? rowKey(row, rowIndex) : rowIndex;
              const extraClass = rowClassName ? rowClassName(row, rowIndex) : undefined;
              return (
                <tr
                  key={key}
                  className={extraClass}
                  onClick={onRowClick ? () => onRowClick(row, rowIndex) : undefined}
                  style={onRowClick ? { cursor: "pointer" } : undefined}
                >
                  {visibleColumns.map((col) => {
                    const rawValue =
                      typeof col.key === "string"
                        ? (row as Record<string, unknown>)[col.key]
                        : (row[col.key as keyof T] as unknown);
                    return (
                      <td
                        key={String(col.key)}
                        style={{ width: col.width ?? col.minWidth, textAlign: col.align }}
                        className={col.className}
                      >
                        {col.render
                          ? col.render(rawValue, row, rowIndex)
                          : String(rawValue ?? "")}
                      </td>
                    );
                  })}
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}

// gridId 없을 때: 기존 렌더 로직 그대로 유지 (non-breaking)
function PlainGridList<T>({
  columns,
  data,
  onRowClick,
  rowKey,
  rowClassName,
  className,
  style,
  emptyMessage,
}: Omit<GridListProps<T>, "gridId">) {
  return (
    <div className={`grid-wrap${className ? ` ${className}` : ""}`} style={style}>
      <table className="grid--list">
        <colgroup>
          {columns.map((col) => (
            <col key={String(col.key)} style={{ width: col.width ?? col.minWidth }} />
          ))}
        </colgroup>
        <thead>
          <tr>
            {columns.map((col) => (
              <th
                key={String(col.key)}
                style={{
                  width: col.width ?? col.minWidth,
                  textAlign: col.align,
                }}
                className={col.required ? "is-required" : undefined}
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="grid__empty">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row, rowIndex) => {
              const key = rowKey ? rowKey(row, rowIndex) : rowIndex;
              const extraClass = rowClassName ? rowClassName(row, rowIndex) : undefined;
              return (
                <tr
                  key={key}
                  className={extraClass}
                  onClick={onRowClick ? () => onRowClick(row, rowIndex) : undefined}
                  style={onRowClick ? { cursor: "pointer" } : undefined}
                >
                  {columns.map((col) => {
                    const rawValue =
                      typeof col.key === "string"
                        ? (row as Record<string, unknown>)[col.key]
                        : (row[col.key as keyof T] as unknown);
                    return (
                      <td
                        key={String(col.key)}
                        style={{ width: col.width ?? col.minWidth, textAlign: col.align }}
                        className={col.className}
                      >
                        {col.render
                          ? col.render(rawValue, row, rowIndex)
                          : String(rawValue ?? "")}
                      </td>
                    );
                  })}
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}

export function GridList<T>({ gridId, ...rest }: GridListProps<T>) {
  if (gridId) {
    return <ManagedGridList<T> gridId={gridId} {...rest} />;
  }
  return <PlainGridList<T> {...rest} />;
}
