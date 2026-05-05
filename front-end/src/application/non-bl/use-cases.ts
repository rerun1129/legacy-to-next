import type { NonBlPort } from './ports';
import type { NonBlFilter } from '@/domain/non-bl';

export function createNonBlUseCases(port: NonBlPort) {
  return {
    list: (filter: NonBlFilter) => port.list(filter),
  };
}
