export type ExchangeRateScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface ExchangeRateRow {
  id: number;
  fromCurrencyCode: string;
  toCurrencyCode: string;
  name: string;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface ExchangeRateDetail {
  id: number;
  fromCurrencyCode: string;
  toCurrencyCode: string;
  name: string;
  nameEn: string | null;
  exchangeDate: string | null;
  cashSellExchangeRate: number | null;
  cashBuyExchangeRate: number | null;
  wireSendExchangeRate: number | null;
  wireReceiveExchangeRate: number | null;
  standardExchangeRate: number | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface ExchangeRateFilter {
  fromCurrencyCode: string;
  toCurrencyCode: string;
  name: string;
  scope: ExchangeRateScope;
}

export interface CreateExchangeRateRequestDto {
  fromCurrencyCode: string;
  toCurrencyCode: string;
  exchangeDate: string | null;
  cashSellExchangeRate: number | null;
  cashBuyExchangeRate: number | null;
  wireSendExchangeRate: number | null;
  wireReceiveExchangeRate: number | null;
  standardExchangeRate: number | null;
  name: string;
  nameEn: string | null;
  active: boolean;
}

export interface UpdateExchangeRateRequestDto {
  exchangeDate: string | null;
  cashSellExchangeRate: number | null;
  cashBuyExchangeRate: number | null;
  wireSendExchangeRate: number | null;
  wireReceiveExchangeRate: number | null;
  standardExchangeRate: number | null;
  name: string;
  nameEn: string | null;
  active: boolean;
}
