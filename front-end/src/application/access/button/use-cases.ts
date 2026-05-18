import { accessButtonPort } from "@/lib/ports";
import type { CreateButtonDto, UpdateButtonDto } from "@/domain/access/button";

export const accessButtonUseCases = {
  search: (page: number, size?: number) => accessButtonPort.search(page, size),
  getById: (id: number) => accessButtonPort.getById(id),
  create: (req: CreateButtonDto) => accessButtonPort.create(req),
  update: (id: number, req: UpdateButtonDto) => accessButtonPort.update(id, req),
  delete: (id: number) => accessButtonPort.delete(id),
  deleteMany: (ids: number[]) => accessButtonPort.deleteMany(ids),
};
