import type {
  PortRow,
  PortDetail,
  PortFilter,
  CreatePortRequestDto,
  UpdatePortRequestDto,
} from "@/domain/code/port";

export interface PortPageResult {
  content: PortRow[];
  totalPages: number;
  totalElements: number;
  page: number; // 1-based FE
  size: number;
}

export interface PortPort {
  search(filter: PortFilter, page: number, size?: number): Promise<PortPageResult>;
  getById(id: number): Promise<PortDetail>;
  create(req: CreatePortRequestDto): Promise<number>;
  update(id: number, req: UpdatePortRequestDto): Promise<void>;
  delete(id: number): Promise<void>;
  deleteMany(ids: number[]): Promise<void>;
}
