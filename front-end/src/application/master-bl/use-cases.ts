import type { MasterBlPort } from './ports';
import type { MasterBlFilter } from '@/domain/master-bl';

export function createMasterBlUseCases(port: MasterBlPort) {
  return {
    list: (filter: MasterBlFilter) => port.list(filter),
    getById: (id: number) => port.getById(id),
    delete: (id: number) => port.delete(id),
  };
}
