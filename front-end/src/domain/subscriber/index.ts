export type SubscriberScope = "ALL" | "ACTIVE" | "INACTIVE" | "DELETED";

export interface SubscriberRow {
  id: number;
  subscriberCode: string;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  memo: string | null;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface SubscriberDetail {
  id: number;
  subscriberCode: string;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  memo: string | null;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface SubscriberFilter {
  subscriberCode: string;
  name: string;
  scope: SubscriberScope;
}

export interface CreateSubscriberRequestDto {
  subscriberCode: string;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  memo: string | null;
  active: boolean;
}

export interface UpdateSubscriberRequestDto {
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  memo: string | null;
  active: boolean;
}

export interface UpdateSubscriberItemDto {
  id: number;
  name: string;
  nameEn: string | null;
  businessNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  memo: string | null;
  active: boolean;
}

export interface SaveSubscriberChangesRequestDto {
  creates: CreateSubscriberRequestDto[];
  updates: UpdateSubscriberItemDto[];
  deleteIds: number[];
}

export interface SaveChangesResultDto {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}
