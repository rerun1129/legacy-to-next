import type { TruckBlRow, TruckBlFilter, TruckBlDetail } from '@/domain/truck-bl';

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
}
