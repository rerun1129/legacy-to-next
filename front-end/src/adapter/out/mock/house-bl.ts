import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlDetail, HouseBlFilter, CreateHouseBlRequest, UpdateHouseBlRequest } from '@/domain/house-bl';
import { NotFoundError } from '@/adapter/out/api/errors';
import { HOUSE_BL_ROWS } from '@/lib/mock-data';

export const mockHouseBlPort: HouseBlPort = {
  async list(_filter: HouseBlFilter): Promise<HouseBlRow[]> {
    return HOUSE_BL_ROWS as HouseBlRow[];
  },
  async getById(id: number): Promise<HouseBlRow> {
    const row = HOUSE_BL_ROWS.find((r) => r.no === id);
    if (!row) throw new NotFoundError('HouseBl', String(id));
    return row as HouseBlRow;
  },
  async save(data): Promise<HouseBlRow> {
    return { ...HOUSE_BL_ROWS[0], ...data } as HouseBlRow;
  },
  async create(req: CreateHouseBlRequest): Promise<HouseBlDetail> {
    return { ...HOUSE_BL_ROWS[0], ...(req as Record<string, unknown>) } as unknown as HouseBlDetail;
  },
  async update(id: number, req: UpdateHouseBlRequest): Promise<HouseBlDetail> {
    const row = HOUSE_BL_ROWS.find(r => r.id === id) ?? HOUSE_BL_ROWS[0];
    return { ...row, ...(req as Record<string, unknown>) } as unknown as HouseBlDetail;
  },
  async delete(_id: number): Promise<void> {
    // mock: no-op
  },
};
