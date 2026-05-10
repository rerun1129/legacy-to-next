import type { NonBlRow, NonBlFilter, NonBlDetail, CreateNonBlRequest, UpdateNonBlRequest } from '@/domain/non-bl';

export interface NonBlPageResult {
  content: NonBlRow[];
  totalPages: number;
  totalElements: number;
  page: number;
  size: number;
}

export interface NonBlPort {
  list(filter: NonBlFilter, page: number, size?: number): Promise<NonBlPageResult>;
  getById(id: number): Promise<NonBlDetail>;
  create(req: CreateNonBlRequest): Promise<NonBlDetail>;
  update(id: number, req: UpdateNonBlRequest): Promise<NonBlDetail>;
  delete(id: number): Promise<void>;
  changeHblNo(id: number, hblNo: string): Promise<void>;
}
