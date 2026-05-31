export interface SubscriptionItem {
  id: number;
  subscriberId: number;
  moduleCode: string;
  startDate: string;
  endDate: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSubscriptionItemDto {
  moduleCode: string;
  startDate: string;
  endDate: string;
  active: boolean;
}

export interface UpdateSubscriptionItemDto {
  id: number;
  startDate: string;
  endDate: string;
  active: boolean;
}

export interface SaveSubscriptionChangesRequestDto {
  creates: CreateSubscriptionItemDto[];
  updates: UpdateSubscriptionItemDto[];
  deleteIds: number[];
}

export interface SaveSubscriptionChangesResultDto {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}
