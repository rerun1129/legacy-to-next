export type PackageUnitScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface PackageUnitRow {
  id: number;
  packageCode: string;
  name: string;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface PackageUnitDetail {
  id: number;
  packageCode: string;
  name: string;
  nameEn: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface PackageUnitFilter {
  packageCode: string;
  name: string;
  scope: PackageUnitScope;
}

export interface CreatePackageUnitRequestDto {
  packageCode: string;
  name: string;
  nameEn: string | null;
  active: boolean;
}

export interface UpdatePackageUnitRequestDto {
  name: string;
  nameEn: string | null;
  active: boolean;
}
