import type { SeaHouseRow, SeaHouseFilter } from '@/domain/sea-house';

export interface SeaHousePageResult {
  content: SeaHouseRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface SeaHousePort {
  list(filter: SeaHouseFilter, page: number, size?: number): Promise<SeaHousePageResult>;
}
