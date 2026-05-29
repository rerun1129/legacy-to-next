import { codeMasterPort } from "@/lib/ports";
import type { CodeMasterFilter, CreateCodeMasterRequestDto, UpdateCodeMasterRequestDto, SaveCodeMasterChangesRequest } from "@/domain/code-master";

export const codeMasterUseCases = {
  search: (filter: CodeMasterFilter, page: number, size?: number) =>
    codeMasterPort.search(filter, page, size),
  getById: (id: number) => codeMasterPort.getById(id),
  create: (req: CreateCodeMasterRequestDto) => codeMasterPort.create(req),
  update: (id: number, req: UpdateCodeMasterRequestDto) => codeMasterPort.update(id, req),
  delete: (id: number) => codeMasterPort.delete(id),
  deleteMany: (ids: number[]) => codeMasterPort.deleteMany(ids),
  saveChanges: (req: SaveCodeMasterChangesRequest) => codeMasterPort.saveChanges(req),
  autocomplete: (q: string, limit?: number) => codeMasterPort.autocomplete(q, limit),
};
