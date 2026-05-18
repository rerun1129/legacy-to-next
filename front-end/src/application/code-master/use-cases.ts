import { codeMasterPort } from "@/lib/ports";
import type { CodeMasterFilter, CreateCodeMasterRequestDto, UpdateCodeMasterRequestDto } from "@/domain/code-master";

export const codeMasterUseCases = {
  search: (filter: CodeMasterFilter, page: number, size?: number) =>
    codeMasterPort.search(filter, page, size),
  getById: (id: number) => codeMasterPort.getById(id),
  create: (req: CreateCodeMasterRequestDto) => codeMasterPort.create(req),
  update: (id: number, req: UpdateCodeMasterRequestDto) => codeMasterPort.update(id, req),
  delete: (id: number) => codeMasterPort.delete(id),
  deleteMany: (ids: number[]) => codeMasterPort.deleteMany(ids),
};
