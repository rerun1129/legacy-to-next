import type {
  FaqRow,
  FaqDetail,
  FaqFilter,
  CreateFaqRequestDto,
  UpdateFaqRequestDto,
} from "@/domain/faq";

export interface FaqPageResult {
  content: FaqRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface FaqPort {
  search(filter: FaqFilter, page: number, size?: number): Promise<FaqPageResult>;
  getById(id: number): Promise<FaqDetail>;
  create(req: CreateFaqRequestDto): Promise<number>;
  update(id: number, req: UpdateFaqRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
}
