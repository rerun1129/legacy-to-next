import type {
  CarrierRow,
  CarrierDetail,
  CarrierFilter,
  CreateCarrierRequestDto,
  UpdateCarrierRequestDto,
} from "@/domain/code/carrier";

export interface CarrierPageResult {
  content: CarrierRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface CarrierPort {
  search(filter: CarrierFilter, page: number, size?: number): Promise<CarrierPageResult>;
  getById(id: number): Promise<CarrierDetail>;
  create(req: CreateCarrierRequestDto): Promise<number>;
  update(id: number, req: UpdateCarrierRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
}
