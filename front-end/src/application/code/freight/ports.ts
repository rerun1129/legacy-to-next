import type {
  FreightRow,
  FreightDetail,
  FreightFilter,
  CreateFreightRequestDto,
  UpdateFreightRequestDto,
} from "@/domain/code/freight";

export interface FreightPageResult {
  content: FreightRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface FreightPort {
  search(filter: FreightFilter, page: number, size?: number): Promise<FreightPageResult>;
  getById(id: number): Promise<FreightDetail>;
  create(req: CreateFreightRequestDto): Promise<number>;
  update(id: number, req: UpdateFreightRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
}
