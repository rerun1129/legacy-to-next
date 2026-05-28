import type { PermissionPresetSummary } from "@/domain/access/permission-preset";
import type { PresetFormRow } from "./permission-preset-grid-columns";

export const PASTE_COLS = ["code", "name", "description", "active"] as const;

export const ROW_IS_EQUAL = (a: PresetFormRow, b: PresetFormRow): boolean =>
  a.name === b.name &&
  a.description === b.description &&
  a.active === b.active;

// code 는 신규 행에서만 create 대상, 기존 행 update 시 BE 가 무시
export const TO_CREATE = (row: PresetFormRow) => ({
  code: row.code.trim(),
  name: row.name.trim(),
  description: row.description.trim() || undefined,
  active: row.active,
});

export const TO_UPDATE = (row: PresetFormRow) => ({
  id: row.entityId,
  name: row.name.trim(),
  description: row.description.trim() || undefined,
  active: row.active,
});

export function toFormRow(row: PermissionPresetSummary): PresetFormRow {
  return {
    entityId: row.id,
    code: row.code,
    name: row.name,
    description: row.description ?? "",
    active: row.active,
  };
}
