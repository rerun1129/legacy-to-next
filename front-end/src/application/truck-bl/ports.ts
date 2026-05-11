import type {
  TruckBlRow,
  TruckBlFilter,
  TruckBlDetail,
  CreateTruckBlRequest,
  UpdateTruckBlRequest,
} from '@/domain/truck-bl';

export interface TruckBlPageResult {
  content: TruckBlRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface TruckBlPort {
  list(filter: TruckBlFilter, page: number, size?: number): Promise<TruckBlPageResult>;
  getById(id: number): Promise<TruckBlDetail>;
  create(payload: CreateTruckBlRequest): Promise<{ id: number }>;
  update(id: number, payload: UpdateTruckBlRequest): Promise<void>;
  delete(id: number): Promise<void>;
  findByHblNo(hblNo: string): Promise<number[]>;
  changeHblNo(id: number, hblNo: string): Promise<void>;
}
