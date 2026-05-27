export type CurrencyScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface CurrencyRow {
  id: number;
  currencyCode: string;
  name: string;
  nameEn: string | null;
  symbol: string | null;
  currencyUnit: number | null;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface CurrencyDetail {
  id: number;
  currencyCode: string;
  name: string;
  nameEn: string | null;
  symbol: string | null;
  currencyUnit: number | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CurrencyFilter {
  currencyCode: string;
  name: string;
  scope: CurrencyScope;
}

export interface CreateCurrencyRequestDto {
  currencyCode: string;
  name: string;
  nameEn: string | null;
  symbol: string | null;
  currencyUnit: number | null;
  active: boolean;
}

export interface UpdateCurrencyRequestDto {
  name: string;
  nameEn: string | null;
  symbol: string | null;
  currencyUnit: number | null;
  active: boolean;
}
