import type { HouseBlRow, HouseBlDetail, HouseBlFilter } from '@/domain/house-bl';

export interface HouseBlPort {
  list(filter: HouseBlFilter): Promise<HouseBlRow[]>;
  getById(id: number): Promise<HouseBlDetail>;
  save(data: unknown): Promise<HouseBlDetail>;
  delete(id: number): Promise<void>;
}
