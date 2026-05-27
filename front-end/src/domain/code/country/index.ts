export type CountryScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface CountryRow {
  id: number;
  countryCode: string;
  name: string | null;
  nameEn: string | null;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface CountryDetail {
  id: number;
  countryCode: string;
  name: string | null;
  nameEn: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CountryFilter {
  countryCode: string;
  name: string;
  scope: CountryScope;
}

export interface CreateCountryRequestDto {
  countryCode: string;
  name: string;
  nameEn: string | null;
  active: boolean;
}

export interface UpdateCountryRequestDto {
  name: string;
  nameEn: string | null;
  active: boolean;
}

export interface UpdateCountryItemDto {
  id: number;
  name: string;
  nameEn: string | null;
  active: boolean;
}

export interface SaveCountryChangesRequestDto {
  creates: CreateCountryRequestDto[];
  updates: UpdateCountryItemDto[];
  deleteIds: number[];
}

export interface SaveChangesResultDto {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}
