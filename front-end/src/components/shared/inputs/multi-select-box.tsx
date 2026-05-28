"use client";

import { useState, useRef, useEffect, useCallback, useMemo } from "react";
import { createPortal } from "react-dom";
import type { InputHTMLAttributes } from "react";
import type { BoxBaseProps } from "./_types";
import { panelClass, cellClass, labelClass } from "./_styles";

export interface MultiSelectBoxOption {
  value: string;
  label: string;
}

export interface MultiSelectBoxProps
  extends BoxBaseProps,
    Omit<
      InputHTMLAttributes<HTMLInputElement>,
      "children" | "required" | "readOnly" | "disabled" | "value" | "onChange"
    > {
  options: MultiSelectBoxOption[];
  value?: string[];
  onChange?: (values: string[]) => void;
  placeholder?: string;
}

export function MultiSelectBox(props: MultiSelectBoxProps) {
  const {
    variant = "panel",
    required,
    readOnly,
    disabled,
    className,
    style,
    options,
    placeholder,
    value,
    onChange,
    ...rest
  } = props;

  const isInert = disabled || (readOnly && !disabled);

  const [open, setOpen] = useState(false);
  const [highlight, setHighlight] = useState(0);

  const wrapRef = useRef<HTMLDivElement>(null);
  const visibleRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLUListElement>(null);
  const rafRef = useRef<number>(0);
  const [popStyle, setPopStyle] = useState({ top: 0, left: 0, width: 0 });

  const selected = useMemo(() => value ?? [], [value]);

  const displayValue =
    selected.length === 0
      ? ""
      : selected
          .map((v) => options.find((o) => o.value === v)?.label ?? v)
          .join(", ");

  const updatePos = useCallback(() => {
    if (!wrapRef.current) return;
    const r = wrapRef.current.getBoundingClientRect();
    setPopStyle({ top: r.bottom + 2, left: r.left, width: r.width });
  }, []);

  const openList = useCallback(() => {
    if (isInert) return;
    updatePos();
    setHighlight(0);
    setOpen(true);
  }, [isInert, updatePos]);

  const closeList = useCallback(() => {
    setOpen(false);
  }, []);

  // 선택 토글 — 드롭다운 유지를 위해 setOpen 호출하지 않음
  const toggleOpt = useCallback(
    (optValue: string) => {
      if (selected.includes(optValue)) {
        onChange?.(selected.filter((v) => v !== optValue));
      } else {
        onChange?.([...selected, optValue]);
      }
    },
    [selected, onChange],
  );

  // scroll/resize 시 위치 재계산
  useEffect(() => {
    if (!open) return;
    const handle = () => {
      cancelAnimationFrame(rafRef.current);
      rafRef.current = requestAnimationFrame(updatePos);
    };
    window.addEventListener("scroll", handle, { capture: true, passive: true });
    window.addEventListener("resize", handle, { passive: true });
    return () => {
      cancelAnimationFrame(rafRef.current);
      window.removeEventListener("scroll", handle, { capture: true });
      window.removeEventListener("resize", handle);
    };
  }, [open, updatePos]);

  // outside mousedown → 닫기
  useEffect(() => {
    if (!open) return;
    const handle = (e: MouseEvent) => {
      const t = e.target as Node;
      if (wrapRef.current?.contains(t) || listRef.current?.contains(t)) return;
      closeList();
    };
    document.addEventListener("mousedown", handle);
    return () => document.removeEventListener("mousedown", handle);
  }, [open, closeList]);

  // highlighted 옵션 스크롤 유지
  useEffect(() => {
    if (!open) return;
    const item = listRef.current?.children[highlight] as HTMLElement | undefined;
    item?.scrollIntoView?.({ block: "nearest" });
  }, [highlight, open]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      if (!open) { openList(); return; }
      setHighlight((h) => Math.min(h + 1, options.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      if (!open) { openList(); return; }
      setHighlight((h) => Math.max(h - 1, 0));
    } else if (e.key === " " || e.key === "Enter") {
      e.preventDefault();
      if (!open) { openList(); return; }
      if (options[highlight]) toggleOpt(options[highlight].value);
      // 드롭다운 유지 — setOpen 호출 없음
    } else if (e.key === "Escape" || e.key === "Tab") {
      closeList();
    }
  };

  const base =
    variant === "cell"
      ? cellClass({ required })
      : variant === "label"
        ? labelClass({ required })
        : panelClass({ required });
  const combined = className ? `${base} ${className}` : base;

  const comboModifier =
    variant === "cell" ? " combo--cell" : variant === "label" ? " combo--label" : "";

  return (
    <div ref={wrapRef} className={`combo${comboModifier}`} style={style}>
      <input
        ref={visibleRef}
        className={combined}
        value={displayValue}
        placeholder={selected.length === 0 ? placeholder : undefined}
        readOnly
        disabled={isInert}
        onFocus={() => openList()}
        onKeyDown={handleKeyDown}
        {...rest}
      />
      <span className="combo__caret" aria-hidden>
        ▾
      </span>
      {open &&
        createPortal(
          <ul
            ref={listRef}
            role="listbox"
            aria-multiselectable="true"
            className="combo__list"
            style={{ top: popStyle.top, left: popStyle.left, width: popStyle.width }}
            onMouseDown={(e) => e.preventDefault()}
          >
            {options.length === 0 ? (
              <li className="combo__option combo__option--empty">결과 없음</li>
            ) : (
              options.map((opt, i) => {
                const checked = selected.includes(opt.value);
                return (
                  <li
                    key={opt.value}
                    role="option"
                    aria-selected={checked}
                    className={`combo__option${i === highlight ? " is-active" : ""}`}
                    onMouseDown={() => toggleOpt(opt.value)}
                    style={{ display: "flex", alignItems: "center", gap: 6 }}
                  >
                    <input
                      type="checkbox"
                      checked={checked}
                      readOnly
                      style={{ pointerEvents: "none", margin: 0, flexShrink: 0 }}
                    />
                    {opt.label}
                  </li>
                );
              })
            )}
          </ul>,
          document.body,
        )}
    </div>
  );
}
