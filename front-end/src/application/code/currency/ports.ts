import type {
  CurrencyRow,
  CurrencyDetail,
  CurrencyFilter,
  CreateCurrencyRequestDto,
  UpdateCurrencyRequestDto,
  SaveCurrencyChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/currency";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface CurrencyPageResult {
  content: CurrencyRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface CurrencyPort {
  search(filter: CurrencyFilter, page: number, size?: number): Promise<CurrencyPageResult>;
  getById(id: number): Promise<CurrencyDetail>;
  create(req: CreateCurrencyRequestDto): Promise<number>;
  update(id: number, req: UpdateCurrencyRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
  saveChanges(req: SaveCurrencyChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number): Promise<CodeBoxSuggestion[]>;
}
