"use client";

import { useEffect, useRef, useState } from "react";

interface DragState {
  startX: number;
  startY: number;
  baseX: number;
  baseY: number;
}

interface UseModalDragResult {
  offset: { x: number; y: number };
  onHeaderMouseDown: (e: React.MouseEvent<HTMLDivElement>) => void;
}

/**
 * 모달 헤더(타이틀 영역)를 잡고 드래그하여 모달을 자유롭게 이동.
 * - 전역 mousemove/mouseup 리스너로 헤더 밖으로 마우스가 빠져도 드래그 유지
 * - 헤더 내부 <button> 클릭은 드래그로 인식하지 않음
 */
export function useModalDrag(): UseModalDragResult {
  const [offset, setOffset] = useState({ x: 0, y: 0 });
  const dragRef = useRef<DragState | null>(null);

  useEffect(() => {
    function onMouseMove(e: MouseEvent) {
      const s = dragRef.current;
      if (!s) return;
      setOffset({
        x: s.baseX + (e.clientX - s.startX),
        y: s.baseY + (e.clientY - s.startY),
      });
    }
    function onMouseUp() {
      dragRef.current = null;
    }
    document.addEventListener("mousemove", onMouseMove);
    document.addEventListener("mouseup", onMouseUp);
    return () => {
      document.removeEventListener("mousemove", onMouseMove);
      document.removeEventListener("mouseup", onMouseUp);
    };
  }, []);

  function onHeaderMouseDown(e: React.MouseEvent<HTMLDivElement>) {
    if ((e.target as HTMLElement).closest("button")) return;
    dragRef.current = {
      startX: e.clientX,
      startY: e.clientY,
      baseX: offset.x,
      baseY: offset.y,
    };
  }

  return { offset, onHeaderMouseDown };
}
