import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlFilter } from '@/domain/house-bl';
import { houseBLRows } from '@/lib/mock-data';

export const mockHouseBlPort: HouseBlPort = {
  async list(_filter: HouseBlFilter): Promise<HouseBlRow[]> {
    return houseBLRows as HouseBlRow[];
  },
  async getById(id: string): Promise<HouseBlRow> {
    const row = houseBLRows.find((r) => r.hbl === id);
    if (!row) throw new Error(`HouseBl not found: ${id}`);
    return row as HouseBlRow;
  },
  async save(data): Promise<HouseBlRow> {
    return { ...houseBLRows[0], ...data } as HouseBlRow;
  },
  async delete(_id: string): Promise<void> {
    // mock: no-op
  },
};
