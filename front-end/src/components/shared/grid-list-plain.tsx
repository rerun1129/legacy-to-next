"use client";

import { useLayoutEffect, useRef, useState } from "react";
import { useVirtualizer } from "@tanstack/react-virtual";
import { GridListProps, ROW_HEIGHT_PX, renderSingleRow } from "./grid-list";
import { useGridCellSelection } from "@/lib/use-grid-cell-selection";

export function PlainGridList<T>({
  columns,
  data,
  onRowClick,
  rowKey,
  rowClassName,
  className,
  style,
  emptyMessage = "No rows.",
  selectedRowKey,
  onSelectRow,
  isLoading = false,
  skeletonRowCount = 12,
}: Omit<GridListProps<T>, "gridId">) {
  const scrollRef = useRef<HTMLDivElement>(null);

  const { selectedCell, setSelectedCell } = useGridCellSelection({
    data,
    rowKey,
    getTable: () => scrollRef.current?.querySelector("table") ?? null,
  });

  // list-wrap(부모)의 높이를 매 렌더마다 직접 읽는다.
  // ResizeObserver 콜백은 비동기라 초기 렌더에서 높이가 0으로 잡히는 문제가 있어 교체.
  const [containerH, setContainerH] = useState(0);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useLayoutEffect(() => {
    const h = scrollRef.current?.parentElement?.clientHeight ?? 0;
    if (h > 0 && h !== containerH) setContainerH(h);
  });

  const rowVirtualizer = useVirtualizer({
    count: data.length,
    getScrollElement: () => scrollRef.current,
    estimateSize: () => ROW_HEIGHT_PX,
    overscan: 30,
  });
  const virtualRows = rowVirtualizer.getVirtualItems();
  const totalSize = rowVirtualizer.getTotalSize();
  const paddingTop = virtualRows.length > 0 ? virtualRows[0].start : 0;
  const paddingBottom =
    virtualRows.length > 0 ? totalSize - virtualRows[virtualRows.length - 1].end : 0;

  return (
    <div className={`grid-wrap${className ? ` ${className}` : ""}`} ref={scrollRef} style={{ ...style, overflowY: isLoading ? "hidden" : undefined }}>
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
                style={{ width: col.width ?? col.minWidth, textAlign: col.align }}
                className={col.isRequired ? "is-required" : undefined}
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {isLoading ? (
            Array.from(
              {
                length: containerH > 0
                  ? Math.ceil(containerH / ROW_HEIGHT_PX) + 10
                  : (skeletonRowCount ?? 20),
              },
              (_, i) => (
                <tr key={`skel-${i}`}>
                  {columns.map((col) => (
                    <td key={String(col.key)}>
                      <div className="h-3 w-full rounded animate-pulse" style={{ backgroundColor: "var(--surface-3)" }} />
                    </td>
                  ))}
                </tr>
              )
            )
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="grid__empty">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            <>
              {paddingTop > 0 && (
                <tr>
                  <td colSpan={columns.length} style={{ height: paddingTop, padding: 0 }} />
                </tr>
              )}
              {virtualRows.map((virtualRow) =>
                renderSingleRow(
                  data[virtualRow.index],
                  virtualRow.index,
                  columns,
                  rowKey,
                  rowClassName,
                  onRowClick,
                  selectedRowKey,
                  onSelectRow,
                  selectedCell,
                  setSelectedCell,
                  virtualRow.key,
                )
              )}
              {paddingBottom > 0 && (
                <tr>
                  <td colSpan={columns.length} style={{ height: paddingBottom, padding: 0 }} />
                </tr>
              )}
            </>
          )}
        </tbody>
      </table>
    </div>
  );
}
