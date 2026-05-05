import type { TruckBlPort } from './ports';
import type { TruckBlFilter } from '@/domain/truck-bl';

export function createTruckBlUseCases(port: TruckBlPort) {
  return {
    list: (filter: TruckBlFilter, page: number, size?: number) => port.list(filter, page, size),
  };
}
