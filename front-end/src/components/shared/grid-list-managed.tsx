"use client";

import { useCallback, useLayoutEffect, useRef, useState, type RefObject } from "react";
import { useVirtualizer } from "@tanstack/react-virtual";
import {
  DndContext,
  type DragEndEvent,
  type DragMoveEvent,
  type DragStartEvent,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  closestCenter,
} from "@dnd-kit/core";
import { SortableContext, horizontalListSortingStrategy } from "@dnd-kit/sortable";
import { useColumnLayout } from "@/lib/use-column-layout";
import { SortableTh } from "./grid-list-header";
import { GridListProps, ROW_HEIGHT_PX } from "./grid-list";
import { GridRow } from "./grid-list-row";
import { useGridCellSelection } from "@/lib/use-grid-cell-selection";
import { useStableOptionalCallback } from "@/lib/use-stable-callback";

export function ManagedGridList<T>({
  gridId,
  columns: defaultColumns,
  data,
  onRowClick,
  rowKey,
  rowClassName,
  className,
  style,
  emptyMessage = "No rows.",
  selectedRowKey,
  onSelectRow,
  onClearRow,
  isLoading = false,
  skeletonRowCount = 12,
}: GridListProps<T> & { gridId: string }) {
  const { visibleColumns, resizeColumn, reorderColumn, hideColumn } =
    useColumnLayout(gridId, defaultColumns);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [isDragOutside, setIsDragOutside] = useState(false);
  const tableRef = useRef<HTMLTableElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);
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
    // index 기반 default key는 행 삭제 시 같은 위치에 다른 row가 들어와
    // uncontrolled input의 DOM value가 stale로 남는 버그를 유발한다.
    getItemKey: (index) => {
      if (rowKey) {
        try { return String(rowKey(data[index], index)); } catch { return index; }
      }
      const idVal = (data[index] as Record<string, unknown> | undefined)?.id;
      return idVal != null ? String(idVal) : index;
    },
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
  const stableOnClearRow = useStableOptionalCallback(onClearRow);

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
    visibleColumns,
    getTable: () => tableRef.current,
    overlayRef: selectionOverlayRef as RefObject<HTMLDivElement | null>,
    copiedOverlayRef: copiedOverlayRef as RefObject<HTMLDivElement | null>,
    rowHeight: ROW_HEIGHT_PX,
    getRowOffset,
    onActiveRowChange,
    onClearActiveRow: () => { stableOnClearRow?.(); },
  });

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );
  const ids = visibleColumns.map((c) => String(c.key));
  const activeCol = visibleColumns.find((c) => String(c.key) === activeId) ?? null;

  const virtualRows = rowVirtualizer.getVirtualItems();
  const totalSize = rowVirtualizer.getTotalSize();
  const paddingTop = virtualRows.length > 0 ? virtualRows[0].start : 0;
  const paddingBottom =
    virtualRows.length > 0 ? totalSize - virtualRows[virtualRows.length - 1].end : 0;

  function isOutsideTable(rect: { left: number; right: number; top: number; bottom: number }) {
    const tableEl = tableRef.current;
    if (!tableEl) return false;
    const t = tableEl.getBoundingClientRect();
    return rect.right < t.left || rect.left > t.right || rect.bottom < t.top || rect.top > t.bottom;
  }

  function handleDragStart(event: DragStartEvent) {
    setActiveId(String(event.active.id));
    setIsDragOutside(false);
  }

  function handleDragMove(event: DragMoveEvent) {
    const rect = event.active.rect.current.translated;
    setIsDragOutside(rect ? isOutsideTable(rect) : false);
  }

  function handleDragEnd(event: DragEndEvent) {
    setIsDragOutside(false);
    setActiveId(null);
    const { active, over } = event;

    // drag-out: 드래그 요소의 최종 위치가 테이블 영역 밖이면 컬럼 숨김
    const translatedRect = active.rect.current.translated;
    const tableEl = tableRef.current;
    if (tableEl && translatedRect) {
      const tableRect = tableEl.getBoundingClientRect();
      const isOutside =
        translatedRect.right < tableRect.left ||
        translatedRect.left > tableRect.right ||
        translatedRect.bottom < tableRect.top ||
        translatedRect.top > tableRect.bottom;
      if (isOutside) {
        hideColumn(String(active.id));
        return;
      }
    }

    if (over && active.id !== over.id) {
      reorderColumn(String(active.id), String(over.id));
    }
  }

  return (
    <div className={`grid-wrap${className ? ` ${className}` : ""}`} ref={scrollRef} style={{ ...style, overflowY: isLoading ? "hidden" : undefined }}>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragMove={handleDragMove}
        onDragEnd={handleDragEnd}
      >
        <table ref={tableRef} className="grid--list" onMouseDown={handleTableMouseDown}>
          <colgroup>
            {visibleColumns.map((col) => (
              <col key={String(col.key)} style={{ width: col.width ?? col.minWidth }} />
            ))}
          </colgroup>
          <thead>
            <SortableContext items={ids} strategy={horizontalListSortingStrategy}>
              <tr>
                {visibleColumns.map((col) => (
                  <SortableTh<T>
                    key={String(col.key)}
                    col={col}
                    onResize={resizeColumn}
                    isDraggingThis={activeId === String(col.key)}
                  />
                ))}
              </tr>
            </SortableContext>
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
                    {visibleColumns.map((col) => (
                      <td key={String(col.key)}>
                        <div className="h-3 w-full rounded animate-pulse" style={{ backgroundColor: "var(--surface-3)" }} />
                      </td>
                    ))}
                  </tr>
                )
              )
            ) : data.length === 0 ? (
              <tr>
                <td colSpan={visibleColumns.length} className="grid__empty">
                  {emptyMessage}
                </td>
              </tr>
            ) : (
              <>
                {paddingTop > 0 && (
                  <tr>
                    <td colSpan={visibleColumns.length} style={{ height: paddingTop, padding: 0 }} />
                  </tr>
                )}
                {virtualRows.map((virtualRow) => {
                  const ri = virtualRow.index;
                  return (
                    <GridRow<T>
                      key={String(virtualRow.key)}
                      row={data[ri]}
                      rowIndex={ri}
                      columns={visibleColumns}
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
                    <td colSpan={visibleColumns.length} style={{ height: paddingBottom, padding: 0 }} />
                  </tr>
                )}
              </>
            )}
          </tbody>
        </table>
        <div ref={selectionOverlayRef} className="grid-selection-overlay" aria-hidden="true" />
        <div ref={copiedOverlayRef} className="grid-copied-overlay" aria-hidden="true" />
        <DragOverlay>
          {activeCol ? (
            <div className={`grid__drag-overlay${isDragOutside ? " grid__drag-overlay--remove" : ""}`}>
              {isDragOutside && (
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true" style={{ flexShrink: 0 }}>
                  <polyline points="3 6 5 6 21 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                  <path d="M19 6l-1 14H6L5 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                  <path d="M10 11v6M14 11v6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                  <path d="M9 6V4h6v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              )}
              {activeCol.label}
            </div>
          ) : null}
        </DragOverlay>
      </DndContext>
    </div>
  );
}
