import { carrierPort } from "@/lib/ports";
import type { CarrierFilter, CreateCarrierRequestDto, UpdateCarrierRequestDto } from "@/domain/code/carrier";

export const carrierUseCases = {
  search: (filter: CarrierFilter, page: number, size?: number) => carrierPort.search(filter, page, size),
  getById: (id: number) => carrierPort.getById(id),
  create: (req: CreateCarrierRequestDto) => carrierPort.create(req),
  update: (id: number, req: UpdateCarrierRequestDto) => carrierPort.update(id, req),
  delete: (id: number) => carrierPort.delete(id),
  deleteMany: (ids: number[]) => carrierPort.deleteMany(ids),
};
