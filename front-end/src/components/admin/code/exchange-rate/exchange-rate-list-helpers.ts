import type { ExchangeRateRow } from "@/domain/code/exchange-rate";
import type { ExchangeRateFormRow } from "./exchange-rate-grid-columns";

export const PASTE_COLS = [
  "fromCurrencyCode",
  "toCurrencyCode",
  "exchangeDate",
  "cashSellExchangeRate",
  "cashBuyExchangeRate",
  "wireSendExchangeRate",
  "wireReceiveExchangeRate",
  "standardExchangeRate",
  "name",
  "nameEn",
  "active",
] as const;

export type PasteCol = typeof PASTE_COLS[number];

export const ROW_IS_EQUAL = (a: ExchangeRateFormRow, b: ExchangeRateFormRow) =>
  a.exchangeDate === b.exchangeDate &&
  a.cashSellExchangeRate === b.cashSellExchangeRate &&
  a.cashBuyExchangeRate === b.cashBuyExchangeRate &&
  a.wireSendExchangeRate === b.wireSendExchangeRate &&
  a.wireReceiveExchangeRate === b.wireReceiveExchangeRate &&
  a.standardExchangeRate === b.standardExchangeRate &&
  a.name === b.name &&
  a.nameEn === b.nameEn &&
  a.active === b.active;

export const TO_CREATE = (row: ExchangeRateFormRow) => ({
  fromCurrencyCode: row.fromCurrencyCode,
  toCurrencyCode: row.toCurrencyCode,
  exchangeDate: row.exchangeDate || null,
  cashSellExchangeRate: Number(row.cashSellExchangeRate) || null,
  cashBuyExchangeRate: Number(row.cashBuyExchangeRate) || null,
  wireSendExchangeRate: Number(row.wireSendExchangeRate) || null,
  wireReceiveExchangeRate: Number(row.wireReceiveExchangeRate) || null,
  standardExchangeRate: Number(row.standardExchangeRate) || null,
  name: row.name,
  nameEn: row.nameEn || null,
  active: row.active,
});

export const TO_UPDATE = (row: ExchangeRateFormRow) => ({
  id: row.entityId,
  exchangeDate: row.exchangeDate || null,
  cashSellExchangeRate: Number(row.cashSellExchangeRate) || null,
  cashBuyExchangeRate: Number(row.cashBuyExchangeRate) || null,
  wireSendExchangeRate: Number(row.wireSendExchangeRate) || null,
  wireReceiveExchangeRate: Number(row.wireReceiveExchangeRate) || null,
  standardExchangeRate: Number(row.standardExchangeRate) || null,
  name: row.name,
  nameEn: row.nameEn || null,
  active: row.active,
});

export function toFormRow(row: ExchangeRateRow): ExchangeRateFormRow {
  return {
    entityId: row.id,
    fromCurrencyCode: row.fromCurrencyCode,
    toCurrencyCode: row.toCurrencyCode,
    exchangeDate: row.exchangeDate ?? "",
    cashSellExchangeRate: row.cashSellExchangeRate != null ? String(row.cashSellExchangeRate) : "",
    cashBuyExchangeRate: row.cashBuyExchangeRate != null ? String(row.cashBuyExchangeRate) : "",
    wireSendExchangeRate: row.wireSendExchangeRate != null ? String(row.wireSendExchangeRate) : "",
    wireReceiveExchangeRate: row.wireReceiveExchangeRate != null ? String(row.wireReceiveExchangeRate) : "",
    standardExchangeRate: row.standardExchangeRate != null ? String(row.standardExchangeRate) : "",
    name: row.name ?? "",
    nameEn: row.nameEn ?? "",
    active: row.active,
  };
}
