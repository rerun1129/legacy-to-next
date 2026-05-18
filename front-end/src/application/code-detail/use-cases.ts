import { codeDetailPort } from "@/lib/ports";
import type { CodeDetailFilter, CreateCodeDetailRequestDto, UpdateCodeDetailRequestDto } from "@/domain/code-detail";

export const codeDetailUseCases = {
  search: (masterId: number, filter: CodeDetailFilter, page: number, size?: number) =>
    codeDetailPort.search(masterId, filter, page, size),
  getById: (id: number) => codeDetailPort.getById(id),
  create: (req: CreateCodeDetailRequestDto) => codeDetailPort.create(req),
  update: (id: number, req: UpdateCodeDetailRequestDto) => codeDetailPort.update(id, req),
  delete: (id: number) => codeDetailPort.delete(id),
};
