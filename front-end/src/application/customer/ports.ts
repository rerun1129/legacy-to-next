import type {
  CustomerRow,
  CustomerDetail,
  CustomerFilter,
  CreateCustomerRequestDto,
  UpdateCustomerRequestDto,
  SaveCustomerChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/customer";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

export interface CustomerPageResult {
  content: CustomerRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface CustomerPort {
  search(filter: CustomerFilter, page: number, size?: number): Promise<CustomerPageResult>;
  getById(id: number): Promise<CustomerDetail>;
  create(req: CreateCustomerRequestDto): Promise<number>;
  update(id: number, req: UpdateCustomerRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
  saveChanges(req: SaveCustomerChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number, type?: string): Promise<CodeBoxSuggestion[]>;
}
