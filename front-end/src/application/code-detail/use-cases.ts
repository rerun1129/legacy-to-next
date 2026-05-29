import { codeDetailPort } from "@/lib/ports";
import type { CreateCodeDetailRequestDto, UpdateCodeDetailRequestDto, SaveCodeDetailChangesRequest } from "@/domain/code-detail";

export const codeDetailUseCases = {
  search: (masterId: number, page: number, size?: number) =>
    codeDetailPort.search(masterId, page, size),
  getById: (id: number) => codeDetailPort.getById(id),
  create: (req: CreateCodeDetailRequestDto) => codeDetailPort.create(req),
  update: (id: number, req: UpdateCodeDetailRequestDto) => codeDetailPort.update(id, req),
  delete: (id: number) => codeDetailPort.delete(id),
  deleteMany: (ids: number[]) => codeDetailPort.deleteMany(ids),
  saveChanges: (req: SaveCodeDetailChangesRequest) => codeDetailPort.saveChanges(req),
};
