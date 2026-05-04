"use client";

import React, { useEffect, useLayoutEffect, useRef, useState } from "react";
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
}

function renderRows<T>(
  columns: GridColumn<T>[],
  data: T[],
  rowKey: GridListProps<T>["rowKey"],
  rowClassName: GridListProps<T>["rowClassName"],
  onRowClick: GridListProps<T>["onRowClick"],
  emptyMessage: string | undefined,
  selectedRowKey?: string | number | null,
  onSelectRow?: (row: T | null, index: number | null) => void,
  selectedCell?: { rowKey: string | number; colKey: string } | null,
  setSelectedCell?: React.Dispatch<React.SetStateAction<{ rowKey: string | number; colKey: string } | null>>,
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
        key={key}
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
  emptyMessage = "No rows.",
  selectedRowKey,
  onSelectRow,
}: GridListProps<T> & { gridId: string }) {
  const { visibleColumns, resizeColumn, reorderColumn, hideColumn } =
    useColumnLayout(gridId, defaultColumns);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [isDragOutside, setIsDragOutside] = useState(false);
  const tableRef = useRef<HTMLTableElement>(null);
  const [selectedCell, setSelectedCell] = useState<{
    rowKey: string | number;
    colKey: string;
  } | null>(null);
  // rowKey prop은 호출처마다 매 렌더에 새 함수가 전달될 수 있으므로
  // ref로 보관해 useEffect 의존성 루프를 방지
  const rowKeyRef = useRef(rowKey);
  // 렌더 body에서 ref.current 직접 대입 금지 — useLayoutEffect로 동기 갱신
  useLayoutEffect(() => {
    rowKeyRef.current = rowKey;
  });

  useEffect(() => {
    if (selectedCell == null) return;
    const rk = rowKeyRef.current;
    const exists = data.some((row, i) => {
      const k = rk
        ? rk(row, i)
        : ((row as Record<string, unknown>).id as string | number | undefined) ?? i;
      return k === selectedCell.rowKey;
    });
    if (!exists) setSelectedCell(null);
  }, [data, selectedCell]);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );
  const ids = visibleColumns.map((c) => String(c.key));
  const activeCol = visibleColumns.find((c) => String(c.key) === activeId) ?? null;

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
    <div className={`grid-wrap${className ? ` ${className}` : ""}`} style={style}>
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
            {renderRows(visibleColumns, data, rowKey, rowClassName, onRowClick, emptyMessage, selectedRowKey, onSelectRow, selectedCell, setSelectedCell)}
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

function PlainGridList<T>({
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
}: Omit<GridListProps<T>, "gridId">) {
  const [selectedCell, setSelectedCell] = useState<{
    rowKey: string | number;
    colKey: string;
  } | null>(null);
  // rowKey prop은 호출처마다 매 렌더에 새 함수가 전달될 수 있으므로
  // ref로 보관해 useEffect 의존성 루프를 방지
  const rowKeyRef = useRef(rowKey);
  // 렌더 body에서 ref.current 직접 대입 금지 — useLayoutEffect로 동기 갱신
  useLayoutEffect(() => {
    rowKeyRef.current = rowKey;
  });

  useEffect(() => {
    if (selectedCell == null) return;
    const rk = rowKeyRef.current;
    const exists = data.some((row, i) => {
      const k = rk
        ? rk(row, i)
        : ((row as Record<string, unknown>).id as string | number | undefined) ?? i;
      return k === selectedCell.rowKey;
    });
    if (!exists) setSelectedCell(null);
  }, [data, selectedCell]);

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
          {renderRows(columns, data, rowKey, rowClassName, onRowClick, emptyMessage, selectedRowKey, onSelectRow, selectedCell, setSelectedCell)}
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
