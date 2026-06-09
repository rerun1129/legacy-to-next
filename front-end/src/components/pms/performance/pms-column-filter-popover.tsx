"use client";

import { useState, useRef, useEffect, useCallback, useMemo } from "react";
import { createPortal } from "react-dom";
import { Filter, ListFilter } from "lucide-react";

interface PmsColumnFilterPopoverProps {
  /** 현재 로드된 페이지 rows에서 추출한 distinct 표시값 목록. */
  values: string[];
  /** 현재 선택 상태. undefined = 필터 없음(전체 선택과 동치). */
  selected: ReadonlySet<string> | undefined;
  /**
   * 선택 변경 콜백.
   * - Set: 해당 컬럼에 필터 적용.
   * - null: 필터 해제(키를 ColumnFilterState에서 제거).
   */
  onChange: (next: Set<string> | null) => void;
  /** 컬럼 label — aria 접근성용. */
  label: string;
}

/** 팝오버 내 검색어로 values를 필터링. */
function filterValues(values: string[], query: string): string[] {
  const q = query.trim().toLowerCase();
  if (!q) return values;
  return values.filter((v) => v.toLowerCase().includes(q));
}

/**
 * PMS 실적 그리드 컬럼 헤더 깔때기 필터 팝오버(PMS 전용).
 * MultiSelectBox의 portal 위치잡기·dismiss 패턴 차용.
 */
export function PmsColumnFilterPopover({
  values,
  selected,
  onChange,
  label,
}: PmsColumnFilterPopoverProps) {
  const [open, setOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  // 팝오버 오픈 시 적용 전 임시 선택 상태 (적용 버튼 클릭 시 커밋)
  const [draft, setDraft] = useState<Set<string>>(new Set());

  const triggerRef = useRef<HTMLButtonElement>(null);
  const popoverRef = useRef<HTMLDivElement>(null);
  const rafRef = useRef<number>(0);
  const [popStyle, setPopStyle] = useState({ top: 0, left: 0 });

  // selected가 undefined이면 필터 없음. Set(빈 Set 포함)이 있으면 필터 활성.
  const isActive = selected !== undefined;

  const updatePos = useCallback(() => {
    if (!triggerRef.current) return;
    const r = triggerRef.current.getBoundingClientRect();
    setPopStyle({ top: r.bottom + 2, left: r.left });
  }, []);

  const openPopover = useCallback(() => {
    // 오픈 시 draft를 현재 selected 기준으로 초기화
    // selected가 undefined(필터 없음)이면 values 전체를 선택된 상태로 초기화
    setDraft(selected !== undefined ? new Set(selected) : new Set(values));
    setSearchQuery("");
    updatePos();
    setOpen(true);
  }, [selected, values, updatePos]);

  const closePopover = useCallback(() => {
    setOpen(false);
  }, []);

  // scroll/resize 시 위치 재계산 (MultiSelectBox 동일 패턴)
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

  // outside mousedown → 닫기 (MultiSelectBox 동일 패턴)
  useEffect(() => {
    if (!open) return;
    const handle = (e: MouseEvent) => {
      const t = e.target as Node;
      if (triggerRef.current?.contains(t) || popoverRef.current?.contains(t)) return;
      closePopover();
    };
    document.addEventListener("mousedown", handle);
    return () => document.removeEventListener("mousedown", handle);
  }, [open, closePopover]);

  // Esc 닫기
  useEffect(() => {
    if (!open) return;
    const handle = (e: KeyboardEvent) => {
      if (e.key === "Escape") closePopover();
    };
    document.addEventListener("keydown", handle);
    return () => document.removeEventListener("keydown", handle);
  }, [open, closePopover]);

  const filtered = useMemo(() => filterValues(values, searchQuery), [values, searchQuery]);

  const allFilteredSelected = filtered.length > 0 && filtered.every((v) => draft.has(v));

  function handleToggleAll() {
    if (allFilteredSelected) {
      // 필터된 항목 전체 해제
      setDraft((prev) => {
        const next = new Set(prev);
        for (const v of filtered) next.delete(v);
        return next;
      });
    } else {
      // 필터된 항목 전체 선택
      setDraft((prev) => {
        const next = new Set(prev);
        for (const v of filtered) next.add(v);
        return next;
      });
    }
  }

  function handleToggleValue(v: string) {
    setDraft((prev) => {
      const next = new Set(prev);
      if (next.has(v)) {
        next.delete(v);
      } else {
        next.add(v);
      }
      return next;
    });
  }

  function handleApply() {
    // 전체 values가 모두 선택된 경우는 필터 없음(null)과 동치 — 키 제거
    const allSelected = values.length > 0 && values.every((v) => draft.has(v));
    if (allSelected || draft.size === 0) {
      // draft.size===0은 아무것도 안 보이게 되므로 지우기와 동일하게 처리
      onChange(draft.size === 0 ? new Set() : null);
    } else {
      onChange(new Set(draft));
    }
    closePopover();
  }

  function handleClear() {
    onChange(null);
    closePopover();
  }

  return (
    <>
      <button
        ref={triggerRef}
        type="button"
        aria-label={`${label} 필터`}
        onClick={(e) => {
          // th의 drag 핸들러로 이벤트가 전파되지 않도록 차단
          e.stopPropagation();
          if (open) closePopover();
          else openPopover();
        }}
        style={{
          display: "inline-flex",
          alignItems: "center",
          justifyContent: "center",
          background: "none",
          border: "none",
          padding: "0 2px",
          cursor: "pointer",
          color: isActive ? "var(--accent)" : "var(--ink-4)",
          verticalAlign: "middle",
          flexShrink: 0,
          lineHeight: 1,
        }}
      >
        {isActive ? <ListFilter size={11} /> : <Filter size={11} />}
      </button>

      {open &&
        createPortal(
          <div
            ref={popoverRef}
            role="dialog"
            aria-label={`${label} 값 필터`}
            onMouseDown={(e) => e.stopPropagation()}
            style={{
              position: "fixed",
              top: popStyle.top,
              left: popStyle.left,
              zIndex: 9999,
              // portal이 body에 마운트되므로 테마 상속이 끊김 — 명시 지정
              background: "var(--surface-1)",
              color: "var(--ink)",
              border: "1px solid var(--border)",
              borderRadius: "var(--radius-sm)",
              boxShadow: "0 4px 12px rgba(0,0,0,0.12)",
              minWidth: 200,
              maxWidth: 280,
              display: "flex",
              flexDirection: "column",
              gap: 0,
            }}
          >
            {/* 검색 입력 */}
            <div style={{ padding: "6px 8px", borderBottom: "1px solid var(--divider)" }}>
              <input
                type="text"
                placeholder="검색…"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={(e) => e.stopPropagation()}
                autoFocus
                style={{
                  width: "100%",
                  padding: "3px 6px",
                  fontSize: "12px",
                  border: "1px solid var(--border)",
                  borderRadius: "var(--radius-sm)",
                  background: "var(--surface-2)",
                  color: "inherit",
                  outline: "none",
                  boxSizing: "border-box",
                }}
              />
            </div>

            {/* 전체선택/해제 */}
            <div
              style={{
                padding: "4px 8px",
                borderBottom: "1px solid var(--divider)",
                fontSize: "11px",
                color: "var(--ink-3)",
              }}
            >
              <label
                style={{ display: "flex", alignItems: "center", gap: 6, cursor: "pointer" }}
              >
                <input
                  type="checkbox"
                  checked={allFilteredSelected}
                  onChange={handleToggleAll}
                  style={{ margin: 0, flexShrink: 0 }}
                />
                전체 {filtered.length > 0 ? `(${filtered.length})` : ""}
              </label>
            </div>

            {/* 값 체크리스트 */}
            <ul
              role="listbox"
              aria-multiselectable="true"
              style={{
                listStyle: "none",
                margin: 0,
                padding: "2px 0",
                maxHeight: 200,
                overflowY: "auto",
                fontSize: "12px",
              }}
            >
              {filtered.length === 0 ? (
                <li
                  style={{
                    padding: "6px 8px",
                    color: "var(--ink-4)",
                    fontSize: "12px",
                  }}
                >
                  결과 없음
                </li>
              ) : (
                filtered.map((v) => (
                  <li
                    key={v || "__empty__"}
                    role="option"
                    aria-selected={draft.has(v)}
                    style={{ padding: "0" }}
                  >
                    <label
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: 6,
                        padding: "3px 8px",
                        cursor: "pointer",
                        color: "inherit",
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={draft.has(v)}
                        onChange={() => handleToggleValue(v)}
                        style={{ margin: 0, flexShrink: 0 }}
                      />
                      {v === "" ? "(빈 값)" : v}
                    </label>
                  </li>
                ))
              )}
            </ul>

            {/* 적용/지우기 버튼 */}
            <div
              style={{
                display: "flex",
                gap: 4,
                padding: "6px 8px",
                borderTop: "1px solid var(--divider)",
              }}
            >
              <button
                type="button"
                onClick={handleApply}
                style={{
                  flex: 1,
                  fontSize: "11px",
                  padding: "3px 0",
                  background: "var(--accent)",
                  color: "var(--accent-ink)",
                  border: "none",
                  borderRadius: "var(--radius-sm)",
                  cursor: "pointer",
                }}
              >
                적용
              </button>
              <button
                type="button"
                onClick={handleClear}
                style={{
                  flex: 1,
                  fontSize: "11px",
                  padding: "3px 0",
                  background: "var(--surface-2)",
                  color: "var(--ink-2)",
                  border: "1px solid var(--border)",
                  borderRadius: "var(--radius-sm)",
                  cursor: "pointer",
                }}
              >
                지우기
              </button>
            </div>
          </div>,
          document.body,
        )}
    </>
  );
}
