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

export interface CreateMenuDto {
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

export interface UpdateMenuDto {
  parentId: number | null;
  path: string | null;
  label: string;
  labelEn: string | null;
  icon: string | null;
  sortOrder: number | null;
  active: boolean;
  moduleCode: string;
}
