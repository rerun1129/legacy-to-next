"use client";

import React, { useRef, useState, useEffect } from "react";
import type { GridColumn } from "./grid-list";
import { useColumnLayout } from "@/lib/use-column-layout";

interface ColumnVisibilityMenuProps<T> {
  gridId: string;
  defaultColumns: GridColumn<T>[];
}

export function ColumnVisibilityMenu<T>({
  gridId,
  defaultColumns,
}: ColumnVisibilityMenuProps<T>): JSX.Element {
  const [isOpen, setIsOpen] = useState(false);
  const btnRef = useRef<HTMLButtonElement>(null);
  const popoverRef = useRef<HTMLUListElement>(null);

  const { hiddenColumns, showColumn } = useColumnLayout(gridId, defaultColumns);

  // ESC 키 및 외부 클릭으로 닫힘
  useEffect(() => {
    if (!isOpen) return;

    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape") setIsOpen(false);
    }

    function handlePointerDown(e: PointerEvent) {
      const target = e.target as Node;
      if (
        !btnRef.current?.contains(target) &&
        !popoverRef.current?.contains(target)
      ) {
        setIsOpen(false);
      }
    }

    document.addEventListener("keydown", handleKeyDown);
    document.addEventListener("pointerdown", handlePointerDown);
    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.removeEventListener("pointerdown", handlePointerDown);
    };
  }, [isOpen]);

  return (
    <span style={{ position: "relative", display: "inline-flex" }}>
      <button
        ref={btnRef}
        type="button"
        className="grid__column-menu-btn"
        aria-label="컬럼 표시 관리"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((v) => !v)}
      >
        {/* 햄버거 아이콘 — lucide 의존성 없이 SVG 인라인 */}
        <svg
          width="14"
          height="14"
          viewBox="0 0 14 14"
          fill="none"
          aria-hidden="true"
        >
          <rect x="1" y="3" width="12" height="1.5" rx="0.75" fill="currentColor" />
          <rect x="1" y="6.25" width="12" height="1.5" rx="0.75" fill="currentColor" />
          <rect x="1" y="9.5" width="12" height="1.5" rx="0.75" fill="currentColor" />
        </svg>
      </button>

      {isOpen && (
        <ul ref={popoverRef} className="grid__column-menu-popover" role="menu">
          {hiddenColumns.length === 0 ? (
            <li className="grid__column-menu-empty" role="none">
              모든 컬럼이 표시 중입니다
            </li>
          ) : (
            hiddenColumns.map((col) => (
              <li
                key={String(col.key)}
                role="menuitem"
                tabIndex={0}
                onClick={() => {
                  showColumn(String(col.key));
                }}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    showColumn(String(col.key));
                  }
                }}
              >
                {col.label}
              </li>
            ))
          )}
        </ul>
      )}
    </span>
  );
}
