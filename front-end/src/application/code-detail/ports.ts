import type {
  CodeDetailRow,
  CodeDetailDetail,
  CreateCodeDetailRequestDto,
  UpdateCodeDetailRequestDto,
} from "@/domain/code-detail";

export interface CodeDetailPageResult {
  content: CodeDetailRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based for FE
  size: number;
}

export interface CodeDetailPort {
  search(masterId: number, page: number, size?: number): Promise<CodeDetailPageResult>;
  getById(id: number): Promise<CodeDetailDetail>;
  create(req: CreateCodeDetailRequestDto): Promise<number>;
  update(id: number, req: UpdateCodeDetailRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
}
