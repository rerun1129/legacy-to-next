"use client";

import React, { useRef } from "react";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import type { GridColumn } from "./grid-list";

interface SortableThProps<T> {
  col: GridColumn<T>;
  onResize: (key: string, width: number) => void;
  isDraggingThis: boolean;
}

export function SortableTh<T>({ col, onResize, isDraggingThis }: SortableThProps<T>) {
  const id = String(col.key);
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id });

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    width: col.width ?? col.minWidth,
    textAlign: col.align,
    position: "relative",
  };

  const startXRef = useRef<number | null>(null);
  const startWidthRef = useRef<number>(col.width ?? col.minWidth ?? 80);

  function handleResizePointerDown(e: React.PointerEvent<HTMLSpanElement>) {
    e.stopPropagation();
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
      target.removeEventListener("pointercancel", onPointerUp);
    }

    target.addEventListener("pointermove", onPointerMove);
    target.addEventListener("pointerup", onPointerUp);
    target.addEventListener("pointercancel", onPointerUp);
  }

  return (
    <th
      ref={setNodeRef}
      style={style}
      suppressHydrationWarning
      className={
        [
          col.isRequired ? "is-required" : undefined,
          isDraggingThis ? "grid__th--dragging" : undefined,
        ]
          .filter(Boolean)
          .join(" ") || undefined
      }
      {...attributes}
      {...listeners}
    >
      {col.label}
      <span className="grid__resize-handle" onPointerDown={handleResizePointerDown} />
    </th>
  );
}
