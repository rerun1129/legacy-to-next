import type { HouseBlRow, HouseBlDetail, HouseBlFilter, CreateHouseBlRequest, UpdateHouseBlRequest } from '@/domain/house-bl';

export interface HouseBlPort {
  list(filter: HouseBlFilter): Promise<HouseBlRow[]>;
  getById(id: number): Promise<HouseBlRow>;
  save(data: Partial<HouseBlRow>): Promise<HouseBlRow>;
  create(req: CreateHouseBlRequest): Promise<HouseBlDetail>;
  update(id: number, req: UpdateHouseBlRequest): Promise<HouseBlDetail>;
  delete(id: number): Promise<void>;
}
