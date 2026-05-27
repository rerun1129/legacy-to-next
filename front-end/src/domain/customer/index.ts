export type CustomerType =
  | "CUSTOMER"
  | "PARTNER"
  | "AIRCARRIER"
  | "LINER"
  | "TRUCKER"
  | "WAREHOUSE"
  | "OTHER";

export type CustomerTypeFilter = "ALL" | CustomerType;

export type CustomerScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface CustomerRow {
  id: number;
  customerCode: string;
  customerType: CustomerType;
  name: string | null;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  customerLocalAddress: string | null;
  customerEnglishAddress: string | null;
  memo: string | null;
  countryCode: string | null;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface CustomerDetail {
  id: number;
  customerCode: string;
  customerType: CustomerType;
  name: string | null;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  customerLocalAddress: string | null;
  customerEnglishAddress: string | null;
  memo: string | null;
  countryCode: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CustomerFilter {
  customerCode: string;
  name: string;
  customerType: CustomerTypeFilter;
  scope: CustomerScope;
}

export interface CreateCustomerRequestDto {
  customerCode: string;
  customerType: CustomerType;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  customerLocalAddress: string | null;
  customerEnglishAddress: string | null;
  memo: string | null;
  countryCode: string | null;
  active: boolean;
}

export interface UpdateCustomerRequestDto {
  customerType: CustomerType;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  customerLocalAddress: string | null;
  customerEnglishAddress: string | null;
  memo: string | null;
  countryCode: string | null;
  active: boolean;
}
