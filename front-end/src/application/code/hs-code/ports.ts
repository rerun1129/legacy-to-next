import type {
  HsCodeRow,
  HsCodeDetail,
  HsCodeFilter,
  CreateHsCodeRequestDto,
  UpdateHsCodeRequestDto,
  SaveHsCodeChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/hs-code";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface HsCodePageResult {
  content: HsCodeRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface HsCodePort {
  search(filter: HsCodeFilter, page: number, size?: number): Promise<HsCodePageResult>;
  getById(id: number): Promise<HsCodeDetail>;
  create(req: CreateHsCodeRequestDto): Promise<number>;
  update(id: number, req: UpdateHsCodeRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
  saveChanges(req: SaveHsCodeChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number): Promise<CodeBoxSuggestion[]>;
}
