"use client";

import { useCallback, useRef } from "react";
import { type CellRange, type CellRef } from "./grid-cell-range-utils";

export function useGridCellDrag(params: {
  selectedRangeRef: React.MutableRefObject<CellRange | null>;
  applyOverlay: () => void;
  onActiveRowChangeRef: React.MutableRefObject<((rowKey: string) => void) | undefined>;
}): { handleTableMouseDown: (e: React.MouseEvent<HTMLElement>) => void } {
  const { selectedRangeRef, applyOverlay, onActiveRowChangeRef } = params;

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

      // mousedown 시점의 행을 즉시 외부에 알린다.
      onActiveRowChangeRef.current?.(cell.rowKey);

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
        // 드래그 중 행이 바뀔 때마다 외부 selected 상태를 갱신한다.
        if (newRowKey !== cell.rowKey) {
          onActiveRowChangeRef.current?.(newRowKey);
        }
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
    [applyOverlay, selectedRangeRef],
  );

  return { handleTableMouseDown };
}
