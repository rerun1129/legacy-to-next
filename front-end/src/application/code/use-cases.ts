import { codePort } from "@/lib/ports";
import type { CodeFilter, CreateCodeRequestDto, UpdateCodeRequestDto } from "@/domain/code";

export const codeUseCases = {
  search: (filter: CodeFilter, page: number, size?: number) => codePort.search(filter, page, size),
  getById: (id: number) => codePort.getById(id),
  create: (req: CreateCodeRequestDto) => codePort.create(req),
  update: (id: number, req: UpdateCodeRequestDto) => codePort.update(id, req),
  delete: (id: number) => codePort.delete(id),
};
