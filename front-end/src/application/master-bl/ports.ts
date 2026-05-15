import type { MasterBlRow, MasterBlFilter, MasterBlDetail, CreateMasterBlRequest, UpdateMasterBlRequest } from '@/domain/master-bl';

export interface MasterBlPort {
  list(filter: MasterBlFilter): Promise<MasterBlRow[]>;
  getById(id: number): Promise<MasterBlDetail>;
  // §6.54 — create는 ID-only 반환 (BE Phase 3 정합)
  create(req: CreateMasterBlRequest): Promise<{ id: number }>;
  update(id: number, req: UpdateMasterBlRequest): Promise<void>;
  delete(id: number): Promise<void>;
  // EXACT 매칭 — POST /api/master-bl/find-by-mbl-no
  findByMblNo(mblNo: string): Promise<number[]>;
}
