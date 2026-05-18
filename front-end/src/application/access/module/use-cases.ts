import { accessModulePort } from "@/lib/ports";
import type { CreateModuleDto, UpdateModuleDto } from "@/domain/access/module";

export const accessModuleUseCases = {
  search: (page: number, size?: number) => accessModulePort.search(page, size),
  getById: (moduleCode: string) => accessModulePort.getById(moduleCode),
  create: (req: CreateModuleDto) => accessModulePort.create(req),
  update: (moduleCode: string, req: UpdateModuleDto) => accessModulePort.update(moduleCode, req),
  delete: (moduleCode: string) => accessModulePort.delete(moduleCode),
  deleteMany: (codes: string[]) => accessModulePort.deleteMany(codes),
};
