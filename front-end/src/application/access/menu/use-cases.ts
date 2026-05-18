import { accessMenuPort } from "@/lib/ports";
import type { CreateMenuDto, UpdateMenuDto } from "@/domain/access/menu";

export const accessMenuUseCases = {
  search: (page: number, size?: number) => accessMenuPort.search(page, size),
  getById: (id: number) => accessMenuPort.getById(id),
  create: (req: CreateMenuDto) => accessMenuPort.create(req),
  update: (id: number, req: UpdateMenuDto) => accessMenuPort.update(id, req),
  delete: (id: number) => accessMenuPort.delete(id),
  deleteMany: (ids: number[]) => accessMenuPort.deleteMany(ids),
};
