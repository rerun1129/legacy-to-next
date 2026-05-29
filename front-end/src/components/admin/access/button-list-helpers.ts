import type { ButtonRow, ButtonActionType, CreateButtonItem, UpdateButtonItem } from "@/domain/access/button";

export interface ButtonFormRow {
  // entityId < 0 → 미저장 신규 행, > 0 → 기존 DB 행의 id
  entityId: number;
  id: number;
  buttonCode: string;
  menuId: number;
  label: string;
  actionType: ButtonActionType;
  apiMethod: string | null;
  apiPath: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface ButtonFormValues {
  rows: ButtonFormRow[];
}

export function toFormRow(row: ButtonRow): ButtonFormRow {
  return {
    entityId: row.id,
    id: row.id,
    buttonCode: row.buttonCode,
    menuId: row.menuId,
    label: row.label,
    actionType: row.actionType,
    apiMethod: row.apiMethod,
    apiPath: row.apiPath,
    sortOrder: row.sortOrder,
    active: row.active,
  };
}

// buttonCode는 비교 제외 — 기존 행은 변경 불가이고 신규 행은 create 대상
export const ROW_IS_EQUAL = (a: ButtonFormRow, b: ButtonFormRow): boolean =>
  a.menuId === b.menuId &&
  a.label === b.label &&
  a.actionType === b.actionType &&
  a.apiMethod === b.apiMethod &&
  a.apiPath === b.apiPath &&
  a.sortOrder === b.sortOrder &&
  a.active === b.active;

export const TO_CREATE = (row: ButtonFormRow): CreateButtonItem => ({
  buttonCode: row.buttonCode.trim().toUpperCase(),
  menuId: row.menuId,
  label: row.label.trim(),
  actionType: row.actionType,
  apiMethod: row.apiMethod?.trim() || null,
  apiPath: row.apiPath?.trim() || null,
  sortOrder: row.sortOrder,
  active: row.active,
});

export const TO_UPDATE = (row: ButtonFormRow): UpdateButtonItem => ({
  id: row.id,
  menuId: row.menuId,
  label: row.label.trim(),
  actionType: row.actionType,
  apiMethod: row.apiMethod?.trim() || null,
  apiPath: row.apiPath?.trim() || null,
  sortOrder: row.sortOrder,
  active: row.active,
});
