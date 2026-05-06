import type { AirHousePort } from './ports';
import type { AirHouseFilter } from '@/domain/air-house';

export function createAirHouseUseCases(port: AirHousePort) {
  return {
    list: (filter: AirHouseFilter, page: number, size?: number) => port.list(filter, page, size),
  };
}
