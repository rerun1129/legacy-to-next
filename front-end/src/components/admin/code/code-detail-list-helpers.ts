import type { CodeDetailRow } from "@/domain/code-detail";
import type { CodeDetailFormRow } from "./code-detail-grid-columns";

export const DETAIL_PASTE_COLS = [
  "codeValue",
  "codeLabel",
  "sortOrder",
  "active",
  "remark",
] as const;

export type DetailPasteCol = (typeof DETAIL_PASTE_COLS)[number];

export const DETAIL_ROW_IS_EQUAL = (a: CodeDetailFormRow, b: CodeDetailFormRow): boolean =>
  a.codeLabel === b.codeLabel &&
  a.sortOrder === b.sortOrder &&
  a.active === b.active &&
  a.remark === b.remark;

export const DETAIL_TO_UPDATE = (row: CodeDetailFormRow) => ({
  id: row.entityId,
  codeLabel: row.codeLabel,
  sortOrder: row.sortOrder,
  active: row.active,
  remark: row.remark || null,
});

export function toDetailFormRow(row: CodeDetailRow): CodeDetailFormRow {
  return {
    entityId: row.id,
    codeValue: row.codeValue,
    codeLabel: row.codeLabel,
    sortOrder: row.sortOrder,
    active: row.active,
    remark: row.remark ?? "",
  };
}
