import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlFilter } from '@/domain/house-bl';
import { NotFoundError } from '@/adapter/out/api/errors';
import { HOUSE_BL_ROWS } from '@/lib/mock-data';

export const mockHouseBlPort: HouseBlPort = {
  async list(_filter: HouseBlFilter): Promise<HouseBlRow[]> {
    return HOUSE_BL_ROWS as HouseBlRow[];
  },
  async getById(id: string): Promise<HouseBlRow> {
    const row = HOUSE_BL_ROWS.find((r) => r.hbl === id);
    if (!row) throw new NotFoundError('HouseBl', id);
    return row as HouseBlRow;
  },
  async save(data): Promise<HouseBlRow> {
    return { ...HOUSE_BL_ROWS[0], ...data } as HouseBlRow;
  },
  async delete(_id: string): Promise<void> {
    // mock: no-op
  },
};
