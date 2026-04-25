"use client";
import React from "react";

export interface GridTableColumn<T> {
  key: keyof T | string;
  label: string;
  width?: number;
  align?: "left" | "center" | "right";
  required?: boolean;       // th에 is-required 클래스
  className?: string;       // td className (is-num 등)
  render?: (value: unknown, row: T, index: number) => React.ReactNode;
}

export interface GridTableProps<T> {
  columns: GridTableColumn<T>[];
  data: T[];
  rowKey?: (row: T, index: number) => string | number;
  emptyMessage?: string;
  style?: React.CSSProperties;   // table 자체 style
}

export function GridTable<T>({ columns, data, rowKey, emptyMessage, style }: GridTableProps<T>) {
  return (
    <table className="grid" style={style}>
      <colgroup>
        {columns.map((col) => (
          <col key={String(col.key)} style={{ width: col.width }} />
        ))}
      </colgroup>
      <thead>
        <tr>
          {columns.map((col) => (
            <th
              key={String(col.key)}
              style={{ width: col.width, textAlign: col.align }}
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
              {emptyMessage ?? "No data"}
            </td>
          </tr>
        ) : (
          data.map((row, rowIndex) => {
            const key = rowKey ? rowKey(row, rowIndex) : rowIndex;
            return (
              <tr key={key}>
                {columns.map((col) => {
                  const rawValue =
                    typeof col.key === "string"
                      ? (row as Record<string, unknown>)[col.key]
                      : (row[col.key as keyof T] as unknown);
                  return (
                    <td
                      key={String(col.key)}
                      style={{ textAlign: col.align }}
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
  );
}
