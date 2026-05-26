export type ExchangeRateScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface ExchangeRateRow {
  id: number;
  baseCurrency: string;
  targetCurrency: string;
  rate: number;
  name: string;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface ExchangeRateDetail {
  id: number;
  baseCurrency: string;
  targetCurrency: string;
  rate: number;
  name: string;
  nameEn: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface ExchangeRateFilter {
  baseCurrency: string;
  targetCurrency: string;
  name: string;
  scope: ExchangeRateScope;
}

export interface CreateExchangeRateRequestDto {
  baseCurrency: string;
  targetCurrency: string;
  rate: number;
  name: string;
  nameEn: string | null;
  active: boolean;
}

export interface UpdateExchangeRateRequestDto {
  rate: number;
  name: string;
  nameEn: string | null;
  active: boolean;
}
