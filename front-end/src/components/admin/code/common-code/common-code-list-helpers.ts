import type { CommonCodeRow } from "@/domain/common-code";
import type { CommonCodeFormRow } from "./common-code-grid-columns";

export function toCommonCodeFormRow(row: CommonCodeRow): CommonCodeFormRow {
  return {
    entityId: row.id,
    code: row.code,
    label: row.label,
    labelKo: row.labelKo,
    sortOrder: row.sortOrder,
    active: row.active,
  };
}

export const COMMON_CODE_ROW_IS_EQUAL = (
  a: CommonCodeFormRow,
  b: CommonCodeFormRow,
): boolean =>
  a.label === b.label &&
  a.labelKo === b.labelKo &&
  a.sortOrder === b.sortOrder &&
  a.active === b.active;

export const COMMON_CODE_TO_CREATE = (row: CommonCodeFormRow) => ({
  code: row.code,
  label: row.label,
  labelKo: row.labelKo,
  sortOrder: row.sortOrder,
  active: row.active,
});

export const COMMON_CODE_TO_UPDATE = (row: CommonCodeFormRow) => ({
  id: row.entityId,
  label: row.label,
  labelKo: row.labelKo,
  sortOrder: row.sortOrder,
  active: row.active,
});
