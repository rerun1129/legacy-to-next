import type { MasterBlPort } from './ports';
import type { MasterBlFilter, CreateMasterBlRequest, UpdateMasterBlRequest } from '@/domain/master-bl';

export function createMasterBlUseCases(port: MasterBlPort) {
  return {
    list: (filter: MasterBlFilter) => port.list(filter),
    getById: (id: number) => port.getById(id),
    create: (req: CreateMasterBlRequest) => port.create(req),
    update: (id: number, req: UpdateMasterBlRequest) => port.update(id, req),
    delete: (id: number) => port.delete(id),
  };
}
