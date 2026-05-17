export interface FaqCategoryRow {
  id: number;
  name: string;
  sortOrder: number;
  active: boolean;
  deletedAt: string | null;
  updatedAt: string;
}

export interface FaqCategoryDetail {
  id: number;
  name: string;
  sortOrder: number;
  active: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CreateFaqCategoryRequestDto {
  name: string;
  sortOrder: number;
  active: boolean;
}

export interface UpdateFaqCategoryRequestDto {
  name: string;
  sortOrder: number;
  active: boolean;
}
