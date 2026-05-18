export type ActiveFilter = "ALL" | "ACTIVE" | "INACTIVE";

export interface CodeMasterRow {
  id: number;
  masterCode: string;
  masterName: string;
  description: string | null;
  sortOrder: number | null;
  active: boolean;
  updatedAt: string;
}

export interface CodeMasterDetail extends CodeMasterRow {
  createdAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CodeMasterFilter {
  masterCode: string;
  masterName: string;
  active: ActiveFilter;
}

export interface CreateCodeMasterRequestDto {
  masterCode: string;
  masterName: string;
  description: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface UpdateCodeMasterRequestDto {
  masterName: string;
  description: string | null;
  sortOrder: number | null;
  active: boolean;
}
