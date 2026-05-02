import type { MasterBlRow, MasterBlFilter, MasterBlDetail, CreateMasterBlRequest, UpdateMasterBlRequest } from '@/domain/master-bl';

export interface MasterBlPort {
  list(filter: MasterBlFilter): Promise<MasterBlRow[]>;
  getById(id: number): Promise<MasterBlRow>;
  create(req: CreateMasterBlRequest): Promise<MasterBlDetail>;
  update(id: number, req: UpdateMasterBlRequest): Promise<MasterBlDetail>;
  delete(id: number): Promise<void>;
}
