import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlFilter } from '@/domain/house-bl';
import { NotFoundError } from '@/adapter/out/api/errors';

const MOCK_ROWS: HouseBlRow[] = [
  { id: 1, hblNo: 'HBLKR24041956', bound: 'EXP', polCode: 'KRBSAN', podCode: 'CNSHA', shipperCode: 'HANJIN', consigneeCode: 'SHANGHAI_TRADING', createdAt: '2024-04-20' },
  { id: 2, hblNo: 'HBLKR24041901', bound: 'EXP', polCode: 'KRICN',  podCode: 'DEHAM', shipperCode: 'SAMSUNG',  consigneeCode: 'SAMSUNG_EU', createdAt: '2024-04-19' },
  { id: 3, hblNo: 'HBLKR24041623', bound: 'IMP', polCode: 'CNNGB',  podCode: 'KRICN', shipperCode: 'NINGBO',   consigneeCode: 'KR_IMPORT',  createdAt: '2024-04-16' },
];

export const mockHouseBlPort: HouseBlPort = {
  async list(_filter: HouseBlFilter): Promise<HouseBlRow[]> {
    return MOCK_ROWS;
  },
  async getById(id: number): Promise<HouseBlRow> {
    const row = MOCK_ROWS.find((r) => r.id === id);
    if (!row) throw new NotFoundError('HouseBl', String(id));
    return row;
  },
  async save(data): Promise<HouseBlRow> {
    return { ...MOCK_ROWS[0], ...data } as HouseBlRow;
  },
  async delete(_id: number): Promise<void> {
    // mock: no-op
  },
};
