export interface CommonCodeGroupRow {
  id: number;
  groupCode: string;
  sourceModule: string;
  description: string | null;
  active: boolean;
}

export interface CommonCodeRow {
  id: number;
  groupCode: string;
  code: string;
  label: string;
  labelKo: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface CreateCommonCodeDto {
  code: string;
  label: string;
  labelKo: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface UpdateCommonCodeItem {
  id: number;
  label: string;
  labelKo: string | null;
  sortOrder: number | null;
  active: boolean;
}

export interface SaveCommonCodeChangesRequest {
  groupCode: string;
  creates: CreateCommonCodeDto[];
  updates: UpdateCommonCodeItem[];
}

export interface SaveChangesResult {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}
