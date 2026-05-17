export type PartnerType =
  | "FORWARDER"
  | "SHIPPER"
  | "CONSIGNEE"
  | "CARRIER"
  | "AGENT"
  | "CUSTOMS_BROKER";

export type PartnerTypeFilter = "ALL" | PartnerType;

export type PartnerScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface PartnerRow {
  id: number;
  partnerCode: string;
  name: string;
  partnerType: PartnerType;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface PartnerDetail {
  id: number;
  partnerCode: string;
  partnerType: PartnerType;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  memo: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface PartnerFilter {
  partnerCode: string;
  name: string;
  partnerType: PartnerTypeFilter;
  scope: PartnerScope;
}

export interface CreatePartnerRequestDto {
  partnerCode: string;
  partnerType: PartnerType;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  memo: string | null;
  active: boolean;
}

export interface UpdatePartnerRequestDto {
  partnerType: PartnerType;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  memo: string | null;
  active: boolean;
}
