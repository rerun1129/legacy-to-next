"use client";

import { useEffect, useLayoutEffect, useRef, useState } from "react";

export function useGridCellSelection<T>(params: {
  data: T[];
  rowKey: ((row: T, index: number) => string | number) | undefined;
  getTable: () => HTMLTableElement | null;
}): {
  selectedCell: { rowKey: string | number; colKey: string } | null;
  setSelectedCell: React.Dispatch<
    React.SetStateAction<{ rowKey: string | number; colKey: string } | null>
  >;
} {
  const { data, rowKey, getTable } = params;

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

  // getTable도 매 렌더마다 새 함수가 전달될 수 있으므로 ref로 보관
  const getTableRef = useRef(getTable);
  useLayoutEffect(() => {
    getTableRef.current = getTable;
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
      if (!selectedCell) return;
      const table = getTableRef.current();
      const td = table?.querySelector(
        `td[data-row-key="${CSS.escape(String(selectedCell.rowKey))}"][data-col-key="${CSS.escape(selectedCell.colKey)}"]`
      ) as HTMLTableCellElement | null;
      if (!td) return;
      const text = td.textContent ?? "";
      e.preventDefault();
      // 의도된 fire-and-forget: 클립보드 쓰기 실패는 UX에 영향이 없으므로 무시
      navigator.clipboard.writeText(text).catch(() => {});
    }
    window.addEventListener("keydown", handleCopy);
    return () => window.removeEventListener("keydown", handleCopy);
  }, [selectedCell]);

  return { selectedCell, setSelectedCell };
}
