"use client";

import { useRef, useCallback } from "react";
import type { CodeBoxProps, CodeBoxSuggestion } from "./_types";

const DEBOUNCE_MS = 300;

/** code input의 onChange/onKeyDown/onBlur를 래핑하여 자동완성 동작을 추가한다. */
export function useCodeInputHandlers(
  codeProps: CodeBoxProps["codeProps"],
  onSearch: CodeBoxProps["onSearch"],
  readOnly: boolean,
  disabled: boolean,
  setIsOpen: (v: boolean) => void,
  setActiveIndex: (v: number) => void,
  activeIndex: number,
  suggestions: CodeBoxSuggestion[],
  onSelect: ((item: CodeBoxSuggestion) => void) | undefined,
  isOpen: boolean,
  onExpand: () => void,
  onShrink: () => void
) {
  const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      codeProps.onChange?.(e);

      if (!onSearch || readOnly || disabled) return;

      const value = e.target.value;
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
      if (value.trim() === "") {
        setIsOpen(false);
        return;
      }
      debounceTimer.current = setTimeout(() => {
        onSearch(value);
        setIsOpen(true);
        setActiveIndex(0);
      }, DEBOUNCE_MS);
    },
    [codeProps, onSearch, readOnly, disabled, setIsOpen, setActiveIndex]
  );

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (!isOpen) {
        if (e.key === "ArrowDown" && suggestions.length > 0) {
          e.preventDefault();
          setIsOpen(true);
          setActiveIndex(0);
        }
        return;
      }
      if (e.key === "ArrowDown") {
        e.preventDefault();
        setActiveIndex(Math.min(activeIndex + 1, suggestions.length - 1));
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        setActiveIndex(Math.max(activeIndex - 1, 0));
      } else if (e.key === "ArrowRight") {
        e.preventDefault();
        onExpand();
      } else if (e.key === "ArrowLeft") {
        e.preventDefault();
        onShrink();
      } else if (e.key === "Enter") {
        const item = suggestions[activeIndex];
        if (item) {
          e.preventDefault();
          onSelect?.(item);
          setIsOpen(false);
        }
      } else if (e.key === "Escape") {
        setIsOpen(false);
      }
    },
    [isOpen, activeIndex, suggestions, onSelect, setIsOpen, setActiveIndex, onExpand, onShrink]
  );

  const handleBlur = useCallback(
    (e: React.FocusEvent<HTMLInputElement>) => {
      codeProps.onBlur?.(e);
      setIsOpen(false);
      if (!onSearch || readOnly || disabled) return;
      const value = e.target.value.trim();
      if (value === "") return;
      const match = suggestions.find((s) => s.code === value);
      if (!match) {
        Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")
          ?.set?.call(e.target, "");
        e.target.dispatchEvent(new Event("input", { bubbles: true }));
      }
    },
    [codeProps, setIsOpen, onSearch, readOnly, disabled, suggestions]
  );

  return { handleChange, handleKeyDown, handleBlur };
}
