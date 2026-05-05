import type { NonBlRow, NonBlFilter } from '@/domain/non-bl';

export interface NonBlPageResult {
  content: NonBlRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface NonBlPort {
  list(filter: NonBlFilter, page: number, size?: number): Promise<NonBlPageResult>;
}
