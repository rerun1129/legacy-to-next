import type { SeaMasterPort } from './ports';
import type { SeaMasterFilter } from '@/domain/sea-master';

export function createSeaMasterUseCases(port: SeaMasterPort) {
  return {
    list: (filter: SeaMasterFilter, page: number, size?: number) => port.list(filter, page, size),
  };
}
