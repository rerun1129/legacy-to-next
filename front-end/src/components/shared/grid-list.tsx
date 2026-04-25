"use client";

import React from "react";

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
  emptyMessage?: string;
}

export function GridList<T>({
  columns,
  data,
  onRowClick,
  rowKey,
  rowClassName,
  className,
  emptyMessage,
}: GridListProps<T>) {
  return (
    <div className={`grid-wrap${className ? ` ${className}` : ""}`}>
      <table className="grid grid--list">
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
