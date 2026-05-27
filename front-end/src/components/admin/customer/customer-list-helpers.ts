import type { CustomerRow, CustomerType } from "@/domain/customer";
import type { CustomerFormRow } from "./customer-grid-columns";

export const VALID_CUSTOMER_TYPES = [
  "CUSTOMER",
  "PARTNER",
  "AIRCARRIER",
  "LINER",
  "TRUCKER",
  "WAREHOUSE",
  "OTHER",
] as const satisfies readonly CustomerType[];

export const PASTE_COLS = [
  "customerCode",
  "customerType",
  "name",
  "nameEn",
  "businessNo",
  "representative",
  "phone",
  "email",
  "customerLocalAddress",
  "customerEnglishAddress",
  "memo",
  "countryCode",
  "active",
] as const;

export type PasteCol = typeof PASTE_COLS[number];

export const ROW_IS_EQUAL = (a: CustomerFormRow, b: CustomerFormRow) =>
  a.customerType === b.customerType &&
  a.name === b.name &&
  a.nameEn === b.nameEn &&
  a.businessNo === b.businessNo &&
  a.representative === b.representative &&
  a.phone === b.phone &&
  a.email === b.email &&
  a.customerLocalAddress === b.customerLocalAddress &&
  a.customerEnglishAddress === b.customerEnglishAddress &&
  a.memo === b.memo &&
  a.countryCode === b.countryCode &&
  a.active === b.active;

export const TO_CREATE = (row: CustomerFormRow) => ({
  customerCode: row.customerCode,
  customerType: row.customerType,
  name: row.name,
  nameEn: row.nameEn || null,
  businessNo: row.businessNo || null,
  representative: row.representative || null,
  phone: row.phone || null,
  email: row.email || null,
  customerLocalAddress: row.customerLocalAddress || null,
  customerEnglishAddress: row.customerEnglishAddress || null,
  memo: row.memo || null,
  countryCode: row.countryCode || null,
  active: row.active,
});

export const TO_UPDATE = (row: CustomerFormRow) => ({
  id: row.entityId,
  customerType: row.customerType,
  name: row.name,
  nameEn: row.nameEn || null,
  businessNo: row.businessNo || null,
  representative: row.representative || null,
  phone: row.phone || null,
  email: row.email || null,
  customerLocalAddress: row.customerLocalAddress || null,
  customerEnglishAddress: row.customerEnglishAddress || null,
  memo: row.memo || null,
  countryCode: row.countryCode || null,
  active: row.active,
});

export function toFormRow(row: CustomerRow): CustomerFormRow {
  return {
    entityId: row.id,
    customerCode: row.customerCode,
    customerType: row.customerType,
    name: row.name ?? "",
    nameEn: row.nameEn ?? "",
    businessNo: row.businessNo ?? "",
    representative: row.representative ?? "",
    phone: row.phone ?? "",
    email: row.email ?? "",
    customerLocalAddress: row.customerLocalAddress ?? "",
    customerEnglishAddress: row.customerEnglishAddress ?? "",
    memo: row.memo ?? "",
    countryCode: row.countryCode ?? "",
    active: row.active,
  };
}
