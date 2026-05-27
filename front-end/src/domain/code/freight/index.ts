export type FreightScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface FreightRow {
  id: number;
  freightCode: string;
  name: string;
  nameEn: string | null;
  description: string | null;
  freightUnit: string | null;
  freightGroup: string | null;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface FreightDetail {
  id: number;
  freightCode: string;
  name: string;
  nameEn: string | null;
  description: string | null;
  freightUnit: string | null;
  freightGroup: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface FreightFilter {
  freightCode: string;
  name: string;
  scope: FreightScope;
}

export interface CreateFreightRequestDto {
  freightCode: string;
  name: string;
  nameEn: string | null;
  description: string | null;
  freightUnit: string | null;
  freightGroup: string | null;
  active: boolean;
}

export interface UpdateFreightRequestDto {
  name: string;
  nameEn: string | null;
  description: string | null;
  freightUnit: string | null;
  freightGroup: string | null;
  active: boolean;
}
