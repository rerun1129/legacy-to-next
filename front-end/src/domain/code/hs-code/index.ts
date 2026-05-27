export type HsCodeScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface HsCodeRow {
  id: number;
  hsCode: string;
  name: string | null;
  nameEn: string | null;
  countryCode: string | null;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface HsCodeDetail {
  id: number;
  hsCode: string;
  name: string | null;
  nameEn: string | null;
  countryCode: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface HsCodeFilter {
  hsCode: string;
  name: string;
  scope: HsCodeScope;
}

export interface CreateHsCodeRequestDto {
  hsCode: string;
  name: string;
  nameEn: string | null;
  countryCode: string | null;
  active: boolean;
}

export interface UpdateHsCodeRequestDto {
  name: string;
  nameEn: string | null;
  countryCode: string | null;
  active: boolean;
}

export interface UpdateHsCodeItemDto {
  id: number;
  name: string;
  nameEn: string | null;
  countryCode: string | null;
  active: boolean;
}

export interface SaveHsCodeChangesRequestDto {
  creates: CreateHsCodeRequestDto[];
  updates: UpdateHsCodeItemDto[];
  deleteIds: number[];
}

export interface SaveChangesResultDto {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}
