import type { AttributeDefinitionRow, AttributeValueType } from "@/domain/access/attribute";

export const PASTE_COLS = ["attributeKey", "name", "valueType", "allowMulti", "active"] as const;

export interface AttributeFormRow {
  // RHF 내부 키 — 음수이면 신규 행
  entityId: number;
  attributeKey: string;
  name: string;
  valueType: AttributeValueType;
  allowMulti: boolean;
  active: boolean;
}

export interface AttributeFormValues {
  rows: AttributeFormRow[];
}

export const ROW_IS_EQUAL = (a: AttributeFormRow, b: AttributeFormRow): boolean =>
  a.name === b.name &&
  a.valueType === b.valueType &&
  a.allowMulti === b.allowMulti &&
  a.active === b.active;

// attributeKey 는 신규 행에서만 create 대상; update 시 식별자로만 사용
export const TO_CREATE = (row: AttributeFormRow) => ({
  attributeKey: row.attributeKey.trim(),
  name: row.name.trim(),
  valueType: row.valueType,
  allowMulti: row.allowMulti,
  active: row.active,
});

export const TO_UPDATE = (row: AttributeFormRow) => ({
  attributeKey: row.attributeKey,
  name: row.name.trim(),
  valueType: row.valueType,
  allowMulti: row.allowMulti,
  active: row.active,
});

export function toFormRow(row: AttributeDefinitionRow): AttributeFormRow {
  return {
    // 기존 행은 attributeKey 해시 → 양수 int 로 변환해 entityId 대용
    entityId: keyToEntityId(row.attributeKey),
    attributeKey: row.attributeKey,
    name: row.name,
    valueType: row.valueType,
    allowMulti: row.allowMulti,
    active: row.active,
  };
}

/**
 * 문자열 attributeKey 를 양수 int entityId 로 변환한다.
 * RHF 가 id 를 UUID 로 덮어쓰므로 entityId 를 별도 필드로 관리해야 한다.
 * 동일 key 에 대해 항상 같은 양수 값을 돌려줘야 신규/기존 판별이 정확하다.
 */
function keyToEntityId(key: string): number {
  let hash = 0;
  for (let i = 0; i < key.length; i++) {
    hash = (Math.imul(31, hash) + key.charCodeAt(i)) | 0;
  }
  // 양수 보장: 음수(신규 행) 와 충돌하지 않도록 abs + 1
  return Math.abs(hash) + 1;
}
