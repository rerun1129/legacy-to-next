"use client";

import { Button } from "@/components/shared/button";
import { PAGE_SIZE_OPTIONS } from "@/lib/grid-pagination";

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  disabled?: boolean;
  pageSize?: number;
  onPageSizeChange?: (size: number) => void;
}

export function Pagination({ currentPage, totalPages, onPageChange, disabled, pageSize, onPageSizeChange }: PaginationProps) {
  if (!onPageSizeChange && totalPages <= 1) return null;

  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8, justifyContent: "flex-end", padding: "6px 0" }}>
      {onPageSizeChange && (
        <select
          className="lcn__select"
          value={pageSize}
          disabled={disabled}
          onChange={(e) => onPageSizeChange(Number(e.target.value))}
        >
          {PAGE_SIZE_OPTIONS.map((n) => (
            <option key={n} value={n}>{n}건</option>
          ))}
        </select>
      )}
      {totalPages > 1 && (
        <>
          <Button
            size="sm"
            variant="normal"
            disabled={disabled || currentPage === 1}
            onClick={() => onPageChange(currentPage - 1)}
          >
            &lt; Prev
          </Button>
          <span style={{ display: "flex", alignItems: "center", gap: 4, fontSize: 12 }}>
            <input
              key={currentPage}
              defaultValue={currentPage}
              type="text"
              inputMode="numeric"
              disabled={disabled}
              aria-label="페이지 번호 입력 후 Enter"
              style={{
                width: 46,
                textAlign: "center",
                fontSize: 12,
                padding: "1px 4px",
              }}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  const el = e.currentTarget;
                  const n = Number(el.value);
                  if (Number.isInteger(n) && n >= 1 && n <= totalPages && n !== currentPage) {
                    onPageChange(n);
                  } else {
                    el.value = String(currentPage);
                  }
                }
              }}
              onBlur={(e) => {
                const el = e.currentTarget;
                const n = Number(el.value);
                if (Number.isInteger(n) && n >= 1 && n <= totalPages && n !== currentPage) {
                  onPageChange(n);
                } else {
                  el.value = String(currentPage);
                }
              }}
            />
            / {totalPages}
          </span>
          <Button
            size="sm"
            variant="normal"
            disabled={disabled || currentPage === totalPages}
            onClick={() => onPageChange(currentPage + 1)}
          >
            Next &gt;
          </Button>
        </>
      )}
    </div>
  );
}
