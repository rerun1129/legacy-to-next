export interface CodeDetailRow {
  id: number;
  masterId: number;
  codeValue: string;
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  updatedAt: string;
}

export interface CodeDetailDetail extends CodeDetailRow {
  remark: string | null;
  createdAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export interface CreateCodeDetailRequestDto {
  masterId: number;
  codeValue: string;
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  remark: string | null;
}

export interface UpdateCodeDetailRequestDto {
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  remark: string | null;
}

export interface UpdateCodeDetailItem {
  id: number;
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  remark: string | null;
}

export interface SaveCodeDetailChangesRequest {
  masterId: number;
  creates: CreateCodeDetailRequestDto[];
  updates: UpdateCodeDetailItem[];
  deleteIds: number[];
}

export interface SaveChangesResult {
  createdCount: number;
  updatedCount: number;
  deletedCount: number;
}
