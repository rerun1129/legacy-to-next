export interface ModuleRow {
  moduleCode: string;
  name: string;
  description: string | null;
  sortOrder: number | null;
  active: boolean;
  updatedAt: string;
}

export interface ModuleDetail extends ModuleRow {
  createdAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CreateModuleDto {
  moduleCode: string;
  name: string;
  description: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface UpdateModuleDto {
  name: string;
  description: string | null;
  sortOrder: number | null;
  active: boolean;
}
