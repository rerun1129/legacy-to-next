import type { MasterBlRow, MasterBlFilter } from '@/domain/master-bl';

export interface MasterBlPort {
  list(filter: MasterBlFilter): Promise<MasterBlRow[]>;
  getById(id: number): Promise<MasterBlRow>;
  delete(id: number): Promise<void>;
}
