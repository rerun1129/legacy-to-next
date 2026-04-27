"use client";

import React, { useRef, useState } from "react";
import {
  DndContext,
  type DragEndEvent,
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
}

function renderRows<T>(
  columns: GridColumn<T>[],
  data: T[],
  rowKey: GridListProps<T>["rowKey"],
  rowClassName: GridListProps<T>["rowClassName"],
  onRowClick: GridListProps<T>["onRowClick"],
  emptyMessage: string | undefined,
): React.ReactNode {
  if (data.length === 0) {
    return (
      <tr>
        <td colSpan={columns.length} className="grid__empty">
          {emptyMessage}
        </td>
      </tr>
    );
  }
  return data.map((row, rowIndex) => {
    // rowKey 미제공 시 row.id → rowIndex 순으로 폴백.
    // rowIndex는 이 그리드가 서버 페이지 고정 순서 데이터를 표시할 때만 허용됨(재정렬·삽입 없음).
    const key = rowKey
      ? rowKey(row, rowIndex)
      : ((row as Record<string, unknown>).id as string | number | undefined) ?? rowIndex;
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
              {col.render ? col.render(rawValue, row, rowIndex) : String(rawValue ?? "")}
            </td>
          );
        })}
      </tr>
    );
  });
}

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
  const [activeId, setActiveId] = useState<string | null>(null);
  const tableRef = useRef<HTMLTableElement>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );
  const ids = visibleColumns.map((c) => String(c.key));
  const activeCol = visibleColumns.find((c) => String(c.key) === activeId) ?? null;

  function handleDragStart(event: DragStartEvent) {
    setActiveId(String(event.active.id));
  }

  function handleDragEnd(event: DragEndEvent) {
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
    <div className={`grid-wrap${className ? ` ${className}` : ""}`} style={style}>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
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
            {renderRows(visibleColumns, data, rowKey, rowClassName, onRowClick, emptyMessage)}
          </tbody>
        </table>
        <DragOverlay>
          {activeCol ? (
            <div className="grid__drag-overlay">{activeCol.label}</div>
          ) : null}
        </DragOverlay>
      </DndContext>
    </div>
  );
}

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
                style={{ width: col.width ?? col.minWidth, textAlign: col.align }}
                className={col.isRequired ? "is-required" : undefined}
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {renderRows(columns, data, rowKey, rowClassName, onRowClick, emptyMessage)}
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
