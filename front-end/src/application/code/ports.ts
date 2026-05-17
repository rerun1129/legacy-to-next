import type { CodeRow, CodeDetail, CodeFilter, CreateCodeRequestDto, UpdateCodeRequestDto } from "@/domain/code";

export interface CodePageResult {
  content: CodeRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based for FE
  size: number;
}

export interface CodePort {
  search(filter: CodeFilter, page: number, size?: number): Promise<CodePageResult>;
  getById(id: number): Promise<CodeDetail>;
  create(req: CreateCodeRequestDto): Promise<number>;
  update(id: number, req: UpdateCodeRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
}
