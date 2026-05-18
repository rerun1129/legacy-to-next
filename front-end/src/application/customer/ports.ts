import type {
  CustomerRow,
  CustomerDetail,
  CustomerFilter,
  CreateCustomerRequestDto,
  UpdateCustomerRequestDto,
} from "@/domain/customer";

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
}
