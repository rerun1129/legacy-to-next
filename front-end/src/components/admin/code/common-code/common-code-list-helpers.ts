import type { CommonCodeRow } from "@/domain/common-code";
import type { CommonCodeFormRow } from "./common-code-grid-columns";

export function toCommonCodeFormRow(row: CommonCodeRow): CommonCodeFormRow {
  return {
    entityId: row.id,
    code: row.code,
    label: row.label,
    // DB nullable → 폼 input은 string 필요
    labelKo: row.labelKo ?? "",
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
  // 빈 문자열은 null로 정규화 — 시드 컨벤션(null) 오염 방지
  labelKo: row.labelKo === "" ? null : row.labelKo,
  sortOrder: row.sortOrder,
  active: row.active,
});

export const COMMON_CODE_TO_UPDATE = (row: CommonCodeFormRow) => ({
  id: row.entityId,
  label: row.label,
  // 빈 문자열은 null로 정규화 — 시드 컨벤션(null) 오염 방지
  labelKo: row.labelKo === "" ? null : row.labelKo,
  sortOrder: row.sortOrder,
  active: row.active,
});
