import type { AirHouseRow, AirHouseFilter } from '@/domain/air-house';

export interface AirHousePageResult {
  content: AirHouseRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface AirHousePort {
  list(filter: AirHouseFilter, page: number, size?: number): Promise<AirHousePageResult>;
}
