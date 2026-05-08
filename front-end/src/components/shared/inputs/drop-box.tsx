"use client";

import { forwardRef, useState, useMemo, useRef, useEffect, useCallback } from "react";
import { createPortal } from "react-dom";
import type { DropBoxProps, DropBoxOption } from "./_types";
import { panelClass, cellClass } from "./_styles";

export const DropBox = forwardRef<HTMLInputElement, DropBoxProps>(
  function DropBox(props, ref) {
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
      onBlur,
      onFocus,
      name,
      ...rest
    } = props;

    const isInert = disabled || (readOnly && !disabled);

    const [open, setOpen] = useState(false);
    const [query, setQuery] = useState("");
    const [highlight, setHighlight] = useState(0);

    const hiddenRef = useRef<HTMLInputElement | null>(null);
    const visibleRef = useRef<HTMLInputElement | null>(null);
    const wrapRef = useRef<HTMLDivElement>(null);
    const listRef = useRef<HTMLUListElement>(null);
    const rafRef = useRef<number>(0);
    const [popStyle, setPopStyle] = useState({ top: 0, left: 0, width: 0 });

    // External ref → hidden input (holds actual option value for RHF / form submit)
    const mergeRef = useCallback(
      (node: HTMLInputElement | null) => {
        hiddenRef.current = node;
        if (typeof ref === "function") ref(node);
        else if (ref) (ref as React.MutableRefObject<HTMLInputElement | null>).current = node;
      },
      [ref]
    );

    const strValue = (value as string) ?? "";

    const selectedLabel = useMemo(
      () => options.find((o) => o.value === strValue)?.label ?? "",
      [options, strValue]
    );

    // Precompute lowercase labels once per options change
    const lower = useMemo(
      () => options.map((o) => ({ o, k: o.label.toLowerCase() })),
      [options]
    );

    // LIKE filter: case-insensitive substring match
    const filtered = useMemo(() => {
      const q = query.toLowerCase().trim();
      return q ? lower.filter((x) => x.k.includes(q)).map((x) => x.o) : options;
    }, [lower, query, options]);

    const updatePos = () => {
      if (!wrapRef.current) return;
      const r = wrapRef.current.getBoundingClientRect();
      setPopStyle({ top: r.bottom + 2, left: r.left, width: r.width });
    };

    const openList = () => {
      if (isInert) return;
      updatePos();
      setQuery("");
      setHighlight(0);
      setOpen(true);
    };

    const closeList = () => {
      setOpen(false);
      setQuery("");
    };

    const selectOpt = (opt: DropBoxOption) => {
      onChange?.({
        target: { value: opt.value, name: name ?? "" },
      } as React.ChangeEvent<HTMLInputElement>);
      closeList();
      visibleRef.current?.blur();
    };

    // Reposition on scroll / resize while open
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
    }, [open]);

    // Outside mousedown → close
    useEffect(() => {
      if (!open) return;
      const handle = (e: MouseEvent) => {
        const t = e.target as Node;
        if (wrapRef.current?.contains(t) || listRef.current?.contains(t)) return;
        closeList();
      };
      document.addEventListener("mousedown", handle);
      return () => document.removeEventListener("mousedown", handle);
    }, [open]);

    // Scroll highlighted option into view
    useEffect(() => {
      if (!open) return;
      const item = listRef.current?.children[highlight] as HTMLElement | undefined;
      item?.scrollIntoView?.({ block: "nearest" });
    }, [highlight, open]);

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "ArrowDown") {
        e.preventDefault();
        if (!open) { openList(); return; }
        setHighlight((h) => Math.min(h + 1, filtered.length - 1));
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        if (!open) { openList(); return; }
        setHighlight((h) => Math.max(h - 1, 0));
      } else if (e.key === "Enter") {
        e.preventDefault();
        if (open && filtered[highlight]) { selectOpt(filtered[highlight]); return; }
        openList();
      } else if (e.key === "Escape" || e.key === "Tab") {
        closeList();
      }
    };

    const displayValue = open ? query : selectedLabel;
    const showPlaceholder = !open && !selectedLabel ? placeholder : undefined;

    const base = variant === "cell" ? cellClass({ required }) : panelClass({ required });
    const combined = className ? `${base} ${className}` : base;

    return (
      <div
        ref={wrapRef}
        className={`combo${variant === "cell" ? " combo--cell" : ""}`}
        style={style}
      >
        {/* Holds actual option value for RHF ref.current.value and form submit */}
        <input ref={mergeRef} type="hidden" name={name} value={strValue} readOnly />
        <input
          ref={visibleRef}
          className={combined}
          value={displayValue}
          placeholder={showPlaceholder}
          disabled={isInert}
          autoComplete="off"
          onChange={(e) => {
            setQuery(e.target.value);
            setHighlight(0);
            if (!open) openList();
          }}
          onFocus={(e) => { openList(); onFocus?.(e); }}
          onBlur={onBlur}
          onKeyDown={handleKeyDown}
          {...rest}
        />
        <span className="combo__caret" aria-hidden>▾</span>
        {open &&
          createPortal(
            <ul
              ref={listRef}
              role="listbox"
              className="combo__list"
              style={{ top: popStyle.top, left: popStyle.left, width: popStyle.width }}
              onMouseDown={(e) => e.preventDefault()}
            >
              {filtered.length === 0 ? (
                <li className="combo__option combo__option--empty">결과 없음</li>
              ) : (
                filtered.map((opt, i) => (
                  <li
                    key={opt.value}
                    role="option"
                    aria-selected={i === highlight}
                    className={`combo__option${i === highlight ? " is-active" : ""}`}
                    onMouseDown={() => selectOpt(opt)}
                  >
                    {opt.label}
                  </li>
                ))
              )}
            </ul>,
            document.body
          )}
      </div>
    );
  }
);
