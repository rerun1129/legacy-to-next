import type {
  ExchangeRateRow,
  ExchangeRateDetail,
  ExchangeRateFilter,
  CreateExchangeRateRequestDto,
  UpdateExchangeRateRequestDto,
  SaveExchangeRateChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/exchange-rate";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface ExchangeRatePageResult {
  content: ExchangeRateRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface ExchangeRatePort {
  search(filter: ExchangeRateFilter, page: number, size?: number): Promise<ExchangeRatePageResult>;
  getById(id: number): Promise<ExchangeRateDetail>;
  create(req: CreateExchangeRateRequestDto): Promise<number>;
  update(id: number, req: UpdateExchangeRateRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
  saveChanges(req: SaveExchangeRateChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number): Promise<CodeBoxSuggestion[]>;
}
