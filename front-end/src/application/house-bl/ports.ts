import type { HouseBlRow, HouseBlFilter } from '@/domain/house-bl';

export interface HouseBlPort {
  list(filter: HouseBlFilter): Promise<HouseBlRow[]>;
  getById(id: number): Promise<HouseBlRow>;
  save(data: Partial<HouseBlRow>): Promise<HouseBlRow>;
  delete(id: number): Promise<void>;
}
