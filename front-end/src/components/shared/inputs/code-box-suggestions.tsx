"use client";

import { useEffect, useState, useLayoutEffect } from "react";
import { createPortal } from "react-dom";
import { ChevronsRight } from "lucide-react";
import type { CodeBoxSuggestion } from "./_types";

interface Props {
  items: CodeBoxSuggestion[];
  loading?: boolean;
  activeIndex: number;
  onSelect: (item: CodeBoxSuggestion) => void;
  visible: boolean;
  expandCount: number;
  onExpand: () => void;
  anchorRef: React.RefObject<HTMLElement | null>;
  dropdownRef: React.RefObject<HTMLDivElement | null>;
  width?: number;
}

const itemBaseStyle: React.CSSProperties = {
  display: "flex",
  alignItems: "baseline",
  gap: 8,
  padding: "6px 10px",
  cursor: "pointer",
  fontSize: 13,
  lineHeight: 1.4,
  userSelect: "none",
};

const codeStyle: React.CSSProperties = {
  fontFamily: "var(--font-mono, monospace)",
  fontWeight: 600,
  flexShrink: 0,
  color: "var(--ink-strong, #111)",
};

const nameStyle: React.CSSProperties = {
  color: "var(--ink-muted, #6b7280)",
  overflow: "hidden",
  textOverflow: "ellipsis",
  whiteSpace: "nowrap",
};

const statusStyle: React.CSSProperties = {
  padding: "8px 10px",
  fontSize: 13,
  color: "var(--ink-muted, #6b7280)",
};

const expandBtnStyle: React.CSSProperties = {
  position: "absolute",
  top: 2,
  right: 2,
  background: "var(--surface-2, #f3f4f6)",
  border: "1px solid var(--border, #d1d5db)",
  borderRadius: 3,
  cursor: "pointer",
  padding: "2px 4px",
  display: "flex",
  alignItems: "center",
  color: "var(--ink-muted)",
};

interface DropdownPos {
  top: number;
  left: number;
  minWidth: number;
}

export function CodeBoxSuggestions({ items, loading, activeIndex, onSelect, visible, expandCount, onExpand, anchorRef, dropdownRef, width }: Props) {
  const [pos, setPos] = useState<DropdownPos>({ top: 0, left: 0, minWidth: 0 });

  // activeIndex 스크롤 추적
  useEffect(() => {
    if (!visible || activeIndex < 0) return;
    const el = dropdownRef.current?.querySelector(`[role="option"]:nth-child(${activeIndex + 1})`) as HTMLElement | null;
    el?.scrollIntoView({ block: "nearest" });
  }, [visible, activeIndex, dropdownRef]);

  // anchor 기준 fixed 위치 계산 + scroll/resize 시 재계산
  useLayoutEffect(() => {
    if (!visible || !anchorRef.current) return;

    function recalc() {
      if (!anchorRef.current) return;
      const rect = anchorRef.current.getBoundingClientRect();
      // 드롭다운이 이미 렌더됐으므로 offsetWidth로 실제 너비를 측정.
      // dropdownRef.current가 없는 경우(이론상 불가)에는 anchor 너비로 fallback.
      const dropdownWidth = dropdownRef.current?.offsetWidth ?? rect.width;
      let left = rect.left;
      // 오른쪽 뷰포트 경계 초과 시 왼쪽으로 밀기
      if (left + dropdownWidth > window.innerWidth - 8) left = window.innerWidth - dropdownWidth - 8;
      // 왼쪽 경계도 8px clamp
      if (left < 8) left = 8;
      setPos({ top: rect.bottom, left, minWidth: rect.width });
    }

    recalc();

    // 스크롤 컨테이너(패널 등)가 스크롤될 때 드롭다운 위치를 갱신
    window.addEventListener("scroll", recalc, true);
    window.addEventListener("resize", recalc);

    return () => {
      window.removeEventListener("scroll", recalc, true);
      window.removeEventListener("resize", recalc);
    };
  }, [visible, items, expandCount, anchorRef, dropdownRef, width]);

  if (!visible) return null;

  // SSR 환경에서는 portal 불가
  if (typeof document === "undefined") return null;

  const containerStyle: React.CSSProperties = {
    position: "fixed",
    top: pos.top,
    left: pos.left,
    zIndex: 1000,
    background: "var(--surface-1, #fff)",
    border: "1px solid var(--border, #d1d5db)",
    borderRadius: 4,
    maxHeight: 240,
    overflowY: "auto",
    boxShadow: "0 4px 12px rgba(0,0,0,0.12)",
    minWidth: pos.minWidth,
    // 부모(code-box)가 관리하는 widthPx — undefined면 자연 너비
    width,
  };

  function handleExpand(e: React.MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    onExpand();
  }

  const content = loading
    ? <div style={statusStyle}>...</div>
    : items.length === 0
      ? <div style={statusStyle}>No results</div>
      : items.map((item, idx) => (
          <div
            key={`${item.code}-${idx}`}
            role="option"
            aria-selected={idx === activeIndex}
            style={{
              ...itemBaseStyle,
              background: idx === activeIndex ? "var(--accent, #3b82f6)" : "transparent",
              color: idx === activeIndex ? "#fff" : undefined,
            }}
            onMouseDown={(e) => { e.preventDefault(); onSelect(item); }}
          >
            <span style={idx === activeIndex ? { ...codeStyle, color: "#fff" } : codeStyle}>{item.code}</span>
            {item.name && <span style={idx === activeIndex ? { ...nameStyle, color: "rgba(255,255,255,0.85)" } : nameStyle}>{item.name}</span>}
          </div>
        ));

  return createPortal(
    <div ref={dropdownRef} style={containerStyle} role="listbox">
      {content}
      <button type="button" style={expandBtnStyle} onMouseDown={handleExpand} title="Expand width">
        <ChevronsRight size={10} />
      </button>
    </div>,
    document.body
  );
}
