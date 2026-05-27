export interface ExchangeRateFormValues {
  fromCurrencyCode: string;
  toCurrencyCode: string;
  exchangeDate: string;
  cashSellExchangeRate: number | null;
  cashBuyExchangeRate: number | null;
  wireSendExchangeRate: number | null;
  wireReceiveExchangeRate: number | null;
  standardExchangeRate: number | null;
  name: string;
  nameEn: string;
  active: boolean;
}

export const DEFAULT_EXCHANGE_RATE_FORM: ExchangeRateFormValues = {
  fromCurrencyCode: "",
  toCurrencyCode: "",
  exchangeDate: "",
  cashSellExchangeRate: null,
  cashBuyExchangeRate: null,
  wireSendExchangeRate: null,
  wireReceiveExchangeRate: null,
  standardExchangeRate: null,
  name: "",
  nameEn: "",
  active: true,
};
