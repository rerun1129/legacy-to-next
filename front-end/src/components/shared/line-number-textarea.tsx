"use client";

import { useRef, useState } from "react";

interface Props {
  defaultValue?: string;
  value?:        string;
  onChange?:     (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  onBlur?:       (e: React.FocusEvent<HTMLTextAreaElement>) => void;
  placeholder?:  string;
  style?:        React.CSSProperties;
  name?:         string;
  readOnly?:     boolean;
  required?:     boolean;
}

export function LineNumberTextarea({ defaultValue = "", value, onChange, onBlur, placeholder, style, name, readOnly, required }: Props) {
  // controlled 모드: value prop이 있으면 사용, 없으면 내부 state
  const [internalValue, setInternalValue] = useState(String(defaultValue));
  const [focused, setFocused] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const gutterRef   = useRef<HTMLDivElement>(null);

  const controlled = value !== undefined;
  const currentValue = controlled ? value : internalValue;
  const lineCount = currentValue.split("\n").length;

  function syncScroll() {
    if (gutterRef.current && textareaRef.current) {
      gutterRef.current.scrollTop = textareaRef.current.scrollTop;
    }
  }

  function handleChange(e: React.ChangeEvent<HTMLTextAreaElement>) {
    if (!controlled) setInternalValue(e.target.value);
    onChange?.(e);
  }

  function handleBlur(e: React.FocusEvent<HTMLTextAreaElement>) {
    setFocused(false);
    onBlur?.(e);
  }

  return (
    <div
      style={{
        display:     "flex",
        border:      `1px solid ${required && !readOnly ? "color-mix(in srgb, var(--required-bar) 30%, var(--border))" : focused ? "var(--focus-ring)" : "var(--border)"}`,
        borderRadius: 5,
        background:  readOnly ? "var(--bg-sunken)" : "var(--surface-2)",
        overflow:    "hidden",
        boxShadow:   focused && !readOnly ? "0 0 0 3px var(--focus-glow)" : undefined,
        borderLeft:  required && !readOnly ? "3px solid var(--required-bar)" : undefined,
        ...style,
      }}
    >
      {/* 행 번호 거터 */}
      <div
        ref={gutterRef}
        aria-hidden
        style={{
          width:         20,
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
        name={name}
        value={currentValue}
        placeholder={placeholder}
        readOnly={readOnly}
        onChange={handleChange}
        onScroll={syncScroll}
        onFocus={() => setFocused(true)}
        onBlur={handleBlur}
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
