import type { HouseBlRow, HouseBlDetail, HouseBlFilter, CreateHouseBlRequest, UpdateHouseBlRequest } from '@/domain/house-bl';

export interface HouseBlPort {
  list(filter: HouseBlFilter): Promise<HouseBlRow[]>;
  getById(id: number): Promise<HouseBlDetail>;
  save(data: unknown): Promise<HouseBlDetail>;
  create(req: CreateHouseBlRequest): Promise<HouseBlDetail>;
  // §6.29 — SEA jobDiv 분기에서 BE가 ApiResponse<Void> 반환 → null, 그 외 HouseBlDetail 반환
  update(id: number, req: UpdateHouseBlRequest): Promise<HouseBlDetail | null>;
  delete(id: number): Promise<void>;
  changeHblNo(id: number, hblNo: string): Promise<void>;
}
