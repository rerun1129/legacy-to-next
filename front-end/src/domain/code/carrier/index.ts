export type CarrierType = "SEA" | "AIR";

export type CarrierTypeFilter = "ALL" | CarrierType;

export type CarrierScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface CarrierRow {
  id: number;
  carrierCode: string;
  name: string | null;
  nameEn: string | null;
  carrierType: CarrierType;
  carrierAddress: string | null;
  ediCode: string | null;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface CarrierDetail {
  id: number;
  carrierCode: string;
  carrierType: CarrierType;
  name: string | null;
  nameEn: string | null;
  carrierAddress: string | null;
  ediCode: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CarrierFilter {
  carrierCode: string;
  name: string;
  carrierType: CarrierTypeFilter;
  scope: CarrierScope;
}

export interface CreateCarrierRequestDto {
  carrierCode: string;
  carrierType: CarrierType;
  name: string;
  nameEn: string | null;
  carrierAddress: string | null;
  ediCode: string | null;
  active: boolean;
}

export interface UpdateCarrierRequestDto {
  carrierType: CarrierType;
  name: string;
  nameEn: string | null;
  carrierAddress: string | null;
  ediCode: string | null;
  active: boolean;
}

export interface UpdateCarrierItemDto {
  id: number;
  carrierType: CarrierType;
  name: string;
  nameEn: string | null;
  carrierAddress: string | null;
  ediCode: string | null;
  active: boolean;
}

export interface SaveCarrierChangesRequestDto {
  creates: CreateCarrierRequestDto[];
  updates: UpdateCarrierItemDto[];
  deleteIds: number[];
}

export interface SaveChangesResultDto {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}
