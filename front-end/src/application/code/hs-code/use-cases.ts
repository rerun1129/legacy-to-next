import { hsCodePort } from "@/lib/ports";
import type {
  HsCodeFilter,
  CreateHsCodeRequestDto,
  UpdateHsCodeRequestDto,
  SaveHsCodeChangesRequestDto,
} from "@/domain/code/hs-code";

export const hsCodeUseCases = {
  search: (filter: HsCodeFilter, page: number, size?: number) => hsCodePort.search(filter, page, size),
  getById: (id: number) => hsCodePort.getById(id),
  create: (req: CreateHsCodeRequestDto) => hsCodePort.create(req),
  update: (id: number, req: UpdateHsCodeRequestDto) => hsCodePort.update(id, req),
  delete: (id: number) => hsCodePort.delete(id),
  deleteMany: (ids: number[]) => hsCodePort.deleteMany(ids),
  saveChanges: (req: SaveHsCodeChangesRequestDto) => hsCodePort.saveChanges(req),
  autocomplete: (q: string, limit?: number) => hsCodePort.autocomplete(q, limit),
};
