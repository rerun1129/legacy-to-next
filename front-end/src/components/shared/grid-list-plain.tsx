"use client";

import { useCallback, useLayoutEffect, useRef, useState, type RefObject } from "react";
import { useVirtualizer } from "@tanstack/react-virtual";
import { GridListProps, ROW_HEIGHT_PX } from "./grid-list";
import { GridRow } from "./grid-list-row";
import { useGridCellSelection } from "@/lib/use-grid-cell-selection";
import { useStableOptionalCallback } from "@/lib/use-stable-callback";

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
  const tableRef = useRef<HTMLTableElement>(null);
  const selectionOverlayRef = useRef<HTMLDivElement>(null);
  const copiedOverlayRef = useRef<HTMLDivElement>(null);

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
    // 실제 DOM 높이로 측정해 추정값과의 누적 오차를 제거한다.
    measureElement: (el) => el?.getBoundingClientRect().height ?? ROW_HEIGHT_PX,
  });

  const getRowOffset = useCallback(
    (i: number) => {
      const m = rowVirtualizer.measurementsCache[i];
      return m ? { start: m.start, size: m.size } : null;
    },
    [rowVirtualizer],
  );

  // 호출처 인라인 콜백을 안정적인 참조로 감싸 GridRow의 React.memo 비교를 통과시킨다.
  // rowClassName은 extraClassName으로 직접 계산해 전달하므로 stable 래핑 불필요.
  const stableRowKey = useStableOptionalCallback(rowKey);
  const stableOnRowClick = useStableOptionalCallback(onRowClick);
  const stableOnSelectRow = useStableOptionalCallback(onSelectRow);

  // 드래그 중 focus 행이 변경될 때 onRowClick을 통해 외부 selected 상태를 갱신한다.
  const onActiveRowChange = useCallback((rk: string) => {
    if (!stableOnRowClick) return;
    for (let i = 0; i < data.length; i++) {
      const k = String(rowKey ? rowKey(data[i], i) : (data[i] as Record<string, unknown>).id ?? i);
      if (k === rk) { stableOnRowClick(data[i], i); return; }
    }
  }, [data, rowKey, stableOnRowClick]);

  const { handleTableMouseDown } = useGridCellSelection({
    data,
    rowKey,
    visibleColumns: columns,
    getTable: () => tableRef.current,
    overlayRef: selectionOverlayRef as RefObject<HTMLDivElement | null>,
    copiedOverlayRef: copiedOverlayRef as RefObject<HTMLDivElement | null>,
    rowHeight: ROW_HEIGHT_PX,
    getRowOffset,
    onActiveRowChange,
  });

  const virtualRows = rowVirtualizer.getVirtualItems();
  const totalSize = rowVirtualizer.getTotalSize();
  const paddingTop = virtualRows.length > 0 ? virtualRows[0].start : 0;
  const paddingBottom =
    virtualRows.length > 0 ? totalSize - virtualRows[virtualRows.length - 1].end : 0;

  return (
    <div className={`grid-wrap${className ? ` ${className}` : ""}`} ref={scrollRef} style={{ ...style, overflowY: isLoading ? "hidden" : undefined }}>
      <table ref={tableRef} className="grid--list" onMouseDown={handleTableMouseDown}>
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
              {virtualRows.map((virtualRow) => {
                const ri = virtualRow.index;
                return (
                  <GridRow<T>
                    key={String(virtualRow.key)}
                    row={data[ri]}
                    rowIndex={ri}
                    columns={columns}
                    rowKey={stableRowKey}
                    extraClassName={rowClassName?.(data[ri], ri) ?? undefined}
                    onRowClick={stableOnRowClick}
                    selectedRowKey={selectedRowKey}
                    onSelectRow={stableOnSelectRow}
                    measureRef={rowVirtualizer.measureElement}
                    dataIndex={ri}
                  />
                );
              })}
              {paddingBottom > 0 && (
                <tr>
                  <td colSpan={columns.length} style={{ height: paddingBottom, padding: 0 }} />
                </tr>
              )}
            </>
          )}
        </tbody>
      </table>
      <div ref={selectionOverlayRef} className="grid-selection-overlay" aria-hidden="true" />
      <div ref={copiedOverlayRef} className="grid-copied-overlay" aria-hidden="true" />
    </div>
  );
}
