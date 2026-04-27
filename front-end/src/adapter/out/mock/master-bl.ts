import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlFilter } from '@/domain/master-bl';
import { NotFoundError } from '@/adapter/out/api/errors';

// Master B/L fixture data: empty until backend integration is completed
const masterBlRows: MasterBlRow[] = [];

export const mockMasterBlPort: MasterBlPort = {
  async list(_filter: MasterBlFilter): Promise<MasterBlRow[]> {
    return masterBlRows;
  },
  async getById(id: number): Promise<MasterBlRow> {
    const row = masterBlRows.find((r) => r.id === id);
    if (!row) throw new NotFoundError('MasterBl', String(id));
    return row;
  },
  async delete(_id: number): Promise<void> {
    // mock: no-op
  },
};
