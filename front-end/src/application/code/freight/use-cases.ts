import { freightPort } from "@/lib/ports";
import type { FreightFilter, CreateFreightRequestDto, UpdateFreightRequestDto, SaveFreightChangesRequestDto } from "@/domain/code/freight";

export const freightUseCases = {
  search: (filter: FreightFilter, page: number, size?: number) => freightPort.search(filter, page, size),
  getById: (id: number) => freightPort.getById(id),
  create: (req: CreateFreightRequestDto) => freightPort.create(req),
  update: (id: number, req: UpdateFreightRequestDto) => freightPort.update(id, req),
  delete: (id: number) => freightPort.delete(id),
  deleteMany: (ids: number[]) => freightPort.deleteMany(ids),
  saveChanges: (req: SaveFreightChangesRequestDto) => freightPort.saveChanges(req),
  autocomplete: (q: string, limit?: number) => freightPort.autocomplete(q, limit),
};
