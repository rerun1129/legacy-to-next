import type { SubscriberRow } from "@/domain/subscriber";
import type { SubscriberFormRow } from "./subscriber-grid-columns";

export const PASTE_COLS = [
  "subscriberCode",
  "name",
  "nameEn",
  "businessNo",
  "representative",
  "phone",
  "email",
  "memo",
  "active",
] as const;

export type PasteCol = typeof PASTE_COLS[number];

export const ROW_IS_EQUAL = (a: SubscriberFormRow, b: SubscriberFormRow) =>
  a.name === b.name &&
  a.nameEn === b.nameEn &&
  a.businessNo === b.businessNo &&
  a.representative === b.representative &&
  a.phone === b.phone &&
  a.email === b.email &&
  a.memo === b.memo &&
  a.active === b.active;

export const TO_CREATE = (row: SubscriberFormRow) => ({
  subscriberCode: row.subscriberCode,
  name: row.name,
  nameEn: row.nameEn || null,
  businessNo: row.businessNo || null,
  representative: row.representative || null,
  phone: row.phone || null,
  email: row.email || null,
  memo: row.memo || null,
  active: row.active,
});

export const TO_UPDATE = (row: SubscriberFormRow) => ({
  id: row.entityId,
  name: row.name,
  nameEn: row.nameEn || null,
  businessNo: row.businessNo || null,
  representative: row.representative || null,
  phone: row.phone || null,
  email: row.email || null,
  memo: row.memo || null,
  active: row.active,
});

export function toFormRow(row: SubscriberRow): SubscriberFormRow {
  return {
    entityId: row.id,
    subscriberCode: row.subscriberCode,
    name: row.name ?? "",
    nameEn: row.nameEn ?? "",
    businessNo: row.businessNo ?? "",
    representative: row.representative ?? "",
    phone: row.phone ?? "",
    email: row.email ?? "",
    memo: row.memo ?? "",
    active: row.active,
  };
}
