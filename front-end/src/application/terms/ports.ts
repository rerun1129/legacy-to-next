import type {
  TermsRow,
  TermsDetail,
  TermsFilter,
  CreateTermsRequestDto,
  UpdateTermsRequestDto,
} from "@/domain/terms";

export interface TermsPageResult {
  content: TermsRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface TermsPort {
  search(filter: TermsFilter, page: number, size?: number): Promise<TermsPageResult>;
  getById(id: number): Promise<TermsDetail>;
  create(req: CreateTermsRequestDto): Promise<number>;
  update(id: number, req: UpdateTermsRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
}
