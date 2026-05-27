import { packageUnitPort } from "@/lib/ports";
import type {
  PackageUnitFilter,
  CreatePackageUnitRequestDto,
  UpdatePackageUnitRequestDto,
  SavePackageUnitChangesRequestDto,
} from "@/domain/code/package-unit";

export const packageUnitUseCases = {
  search: (filter: PackageUnitFilter, page: number, size?: number) => packageUnitPort.search(filter, page, size),
  getById: (id: number) => packageUnitPort.getById(id),
  create: (req: CreatePackageUnitRequestDto) => packageUnitPort.create(req),
  update: (id: number, req: UpdatePackageUnitRequestDto) => packageUnitPort.update(id, req),
  delete: (id: number) => packageUnitPort.delete(id),
  deleteMany: (ids: number[]) => packageUnitPort.deleteMany(ids),
  saveChanges: (req: SavePackageUnitChangesRequestDto) => packageUnitPort.saveChanges(req),
  autocomplete: (q: string, limit?: number) => packageUnitPort.autocomplete(q, limit),
};
