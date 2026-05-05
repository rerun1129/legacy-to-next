"use client";

import { Button } from "@/components/shared/button";

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  disabled?: boolean;
}

export function Pagination({ currentPage, totalPages, onPageChange, disabled }: PaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8, justifyContent: "flex-end", padding: "6px 0" }}>
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
    </div>
  );
}
