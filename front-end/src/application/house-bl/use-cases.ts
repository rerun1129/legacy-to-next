import type { HouseBlPort } from './ports';
import type { HouseBlFilter } from '@/domain/house-bl';

export function createHouseBlUseCases(port: HouseBlPort) {
  return {
    list: (filter: HouseBlFilter) => port.list(filter),
    getById: (id: string) => port.getById(id),
    save: (data: Parameters<HouseBlPort['save']>[0]) => port.save(data),
    delete: (id: string) => port.delete(id),
  };
}
