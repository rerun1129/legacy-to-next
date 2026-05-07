import type { SeaMasterRow, SeaMasterFilter } from '@/domain/sea-master';

export interface SeaMasterPageResult {
  content: SeaMasterRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface SeaMasterPort {
  list(filter: SeaMasterFilter, page: number, size?: number): Promise<SeaMasterPageResult>;
}
