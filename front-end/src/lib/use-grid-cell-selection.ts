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
  /** 그리드 외부 클릭 시 selection이 해제된 후 호출된다. 외부 행 highlight 해제에 사용한다. */
  onClearActiveRow?: () => void;
}): {
  handleTableMouseDown: (e: React.MouseEvent<HTMLElement>) => void;
} {
  const { data, rowKey, visibleColumns, getTable, overlayRef, copiedOverlayRef, rowHeight, getRowOffset, onActiveRowChange, onClearActiveRow } =
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
  const onClearActiveRowRef = useRef<(() => void) | undefined>(onClearActiveRow);
  useLayoutEffect(() => {
    onClearActiveRowRef.current = onClearActiveRow;
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

  // 컬럼 리사이즈로 table 너비가 바뀔 때 overlay 좌표를 즉시 보정한다.
  useEffect(() => {
    const el = getTableRef.current();
    if (!el) return;
    const observer = new ResizeObserver(() => {
      if (selectedRangeRef.current !== null) {
        applyOverlay();
        applyCopiedOverlay();
      }
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, [applyOverlay, applyCopiedOverlay]);

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

  // 그리드 테이블 영역 외부 클릭 시 selection 및 copied overlay를 초기화한다.
  // 한 화면에 그리드가 여러 개일 때 다른 그리드 클릭으로 이 그리드의 selection이 해제되어야 한다.
  useEffect(() => {
    function handleOutsideClick(e: MouseEvent) {
      const tableEl = getTableRef.current();
      if (tableEl?.contains(e.target as Node)) return;
      selectedRangeRef.current = null;
      copiedRangeRef.current = null;
      applyOverlay();
      applyCopiedOverlay();
      onClearActiveRowRef.current?.();
    }
    document.addEventListener("mousedown", handleOutsideClick);
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, [applyOverlay, applyCopiedOverlay]);

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
