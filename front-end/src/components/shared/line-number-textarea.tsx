"use client";

import { useRef, useState } from "react";

interface Props {
  defaultValue?: string;
  placeholder?:  string;
  style?:        React.CSSProperties; // wrapper에 적용 (flex: 1, minHeight 등)
}

export function LineNumberTextarea({ defaultValue = "", placeholder, style }: Props) {
  const [value,   setValue]   = useState(String(defaultValue));
  const [focused, setFocused] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const gutterRef   = useRef<HTMLDivElement>(null);

  const lineCount = value.split("\n").length;

  function syncScroll() {
    if (gutterRef.current && textareaRef.current) {
      gutterRef.current.scrollTop = textareaRef.current.scrollTop;
    }
  }

  return (
    <div
      style={{
        display:     "flex",
        border:      `1px solid ${focused ? "var(--focus-ring)" : "var(--border)"}`,
        borderRadius: 5,
        background:  "var(--surface-2)",
        overflow:    "hidden",
        boxShadow:   focused ? "0 0 0 3px var(--focus-glow)" : undefined,
        ...style,
      }}
    >
      {/* 행 번호 거터 */}
      <div
        ref={gutterRef}
        aria-hidden
        style={{
          width:         30,
          paddingTop:    8,
          paddingBottom: 8,
          paddingRight:  6,
          background:    "color-mix(in srgb, var(--border) 40%, var(--surface-2))",
          borderRight:   "1px solid var(--border)",
          overflowY:     "hidden",
          textAlign:     "right",
          color:         "var(--ink)",
          fontSize:      8,
          lineHeight:    "18px",  // textarea와 동일한 줄 높이로 행 정렬 보장
          userSelect:    "none",
          flexShrink:    0,
        }}
      >
        {Array.from({ length: lineCount }, (_, i) => (
          <div key={i} style={{ height: 18 }}>{i + 1}</div>
        ))}
      </div>

      {/* 실제 textarea */}
      <textarea
        ref={textareaRef}
        value={value}
        placeholder={placeholder}
        onChange={e => setValue(e.target.value)}
        onScroll={syncScroll}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        style={{
          flex:       1,
          minWidth:   0,
          minHeight:  0,
          border:     "none",
          background: "transparent",
          padding:    "8px 10px",
          fontFamily: "inherit",
          fontSize:   "var(--fs-sm)",
          lineHeight: "18px",  // 거터와 동일
          color:      "var(--ink)",
          outline:    "none",
          resize:     "none",
          overflowY:  "auto",
        }}
      />
    </div>
  );
}
