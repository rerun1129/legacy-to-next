"use client";

import { useRef, useEffect, useState } from "react";
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

export function CodeBoxSuggestions({ items, loading, activeIndex, onSelect, visible, expandCount, onExpand }: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const [baseWidth, setBaseWidth] = useState(0);

  useEffect(() => {
    if (visible && ref.current && baseWidth === 0) {
      setBaseWidth(ref.current.offsetWidth);
    }
  }, [visible, items, baseWidth]);

  useEffect(() => {
    if (!visible || activeIndex < 0) return;
    const el = ref.current?.querySelector(`[role="option"]:nth-child(${activeIndex + 1})`) as HTMLElement | null;
    el?.scrollIntoView({ block: "nearest" });
  }, [visible, activeIndex]);

  if (!visible) return null;

  const containerStyle: React.CSSProperties = {
    position: "absolute",
    top: "100%",
    left: 0,
    zIndex: 100,
    background: "var(--surface-1, #fff)",
    border: "1px solid var(--border, #d1d5db)",
    borderRadius: 4,
    maxHeight: 240,
    overflowY: "auto",
    boxShadow: "0 4px 12px rgba(0,0,0,0.12)",
    minWidth: "100%",
    width: expandCount > 0 && baseWidth > 0
      ? baseWidth * (1 + expandCount)
      : undefined,
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

  return (
    <div ref={ref} style={containerStyle} role="listbox">
      {content}
      <button type="button" style={expandBtnStyle} onMouseDown={handleExpand} title="Expand width">
        <ChevronsRight size={10} />
      </button>
    </div>
  );
}
