"use client";

import React, { useRef, useState } from "react";
import {
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  PointerSensor,
  useSensor,
  useSensors,
  closestCenter,
} from "@dnd-kit/core";
import {
  SortableContext,
  horizontalListSortingStrategy,
  useSortable,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import type { GridColumn } from "./grid-list";

interface SortableThProps<T> {
  col: GridColumn<T>;
  onResize: (key: string, width: number) => void;
  isDraggingThis: boolean;
}

function SortableTh<T>({ col, onResize, isDraggingThis }: SortableThProps<T>) {
  const id = String(col.key);
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id });

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    width: col.width ?? col.minWidth,
    textAlign: col.align,
    position: "relative",
  };

  // resize handle: pointer 이벤트로 직접 처리, DnD와 충돌 방지
  const startXRef = useRef<number | null>(null);
  const startWidthRef = useRef<number>(col.width ?? col.minWidth ?? 80);

  function handleResizePointerDown(e: React.PointerEvent<HTMLSpanElement>) {
    e.stopPropagation(); // DnD drag 시작 차단
    e.preventDefault();
    startXRef.current = e.clientX;
    startWidthRef.current = col.width ?? col.minWidth ?? 80;

    const target = e.currentTarget;
    target.setPointerCapture(e.pointerId);

    function onPointerMove(ev: PointerEvent) {
      if (startXRef.current === null) return;
      const delta = ev.clientX - startXRef.current;
      const next = Math.max(40, startWidthRef.current + delta);
      onResize(id, next);
    }

    function onPointerUp() {
      startXRef.current = null;
      target.removeEventListener("pointermove", onPointerMove);
      target.removeEventListener("pointerup", onPointerUp);
    }

    target.addEventListener("pointermove", onPointerMove);
    target.addEventListener("pointerup", onPointerUp);
  }

  return (
    <th
      ref={setNodeRef}
      style={style}
      className={[
        col.required ? "is-required" : undefined,
        isDraggingThis ? "grid__th--dragging" : undefined,
      ]
        .filter(Boolean)
        .join(" ") || undefined}
      {...attributes}
      {...listeners}
    >
      {col.label}
      <span
        className="grid__resize-handle"
        onPointerDown={handleResizePointerDown}
      />
    </th>
  );
}

export interface GridListHeaderProps<T> {
  columns: GridColumn<T>[];
  onReorder: (activeKey: string, overKey: string) => void;
  onHide: (key: string) => void;
  onResize: (key: string, width: number) => void;
}

export function GridListHeader<T>({
  columns,
  onReorder,
  onHide,
  onResize,
}: GridListHeaderProps<T>) {
  const [activeId, setActiveId] = useState<string | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      // 5px 이상 움직여야 drag 시작 — resize handle 클릭과 구분
      activationConstraint: { distance: 5 },
    })
  );

  const ids = columns.map((c) => String(c.key));
  const activeCol = columns.find((c) => String(c.key) === activeId) ?? null;

  function handleDragStart(event: DragStartEvent) {
    setActiveId(String(event.active.id));
  }

  function handleDragEnd(event: DragEndEvent) {
    setActiveId(null);
    const { active, over } = event;
    if (over == null) {
      // drag-out: 드롭 대상 없음 → 컬럼 숨김
      onHide(String(active.id));
    } else if (active.id !== over.id) {
      onReorder(String(active.id), String(over.id));
    }
  }

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      <thead>
        <SortableContext items={ids} strategy={horizontalListSortingStrategy}>
          <tr>
            {columns.map((col) => (
              <SortableTh<T>
                key={String(col.key)}
                col={col}
                onResize={onResize}
                isDraggingThis={activeId === String(col.key)}
              />
            ))}
          </tr>
        </SortableContext>
      </thead>
      <DragOverlay>
        {activeCol ? (
          <div className="grid__drag-overlay">{activeCol.label}</div>
        ) : null}
      </DragOverlay>
    </DndContext>
  );
}
