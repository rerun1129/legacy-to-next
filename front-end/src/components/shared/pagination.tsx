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
          <span style={{ fontSize: 12 }}>{currentPage} / {totalPages}</span>
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
