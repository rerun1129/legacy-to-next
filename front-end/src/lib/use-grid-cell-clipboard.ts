"use client";

import { useEffect } from "react";
import { type CellRange, serializeRange } from "./grid-cell-range-utils";

export function useGridCellClipboard<T>(params: {
  selectedRangeRef: React.MutableRefObject<CellRange | null>;
  copiedRangeRef: React.MutableRefObject<CellRange | null>;
  applyCopiedOverlay: () => void;
  dataRef: React.MutableRefObject<T[]>;
  visibleColumnsRef: React.MutableRefObject<{ key: string | number | symbol; width?: number; minWidth?: number }[]>;
  rowKeyRef: React.MutableRefObject<((row: T, i: number) => string | number) | undefined>;
  getTableRef: React.MutableRefObject<() => HTMLTableElement | null>;
}): void {
  const {
    selectedRangeRef,
    copiedRangeRef,
    applyCopiedOverlay,
    dataRef,
    visibleColumnsRef,
    rowKeyRef,
    getTableRef,
  } = params;

  // Ctrl+C: 직렬화 + marching ants 표시
  useEffect(() => {
    function handleCopy(e: KeyboardEvent) {
      if (!(e.ctrlKey || e.metaKey) || e.key.toLowerCase() !== "c") return;
      const active = document.activeElement;
      if (
        active &&
        (active.tagName === "INPUT" ||
          active.tagName === "TEXTAREA" ||
          (active as HTMLElement).isContentEditable)
      )
        return;
      if (window.getSelection()?.toString()) return;
      const range = selectedRangeRef.current;
      if (!range) return;
      const text = serializeRange(
        range,
        dataRef.current,
        visibleColumnsRef.current,
        rowKeyRef.current as ((row: T, i: number) => string | number) | undefined,
        getTableRef.current,
      );
      if (!text) return;
      e.preventDefault();
      // 의도된 fire-and-forget: 클립보드 쓰기 실패는 UX에 영향이 없으므로 무시
      navigator.clipboard.writeText(text).catch(() => {});
      // 복사된 영역을 marching ants overlay로 표시 (재복사 시 새 영역으로 갱신)
      copiedRangeRef.current = range;
      applyCopiedOverlay();
    }
    window.addEventListener("keydown", handleCopy);
    return () => window.removeEventListener("keydown", handleCopy);
  }, [applyCopiedOverlay, copiedRangeRef, dataRef, getTableRef, rowKeyRef, selectedRangeRef, visibleColumnsRef]);

  // Esc: marching ants 해제
  useEffect(() => {
    function handleEsc(e: KeyboardEvent) {
      if (e.key !== "Escape") return;
      if (copiedRangeRef.current) {
        copiedRangeRef.current = null;
        applyCopiedOverlay();
      }
    }
    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [applyCopiedOverlay, copiedRangeRef]);
}
