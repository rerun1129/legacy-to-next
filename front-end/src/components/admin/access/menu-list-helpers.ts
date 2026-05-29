import type { MenuRow, CreateMenuItem, UpdateMenuItem } from "@/domain/access/menu";

export interface MenuFormRow {
  // entityId < 0 → 미저장 신규 행, > 0 → 기존 DB 행의 id
  entityId: number;
  id: number;
  menuCode: string;
  parentId: number | null;
  path: string | null;
  label: string;
  labelEn: string | null;
  icon: string | null;
  sortOrder: number | null;
  active: boolean;
  moduleCode: string;
}

export interface MenuFormValues {
  rows: MenuFormRow[];
}

export function toFormRow(row: MenuRow): MenuFormRow {
  return {
    entityId: row.id,
    id: row.id,
    menuCode: row.menuCode,
    parentId: row.parentId,
    path: row.path,
    label: row.label,
    labelEn: row.labelEn,
    icon: row.icon,
    sortOrder: row.sortOrder,
    active: row.active,
    moduleCode: row.moduleCode,
  };
}

// menuCode는 비교 제외 — 기존 행은 변경 불가이고 신규 행은 create 대상
export const ROW_IS_EQUAL = (a: MenuFormRow, b: MenuFormRow): boolean =>
  a.label === b.label &&
  a.labelEn === b.labelEn &&
  a.path === b.path &&
  a.icon === b.icon &&
  a.sortOrder === b.sortOrder &&
  a.active === b.active &&
  a.moduleCode === b.moduleCode &&
  a.parentId === b.parentId;

export const TO_CREATE = (row: MenuFormRow): CreateMenuItem => ({
  menuCode: row.menuCode.trim().toUpperCase(),
  parentId: row.parentId,
  path: row.path?.trim() || null,
  label: row.label.trim(),
  labelEn: row.labelEn?.trim() || null,
  icon: row.icon?.trim() || null,
  sortOrder: row.sortOrder,
  active: row.active,
  moduleCode: row.moduleCode,
});

export const TO_UPDATE = (row: MenuFormRow): UpdateMenuItem => ({
  id: row.id,
  parentId: row.parentId,
  path: row.path?.trim() || null,
  label: row.label.trim(),
  labelEn: row.labelEn?.trim() || null,
  icon: row.icon?.trim() || null,
  sortOrder: row.sortOrder,
  active: row.active,
  moduleCode: row.moduleCode,
});
