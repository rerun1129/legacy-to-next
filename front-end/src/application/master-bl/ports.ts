import type { MasterBlRow, MasterBlFilter } from '@/domain/master-bl';

export interface MasterBlPort {
  list(filter: MasterBlFilter): Promise<MasterBlRow[]>;
  getById(id: string): Promise<MasterBlRow>;
  delete(id: string): Promise<void>;
}
