import type { AirMasterPort } from './ports';
import type { AirMasterFilter } from '@/domain/air-master';

export function createAirMasterUseCases(port: AirMasterPort) {
  return {
    list: (filter: AirMasterFilter, page: number, size?: number) => port.list(filter, page, size),
  };
}
