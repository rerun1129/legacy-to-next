export type PortType = "SEA" | "AIR";

export type PortTypeFilter = "ALL" | PortType;

export type PortScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface PortRow {
  id: number;
  portCode: string;
  name: string | null;
  nameEn: string | null;
  countryCode: string;
  portType: PortType;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface PortDetail {
  id: number;
  portCode: string;
  portType: PortType;
  name: string | null;
  nameEn: string | null;
  countryCode: string;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface PortFilter {
  portCode: string;
  name: string;
  countryCode: string;
  portType: PortTypeFilter;
  scope: PortScope;
}

export interface CreatePortRequestDto {
  portCode: string;
  portType: PortType;
  name: string;
  nameEn: string | null;
  countryCode: string;
  active: boolean;
}

export interface UpdatePortRequestDto {
  portType: PortType;
  name: string;
  nameEn: string | null;
  countryCode: string;
  active: boolean;
}
