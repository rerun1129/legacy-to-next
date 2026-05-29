export interface MenuRow {
  id: number;
  menuCode: string;
  parentId: number | null;
  path: string | null;
  label: string;
  labelEn: string | null;
  icon: string | null;
  sortOrder: number | null;
  active: boolean;
  moduleCode: string;
  updatedAt: string;
}

export interface MenuDetail extends MenuRow {
  createdAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

// save-changes DTOs (batch inline edit)

export interface CreateMenuItem {
  menuCode: string;
  parentId: number | null;
  path: string | null;
  label: string;
  labelEn: string | null;
  icon: string | null;
  sortOrder: number | null;
  active: boolean;
  moduleCode: string;
}

export interface UpdateMenuItem {
  id: number;
  parentId: number | null;
  path: string | null;
  label: string;
  labelEn: string | null;
  icon: string | null;
  sortOrder: number | null;
  active: boolean;
  moduleCode: string;
}

export interface SaveMenuChangesRequest {
  creates: CreateMenuItem[];
  updates: UpdateMenuItem[];
}

// 공용 타입 — attribute와 동일 구조
export interface SaveChangesResult {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}

export interface MenuAutocompleteItem {
  code: string;
  name: string;
}
