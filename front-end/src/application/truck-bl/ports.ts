import type { TruckBlRow, TruckBlFilter } from '@/domain/truck-bl';

export interface TruckBlPageResult {
  content: TruckBlRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface TruckBlPort {
  list(filter: TruckBlFilter, page: number, size?: number): Promise<TruckBlPageResult>;
}
