import type {
  CodeMasterRow,
  CodeMasterDetail,
  CodeMasterFilter,
  CreateCodeMasterRequestDto,
  UpdateCodeMasterRequestDto,
} from "@/domain/code-master";

export interface CodeMasterPageResult {
  content: CodeMasterRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based for FE
  size: number;
}

export interface CodeMasterPort {
  search(filter: CodeMasterFilter, page: number, size?: number): Promise<CodeMasterPageResult>;
  getById(id: number): Promise<CodeMasterDetail>;
  create(req: CreateCodeMasterRequestDto): Promise<number>;
  update(id: number, req: UpdateCodeMasterRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
}
