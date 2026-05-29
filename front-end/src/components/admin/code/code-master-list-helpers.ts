import type { CodeMasterRow } from "@/domain/code-master";
import type { CodeMasterFormRow } from "./code-master-grid-columns";

export const MASTER_PASTE_COLS = [
  "masterCode",
  "masterName",
  "description",
  "sortOrder",
  "active",
] as const;

export type MasterPasteCol = (typeof MASTER_PASTE_COLS)[number];

export const MASTER_ROW_IS_EQUAL = (a: CodeMasterFormRow, b: CodeMasterFormRow): boolean =>
  a.masterName === b.masterName &&
  a.description === b.description &&
  a.sortOrder === b.sortOrder &&
  a.active === b.active;

export const MASTER_TO_CREATE = (row: CodeMasterFormRow) => ({
  masterCode: row.masterCode,
  masterName: row.masterName,
  description: row.description || null,
  sortOrder: row.sortOrder,
  active: row.active,
});

export const MASTER_TO_UPDATE = (row: CodeMasterFormRow) => ({
  id: row.entityId,
  masterName: row.masterName,
  description: row.description || null,
  sortOrder: row.sortOrder,
  active: row.active,
});

export function toMasterFormRow(row: CodeMasterRow): CodeMasterFormRow {
  return {
    entityId: row.id,
    masterCode: row.masterCode,
    masterName: row.masterName,
    description: row.description ?? "",
    sortOrder: row.sortOrder,
    active: row.active,
  };
}
