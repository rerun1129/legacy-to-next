import type { AttributeValueRow } from "@/domain/access/attribute-value";

export const ATTR_VAL_PASTE_COLS = ["value", "label", "sortOrder", "active"] as const;

export interface AttributeValueFormRow {
  // RHF 내부 키 — 음수이면 신규 행, 양수이면 기존 행 id
  entityId: number;
  value: string;
  label: string;
  sortOrder: number | null;
  active: boolean;
}

export interface AttributeValueFormValues {
  rows: AttributeValueFormRow[];
}

export const ROW_IS_EQUAL = (a: AttributeValueFormRow, b: AttributeValueFormRow): boolean =>
  a.label === b.label &&
  a.sortOrder === b.sortOrder &&
  a.active === b.active;

// value 는 신규 행에서만 create 대상; update 시 id 로 식별
export const TO_CREATE = (row: AttributeValueFormRow, attributeKey: string) => ({
  attributeKey,
  value: row.value.trim(),
  label: row.label.trim() || null,
  sortOrder: row.sortOrder,
  active: row.active,
});

export const TO_UPDATE = (row: AttributeValueFormRow) => ({
  id: row.entityId,
  label: row.label.trim() || null,
  sortOrder: row.sortOrder,
  active: row.active,
});

export function toValueFormRow(row: AttributeValueRow): AttributeValueFormRow {
  return {
    entityId: row.id,
    value: row.value,
    label: row.label ?? "",
    sortOrder: row.sortOrder,
    active: row.active,
  };
}
