import type {
  CarrierRow,
  CarrierDetail,
  CarrierFilter,
  CreateCarrierRequestDto,
  UpdateCarrierRequestDto,
  SaveCarrierChangesRequestDto,
  SaveChangesResultDto,
} from "@/domain/code/carrier";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";

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
  saveChanges(req: SaveCarrierChangesRequestDto): Promise<SaveChangesResultDto>;
  autocomplete(q: string, limit?: number, type?: string): Promise<CodeBoxSuggestion[]>;
}
