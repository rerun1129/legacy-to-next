import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlFilter, HouseBlDetail } from '@/domain/house-bl';
import { NotFoundError } from '@/adapter/out/api/errors';
import { HOUSE_BL_ROWS } from '@/lib/mock-data';

export const mockHouseBlPort: HouseBlPort = {
  async list(_filter: HouseBlFilter): Promise<HouseBlRow[]> {
    return HOUSE_BL_ROWS as unknown as HouseBlRow[];
  },
  async getById(id: number): Promise<HouseBlDetail> {
    const row = HOUSE_BL_ROWS.find((r) => r.no === id);
    if (!row) throw new NotFoundError('HouseBl', String(id));
    return row as unknown as HouseBlDetail;
  },
  async save(data: unknown): Promise<HouseBlDetail> {
    return { ...HOUSE_BL_ROWS[0], ...(data as Record<string, unknown>) } as unknown as HouseBlDetail;
  },
  async delete(_id: number): Promise<void> {
    // mock: no-op
  },
};
