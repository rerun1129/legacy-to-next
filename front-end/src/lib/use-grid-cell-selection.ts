"use client";

import { useCallback, useEffect, useLayoutEffect, useRef } from "react";
import {
  type CellRange,
  type CellRef,
  resolveRowKeyAsString,
  serializeRange,
} from "./grid-cell-range-utils";

export type { CellRange, CellRef };

interface VisibleCol {
  key: string | number | symbol;
  width?: number;
  minWidth?: number;
}

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
}): {
  handleTableMouseDown: (e: React.MouseEvent<HTMLElement>) => void;
} {
  const { data, rowKey, visibleColumns, getTable, overlayRef, copiedOverlayRef, rowHeight } =
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
  const overlayRefStore = useRef(overlayRef);
  useLayoutEffect(() => {
    overlayRefStore.current = overlayRef;
  });
  const copiedOverlayRefStore = useRef(copiedOverlayRef);
  useLayoutEffect(() => {
    copiedOverlayRefStore.current = copiedOverlayRef;
  });

  const selectedRangeRef = useRef<CellRange | null>(null);
  const copiedRangeRef = useRef<CellRange | null>(null);

  /** range → overlay div 위치/크기를 계산해 imperative하게 갱신하는 공통 헬퍼. */
  const applyOverlayToEl = useCallback(
    (overlayEl: HTMLDivElement | null, range: CellRange | null) => {
      if (!overlayEl) return;
      if (!range) {
        overlayEl.style.display = "none";
        return;
      }
      const curData = dataRef.current;
      const cols = visibleColumnsRef.current;
      const rk = rowKeyRef.current;

      let aR = -1;
      let fR = -1;
      for (let i = 0; i < curData.length; i++) {
        const k = resolveRowKeyAsString(curData[i], i, rk);
        if (k === range.anchor.rowKey) aR = i;
        if (k === range.focus.rowKey) fR = i;
        if (aR !== -1 && fR !== -1) break;
      }
      if (aR < 0 || fR < 0) {
        overlayEl.style.display = "none";
        return;
      }

      let aC = -1;
      let fC = -1;
      for (let i = 0; i < cols.length; i++) {
        const ck = String(cols[i].key);
        if (ck === range.anchor.colKey) aC = i;
        if (ck === range.focus.colKey) fC = i;
      }
      if (aC < 0 || fC < 0) {
        overlayEl.style.display = "none";
        return;
      }

      const minRow = Math.min(aR, fR);
      const maxRow = Math.max(aR, fR);
      const minCol = Math.min(aC, fC);
      const maxCol = Math.max(aC, fC);

      let left = 0;
      for (let i = 0; i < minCol; i++) {
        left += cols[i].width ?? cols[i].minWidth ?? 80;
      }
      let width = 0;
      for (let i = minCol; i <= maxCol; i++) {
        width += cols[i].width ?? cols[i].minWidth ?? 80;
      }

      const table = getTableRef.current();
      const thead = table?.querySelector("thead") as HTMLElement | null;
      const theadHeight = thead?.offsetHeight ?? 0;
      // table이 grid-wrap 기준 정확히 0이 아닐 수 있으므로 offsetTop/Left 포함
      const tableOffsetTop = table?.offsetTop ?? 0;
      const tableOffsetLeft = table?.offsetLeft ?? 0;
      const top = tableOffsetTop + theadHeight + rowHeightRef.current * minRow;
      const height = rowHeightRef.current * (maxRow - minRow + 1);
      left += tableOffsetLeft;

      overlayEl.style.display = "block";
      overlayEl.style.transform = `translate3d(${left}px, ${top}px, 0)`;
      overlayEl.style.width = `${width}px`;
      overlayEl.style.height = `${height}px`;
    },
    [],
  );

  const applyOverlay = useCallback(() => {
    applyOverlayToEl(overlayRefStore.current.current, selectedRangeRef.current);
  }, [applyOverlayToEl]);

  const applyCopiedOverlay = useCallback(() => {
    const overlayEl = copiedOverlayRefStore.current.current;
    applyOverlayToEl(overlayEl, copiedRangeRef.current);
    // display:block이 됐다면 pulse animation을 처음부터 재시작
    if (overlayEl && overlayEl.style.display !== "none") {
      overlayEl.style.animation = "none";
      void overlayEl.offsetHeight; // reflow로 animation 리셋
      overlayEl.style.animation = "";
    }
  }, [applyOverlayToEl]);

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
  }, [applyCopiedOverlay]);

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
  }, [applyCopiedOverlay]);

  const pendingRef = useRef<{
    anchor: CellRef;
    startX: number;
    startY: number;
    didDrag: boolean;
  } | null>(null);

  const handleTableMouseDown = useCallback(
    (e: React.MouseEvent<HTMLElement>) => {
      if (e.button !== 0) return;
      const td = (e.target as HTMLElement).closest(
        "td[data-row-key][data-col-key]",
      ) as HTMLTableCellElement | null;
      if (!td) return;

      const cell: CellRef = { rowKey: td.dataset.rowKey!, colKey: td.dataset.colKey! };

      // Shift+클릭: 영역 확장
      if (e.shiftKey && selectedRangeRef.current) {
        selectedRangeRef.current = {
          anchor: selectedRangeRef.current.anchor,
          focus: cell,
        };
        applyOverlay();
        return;
      }

      const prev = selectedRangeRef.current;
      const wasSameSingle =
        prev != null &&
        prev.anchor.rowKey === cell.rowKey &&
        prev.anchor.colKey === cell.colKey &&
        prev.focus.rowKey === cell.rowKey &&
        prev.focus.colKey === cell.colKey;

      // mousedown 즉시 새 셀로 변경 → overlay 즉시 표시 (즉각적 시각 피드백)
      if (!wasSameSingle) {
        selectedRangeRef.current = { anchor: cell, focus: cell };
        applyOverlay();
      }

      pendingRef.current = {
        anchor: cell,
        startX: e.clientX,
        startY: e.clientY,
        didDrag: false,
      };

      let lastFocusRowKey = cell.rowKey;
      let lastFocusColKey = cell.colKey;
      let rafId: number | null = null;
      let pendingEv: MouseEvent | null = null;

      function processMove() {
        rafId = null;
        const ev = pendingEv;
        pendingEv = null;
        if (!ev) return;
        const pd = pendingRef.current;
        if (!pd) return;

        if (!pd.didDrag) {
          if (Math.hypot(ev.clientX - pd.startX, ev.clientY - pd.startY) < 3) return;
          pd.didDrag = true;
          if (wasSameSingle) {
            selectedRangeRef.current = { anchor: pd.anchor, focus: pd.anchor };
            applyOverlay();
          }
        }
        const el = document.elementFromPoint(ev.clientX, ev.clientY);
        const moveTd = el?.closest(
          "td[data-row-key][data-col-key]",
        ) as HTMLTableCellElement | null;
        if (!moveTd) return;
        const newRowKey = moveTd.dataset.rowKey!;
        const newColKey = moveTd.dataset.colKey!;
        if (newRowKey === lastFocusRowKey && newColKey === lastFocusColKey) return;
        lastFocusRowKey = newRowKey;
        lastFocusColKey = newColKey;
        const cur = selectedRangeRef.current;
        if (cur) {
          selectedRangeRef.current = {
            anchor: cur.anchor,
            focus: { rowKey: newRowKey, colKey: newColKey },
          };
          applyOverlay();
        }
      }

      function handleMouseMove(ev: MouseEvent) {
        pendingEv = ev;
        if (rafId !== null) return;
        rafId = requestAnimationFrame(processMove);
      }

      function handleMouseUp() {
        window.removeEventListener("mousemove", handleMouseMove);
        window.removeEventListener("mouseup", handleMouseUp);
        if (rafId !== null) {
          cancelAnimationFrame(rafId);
          rafId = null;
        }
        pendingEv = null;
        const pd = pendingRef.current;
        if (!pd) return;
        pendingRef.current = null;
        if (!pd.didDrag && wasSameSingle) {
          selectedRangeRef.current = null;
          applyOverlay();
        }
      }

      window.addEventListener("mousemove", handleMouseMove);
      window.addEventListener("mouseup", handleMouseUp);
    },
    [applyOverlay],
  );

  return { handleTableMouseDown };
}
