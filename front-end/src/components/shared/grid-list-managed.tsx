"use client";

import { useLayoutEffect, useRef, useState } from "react";
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
import { GridListProps, ROW_HEIGHT_PX, renderSingleRow } from "./grid-list";
import { useGridCellSelection } from "@/lib/use-grid-cell-selection";

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
  isLoading = false,
  skeletonRowCount = 12,
}: GridListProps<T> & { gridId: string }) {
  const { visibleColumns, resizeColumn, reorderColumn, hideColumn } =
    useColumnLayout(gridId, defaultColumns);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [isDragOutside, setIsDragOutside] = useState(false);
  const tableRef = useRef<HTMLTableElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);

  const { selectedCell, setSelectedCell } = useGridCellSelection({
    data,
    rowKey,
    getTable: () => tableRef.current,
  });

  // list-wrap(부모)의 높이를 매 렌더마다 직접 읽는다.
  // ResizeObserver 콜백은 비동기라 초기 렌더에서 높이가 0으로 잡히는 문제가 있어 교체.
  const [containerH, setContainerH] = useState(0);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useLayoutEffect(() => {
    const h = scrollRef.current?.parentElement?.clientHeight ?? 0;
    if (h > 0 && h !== containerH) setContainerH(h);
  });

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );
  const ids = visibleColumns.map((c) => String(c.key));
  const activeCol = visibleColumns.find((c) => String(c.key) === activeId) ?? null;

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
        <table ref={tableRef} className="grid--list">
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
                {virtualRows.map((virtualRow) =>
                  renderSingleRow(
                    data[virtualRow.index],
                    virtualRow.index,
                    visibleColumns,
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
                    <td colSpan={visibleColumns.length} style={{ height: paddingBottom, padding: 0 }} />
                  </tr>
                )}
              </>
            )}
          </tbody>
        </table>
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
