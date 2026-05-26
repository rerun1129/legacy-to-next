import type {
  ExchangeRateRow,
  ExchangeRateDetail,
  ExchangeRateFilter,
  CreateExchangeRateRequestDto,
  UpdateExchangeRateRequestDto,
} from "@/domain/code/exchange-rate";

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
}
