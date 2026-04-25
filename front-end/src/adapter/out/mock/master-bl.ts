import type { MasterBlPort } from '@/application/master-bl/ports';
import type { MasterBlRow, MasterBlFilter } from '@/domain/master-bl';

const masterBlRows: MasterBlRow[] = [];

export const mockMasterBlPort: MasterBlPort = {
  async list(_filter: MasterBlFilter): Promise<MasterBlRow[]> {
    return masterBlRows;
  },
  async getById(id: string): Promise<MasterBlRow> {
    const row = masterBlRows.find((r) => r.id === id);
    if (!row) throw new Error(`MasterBl not found: ${id}`);
    return row;
  },
  async delete(_id: string): Promise<void> {
    // mock: no-op
  },
};
