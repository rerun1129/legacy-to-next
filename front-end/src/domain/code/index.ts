export interface CodeRow {
  id: number;
  codeGroup: string;
  codeValue: string;
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  updatedAt: string;
}

export interface CodeDetail {
  id: number;
  codeGroup: string;
  codeValue: string;
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

export type ActiveFilter = "ALL" | "ACTIVE" | "INACTIVE";

export interface CodeFilter {
  codeGroup: string;
  codeValue: string;
  codeLabel: string;
  active: ActiveFilter;
}

export interface CreateCodeRequestDto {
  codeGroup: string;
  codeValue: string;
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  remark: string | null;
}

export interface UpdateCodeRequestDto {
  codeLabel: string;
  sortOrder: number | null;
  active: boolean;
  remark: string | null;
}
