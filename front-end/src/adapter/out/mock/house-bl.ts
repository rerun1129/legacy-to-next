import type { HouseBlPort } from '@/application/house-bl/ports';
import type { HouseBlRow, HouseBlDetail, HouseBlFilter, CreateHouseBlRequest, UpdateHouseBlRequest } from '@/domain/house-bl';
import { NotFoundError } from '@/adapter/out/api/errors';

const MOCK_ROWS: HouseBlRow[] = [
  { id: 1, hblNo: 'HBLKR24041956', jobDiv: 'SEA', bound: 'EXP', polCode: 'KRBSAN', podCode: 'CNSHA', etd: null, eta: null, shipperCode: 'HANJIN', consigneeCode: 'SHANGHAI_TRADING', pkgQty: null, pkgUnit: null, createdAt: '2024-04-20' },
  { id: 2, hblNo: 'HBLKR24041901', jobDiv: 'SEA', bound: 'EXP', polCode: 'KRICN',  podCode: 'DEHAM', etd: null, eta: null, shipperCode: 'SAMSUNG',  consigneeCode: 'SAMSUNG_EU', pkgQty: null, pkgUnit: null, createdAt: '2024-04-19' },
  { id: 3, hblNo: 'HBLKR24041623', jobDiv: 'SEA', bound: 'IMP', polCode: 'CNNGB',  podCode: 'KRICN', etd: null, eta: null, shipperCode: 'NINGBO',   consigneeCode: 'KR_IMPORT',  pkgQty: null, pkgUnit: null, createdAt: '2024-04-16' },
];

export const mockHouseBlPort: HouseBlPort = {
  async list(_filter: HouseBlFilter): Promise<HouseBlRow[]> {
    return MOCK_ROWS as unknown as HouseBlRow[];
  },
  async getById(id: number): Promise<HouseBlDetail> {
    const row = MOCK_ROWS.find((r) => r.id === id);
    if (!row) throw new NotFoundError('HouseBl', String(id));
    return row as unknown as HouseBlDetail;
  },
  async save(data: unknown): Promise<HouseBlDetail> {
    return { ...MOCK_ROWS[0], ...(data as Record<string, unknown>) } as unknown as HouseBlDetail;
  },
  async create(req: CreateHouseBlRequest): Promise<HouseBlDetail> {
    return { ...MOCK_ROWS[0], ...(req as unknown as Record<string, unknown>) } as unknown as HouseBlDetail;
  },
  async update(id: number, req: UpdateHouseBlRequest): Promise<HouseBlDetail> {
    const row = MOCK_ROWS.find(r => r.id === id) ?? MOCK_ROWS[0];
    return { ...row, ...(req as Record<string, unknown>) } as unknown as HouseBlDetail;
  },
  async delete(_id: number): Promise<void> {
    // mock: no-op
  },
};
