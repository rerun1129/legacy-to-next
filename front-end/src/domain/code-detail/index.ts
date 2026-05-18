import type { ActiveFilter } from "@/domain/code-master";

export type { ActiveFilter };

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

export interface CodeDetailFilter {
  codeValue: string;
  codeLabel: string;
  active: ActiveFilter;
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
