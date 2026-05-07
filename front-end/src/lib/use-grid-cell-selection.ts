"use client";

import { useCallback, useEffect, useLayoutEffect, useRef } from "react";
import { type CellRange, type CellRef, resolveRowKeyAsString } from "./grid-cell-range-utils";
import { applyOverlayPosition, type VisibleCol } from "./grid-cell-overlay-position";
import { useGridCellClipboard } from "./use-grid-cell-clipboard";
import { useGridCellDrag } from "./use-grid-cell-drag";

export type { CellRange, CellRef, VisibleCol };

/**
 * 셀 선택을 React state 대신 ref + imperative DOM 업데이트로 처리한다.
 * mousemove마다 단일 overlay div의 transform/width/height만 갱신하므로
 * React 재렌더링이 전혀 발생하지 않아 큰 그리드에서도 즉각적이다.
 */
export function useGridCellSelection<T>(params: {
  data: T[];
  rowKey: ((row: T, index: number) => string | number) | undefined;
  visibleColumns: VisibleCol[];
  getTable: () => HTMLTableElement | null;
  overlayRef: React.RefObject<HTMLDivElement | null>;
  copiedOverlayRef: React.RefObject<HTMLDivElement | null>;
  rowHeight: number;
  /** virtualizer의 실측 row 오프셋을 제공해 누적 어긋남을 방지한다. fallback은 rowHeight 고정값. */
  getRowOffset?: (index: number) => { start: number; size: number } | null;
  /** 드래그 중 focus 행이 바뀔 때마다 호출된다. 외부 selected 상태 갱신에 사용한다. */
  onActiveRowChange?: (rowKey: string) => void;
}): {
  handleTableMouseDown: (e: React.MouseEvent<HTMLElement>) => void;
} {
  const { data, rowKey, visibleColumns, getTable, overlayRef, copiedOverlayRef, rowHeight, getRowOffset, onActiveRowChange } =
    params;

  const rowKeyRef = useRef(rowKey);
  useLayoutEffect(() => {
    rowKeyRef.current = rowKey;
  });
  const getTableRef = useRef(getTable);
  useLayoutEffect(() => {
    getTableRef.current = getTable;
  });
  const visibleColumnsRef = useRef(visibleColumns);
  useLayoutEffect(() => {
    visibleColumnsRef.current = visibleColumns;
  });
  const dataRef = useRef(data);
  useLayoutEffect(() => {
    dataRef.current = data;
  });
  const rowHeightRef = useRef(rowHeight);
  useLayoutEffect(() => {
    rowHeightRef.current = rowHeight;
  });
  const getRowOffsetRef = useRef(getRowOffset);
  useLayoutEffect(() => {
    getRowOffsetRef.current = getRowOffset;
  });
  const overlayRefStore = useRef(overlayRef);
  useLayoutEffect(() => {
    overlayRefStore.current = overlayRef;
  });
  const copiedOverlayRefStore = useRef(copiedOverlayRef);
  useLayoutEffect(() => {
    copiedOverlayRefStore.current = copiedOverlayRef;
  });
  const onActiveRowChangeRef = useRef<((rowKey: string) => void) | undefined>(onActiveRowChange);
  useLayoutEffect(() => {
    onActiveRowChangeRef.current = onActiveRowChange;
  });

  const selectedRangeRef = useRef<CellRange | null>(null);
  const copiedRangeRef = useRef<CellRange | null>(null);

  const applyOverlay = useCallback(() => {
    applyOverlayPosition(overlayRefStore.current.current, selectedRangeRef.current, {
      data: dataRef.current,
      cols: visibleColumnsRef.current,
      rowKey: rowKeyRef.current,
      getTable: getTableRef.current,
      rowHeight: rowHeightRef.current,
      getRowOffset: getRowOffsetRef.current,
    });
  }, []);

  const applyCopiedOverlay = useCallback(() => {
    const overlayEl = copiedOverlayRefStore.current.current;
    applyOverlayPosition(overlayEl, copiedRangeRef.current, {
      data: dataRef.current,
      cols: visibleColumnsRef.current,
      rowKey: rowKeyRef.current,
      getTable: getTableRef.current,
      rowHeight: rowHeightRef.current,
      getRowOffset: getRowOffsetRef.current,
    });
    // display:block이 됐다면 pulse animation을 처음부터 재시작
    if (overlayEl && overlayEl.style.display !== "none") {
      overlayEl.style.animation = "none";
      void overlayEl.offsetHeight; // reflow로 animation 리셋
      overlayEl.style.animation = "";
    }
  }, []);

  // data/columns 변경 시 stale 정리 + 두 overlay 좌표 갱신 (resize/reorder 포함)
  useEffect(() => {
    const curData = dataRef.current;
    const rk = rowKeyRef.current;
    const cols = visibleColumnsRef.current;

    const rowKeys = new Set<string>();
    for (let i = 0; i < curData.length; i++) {
      rowKeys.add(resolveRowKeyAsString(curData[i], i, rk));
    }
    const colKeys = new Set<string>();
    for (let i = 0; i < cols.length; i++) {
      colKeys.add(String(cols[i].key));
    }

    function isStale(range: CellRange | null): boolean {
      if (!range) return false;
      return (
        !rowKeys.has(range.anchor.rowKey) ||
        !rowKeys.has(range.focus.rowKey) ||
        !colKeys.has(range.anchor.colKey) ||
        !colKeys.has(range.focus.colKey)
      );
    }

    if (isStale(selectedRangeRef.current)) selectedRangeRef.current = null;
    if (isStale(copiedRangeRef.current)) copiedRangeRef.current = null;

    applyOverlay();
    applyCopiedOverlay();
  }, [data, visibleColumns, applyOverlay, applyCopiedOverlay]);

  useGridCellClipboard({
    selectedRangeRef,
    copiedRangeRef,
    applyCopiedOverlay,
    dataRef,
    visibleColumnsRef,
    rowKeyRef,
    getTableRef,
  });

  const { handleTableMouseDown } = useGridCellDrag({
    selectedRangeRef,
    applyOverlay,
    onActiveRowChangeRef,
  });

  return { handleTableMouseDown };
}
