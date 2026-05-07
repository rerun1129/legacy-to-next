import type { SeaHousePort } from './ports';
import type { SeaHouseFilter } from '@/domain/sea-house';

export function createSeaHouseUseCases(port: SeaHousePort) {
  return {
    list: (filter: SeaHouseFilter, page: number, size?: number) => port.list(filter, page, size),
  };
}
