import type { AirMasterRow, AirMasterFilter } from '@/domain/air-master';

export interface AirMasterPageResult {
  content: AirMasterRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface AirMasterPort {
  list(filter: AirMasterFilter, page: number, size?: number): Promise<AirMasterPageResult>;
}
